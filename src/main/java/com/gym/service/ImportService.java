package com.gym.service;

import com.gym.entity.Rate;
import com.gym.web.form.EquipmentForm;
import com.gym.web.form.MemberForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bulk import of members and equipment from a CSV file.
 *
 * Every row goes through the same rules as the normal entry forms (MemberForm / EquipmentForm),
 * and the file is all-or-nothing: if one row has a problem, nothing is saved and every problem
 * is listed with its line number, so the file can be fixed and uploaded again.
 */
@Service
public class ImportService {

    /** Largest number of data rows accepted in one file (each member password is hashed with BCrypt). */
    public static final int MAX_ROWS = 1000;

    private static final int MAX_ERRORS_SHOWN = 50;
    private static final List<Integer> PLANS = List.of(1, 3, 6, 12);
    private static final List<String> GENDERS = List.of("Male", "Female", "Other");

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            strict("d/M/uuuu"), strict("d-M-uuuu"), strict("d.M.uuuu"), strict("uuuu/M/d")
    };

    private static DateTimeFormatter strict(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    // ------------------------------------------------------------------ what the import pages show

    /** One column of a CSV template, described on the import page. */
    public static final class Column {
        private final String name;
        private final boolean required;
        private final String help;

        Column(String name, boolean required, String help) {
            this.name = name;
            this.required = required;
            this.help = help;
        }

        public String getName() { return name; }
        public boolean isRequired() { return required; }
        public String getHelp() { return help; }
    }

    public static final List<Column> MEMBER_COLUMNS = List.of(
            new Column("fullname", true, "Full name, 20 characters at most."),
            new Column("username", true, "Login name, 20 characters at most. Must be unique."),
            new Column("password", true, "First password of the member (stored encrypted)."),
            new Column("gender", true, "Male, Female or Other."),
            new Column("dor", false, "Date of registration, like 2026-10-26 or 26/10/2026. Today if empty."),
            new Column("services", true, "One of the services of the gym (see the list below)."),
            new Column("plan", true, "Number of months: 1, 3, 6 or 12."),
            new Column("amount", false, "Price per month. The monthly rate of the service if empty."),
            new Column("address", true, "20 characters at most."),
            new Column("contact", true, "Phone number, digits only (6 to 15).")
    );

    public static final List<Column> EQUIPMENT_COLUMNS = List.of(
            new Column("name", true, "Equipment name, 30 characters at most."),
            new Column("description", true, "50 characters at most."),
            new Column("date", false, "Date of purchase, like 2026-10-26 or 26/10/2026. Today if empty."),
            new Column("quantity", true, "Number of items, at least 1."),
            new Column("vendor", true, "Vendor name, 50 characters at most."),
            new Column("address", true, "Vendor address, 20 characters at most."),
            new Column("contact", true, "Vendor phone number, digits only (6 to 15)."),
            new Column("amount", true, "Cost of ONE item (the total is calculated).")
    );

    /** Outcome of an import. When errors is not empty, nothing was saved. */
    public static final class Result {
        private int rows;
        private int imported;
        private final List<String> errors = new ArrayList<>();
        private int hiddenErrors;

        void error(String message) {
            if (errors.size() < MAX_ERRORS_SHOWN) {
                errors.add(message);
            } else {
                hiddenErrors++;
            }
        }

        public int getRows() { return rows; }
        public int getImported() { return imported; }
        public List<String> getErrors() { return errors; }
        public int getHiddenErrors() { return hiddenErrors; }
        public boolean isOk() { return errors.isEmpty() && imported > 0; }
    }

    // ------------------------------------------------------------------ service

    private final MemberService members;
    private final EquipmentService equipment;
    private final Validator validator;
    private final GymClock clock;

    public ImportService(MemberService members, EquipmentService equipment, Validator validator, GymClock clock) {
        this.members = members;
        this.equipment = equipment;
        this.validator = validator;
        this.clock = clock;
    }

    /** Names of the services members can choose (table "rates"), for the import page. */
    @Transactional(readOnly = true)
    public List<String> serviceNames() {
        return members.rates().stream().map(Rate::getName).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ templates

    @Transactional(readOnly = true)
    public String memberTemplate() {
        List<Rate> rates = members.rates();
        String service = rates.isEmpty() ? "Fitness" : rates.get(0).getName();
        String today = clock.today().toString();
        return template(MEMBER_COLUMNS, List.of(
                List.of("Sara Ben Ali", "sara.benali", "ChangeMe123", "Female", today, service, "3", "", "Tunis", "21612345678"),
                List.of("Ali Trabelsi", "ali.trabelsi", "ChangeMe456", "Male", "", service, "1", "", "Ariana", "21698765432")));
    }

    public String equipmentTemplate() {
        String today = clock.today().toString();
        return template(EQUIPMENT_COLUMNS, List.of(
                List.of("Treadmill", "Running machine, 20 km/h", today, "2", "Sport Pro", "Tunis", "21671234567", "1200"),
                List.of("Dumbbell set", "2 to 20 kg", "", "5", "Fit Shop", "Sfax", "21674123456", "300")));
    }

    private static String template(List<Column> columns, List<List<String>> examples) {
        try {
            StringWriter out = new StringWriter();
            CsvSupport.writeStart(out);
            CsvSupport.writeRow(out, columns.stream().map(Column::getName).collect(Collectors.toList()));
            for (List<String> example : examples) {
                CsvSupport.writeRow(out, example);
            }
            return out.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e); // a StringWriter never fails
        }
    }

    // ------------------------------------------------------------------ members

    @Transactional
    public Result importMembers(InputStream in) throws IOException {
        Result result = new Result();
        Table table = read(in, MEMBER_COLUMNS, result);
        if (table == null) {
            return result;
        }

        Map<String, Rate> rates = new LinkedHashMap<>();
        for (Rate r : members.rates()) {
            rates.put(r.getName().trim().toLowerCase(Locale.ROOT), r);
        }
        String allowedServices = rates.values().stream().map(Rate::getName).collect(Collectors.joining(", "));

        Map<String, Integer> usernameLines = new HashMap<>();
        List<MemberForm> forms = new ArrayList<>();

        for (CsvSupport.Row row : table.rows) {
            Check check = new Check();
            MemberForm f = new MemberForm();
            f.setFullname(table.get(row, "fullname"));
            f.setUsername(table.get(row, "username"));
            f.setPassword(table.get(row, "password"));
            f.setAddress(table.get(row, "address"));
            f.setContact(table.get(row, "contact"));

            if (f.getPassword().isEmpty()) {
                check.fail("password", "Password is required");
            }

            String genderText = table.get(row, "gender");
            String gender = GENDERS.stream().filter(g -> g.equalsIgnoreCase(genderText)).findFirst().orElse(null);
            if (gender == null) {
                check.fail("gender", "Gender must be Male, Female or Other");
            } else {
                f.setGender(gender);
            }

            Integer plan = parseInt(table.get(row, "plan"));
            if (plan == null || !PLANS.contains(plan)) {
                check.fail("plan", "Plan must be 1, 3, 6 or 12 (months)");
            } else {
                f.setPlan(plan);
            }

            String serviceText = table.get(row, "services");
            Rate rate = rates.get(serviceText.toLowerCase(Locale.ROOT));
            if (rate == null) {
                check.fail("services", "Unknown service '" + serviceText + "' (choose one of: " + allowedServices + ")");
            } else {
                f.setServices(rate.getName());
            }

            String amountText = table.get(row, "amount");
            if (amountText.isEmpty()) {
                if (rate != null) {
                    f.setAmount(rate.getChargeValue());
                } else {
                    check.skip("amount"); // the service error above already explains it
                }
            } else {
                Integer amount = parseInt(amountText);
                if (amount == null) {
                    check.fail("amount", "Amount must be a whole number");
                } else {
                    f.setAmount(amount);
                }
            }

            String dorText = table.get(row, "dor");
            if (dorText.isEmpty()) {
                f.setDor(clock.today());
            } else {
                LocalDate dor = parseDate(dorText);
                if (dor == null) {
                    check.fail("dor", "Date '" + dorText + "' is not valid (use 2026-10-26 or 26/10/2026)");
                } else {
                    f.setDor(dor);
                }
            }

            check.addViolations(validator.validate(f));

            String username = f.getUsername();
            if (!username.isEmpty()) {
                Integer firstLine = usernameLines.putIfAbsent(username.toLowerCase(Locale.ROOT), row.getLine());
                if (firstLine != null) {
                    check.problems.add("Username '" + username + "' is also used on line " + firstLine);
                } else if (members.usernameTaken(username, null)) {
                    check.problems.add("Username '" + username + "' is already taken");
                }
            }

            if (check.problems.isEmpty()) {
                forms.add(f);
            } else {
                result.error("Line " + row.getLine() + ": " + String.join("; ", check.problems));
            }
        }

        if (!result.errors.isEmpty()) {
            return result; // nothing is saved
        }
        for (MemberForm f : forms) {
            members.create(f);
        }
        result.imported = forms.size();
        return result;
    }

    // ------------------------------------------------------------------ equipment

    @Transactional
    public Result importEquipment(InputStream in) throws IOException {
        Result result = new Result();
        Table table = read(in, EQUIPMENT_COLUMNS, result);
        if (table == null) {
            return result;
        }

        List<EquipmentForm> forms = new ArrayList<>();
        for (CsvSupport.Row row : table.rows) {
            Check check = new Check();
            EquipmentForm f = new EquipmentForm();
            f.setName(table.get(row, "name"));
            f.setDescription(table.get(row, "description"));
            f.setVendor(table.get(row, "vendor"));
            f.setAddress(table.get(row, "address"));
            f.setContact(table.get(row, "contact"));

            Integer quantity = parseInt(table.get(row, "quantity"));
            if (quantity == null) {
                check.fail("quantity", "Quantity must be a whole number (1 or more)");
            } else {
                f.setQuantity(quantity);
            }

            Integer amount = parseInt(table.get(row, "amount"));
            if (amount == null) {
                check.fail("amount", "Cost per item must be a whole number");
            } else {
                f.setAmount(amount);
            }

            String dateText = table.get(row, "date");
            if (dateText.isEmpty()) {
                f.setDate(clock.today());
            } else {
                LocalDate date = parseDate(dateText);
                if (date == null) {
                    check.fail("date", "Date '" + dateText + "' is not valid (use 2026-10-26 or 26/10/2026)");
                } else {
                    f.setDate(date);
                }
            }

            check.addViolations(validator.validate(f));

            if (check.problems.isEmpty()) {
                forms.add(f);
            } else {
                result.error("Line " + row.getLine() + ": " + String.join("; ", check.problems));
            }
        }

        if (!result.errors.isEmpty()) {
            return result; // nothing is saved
        }
        for (EquipmentForm f : forms) {
            equipment.create(f);
        }
        result.imported = forms.size();
        return result;
    }

    // ------------------------------------------------------------------ helpers

    /** Header of the file (column name -> position) and its data rows. */
    private static final class Table {
        private final Map<String, Integer> columns;
        private final List<CsvSupport.Row> rows;

        Table(Map<String, Integer> columns, List<CsvSupport.Row> rows) {
            this.columns = columns;
            this.rows = rows;
        }

        String get(CsvSupport.Row row, String column) {
            Integer index = columns.get(column);
            return index == null ? "" : row.get(index);
        }
    }

    /** Problems found on one row. "failed" holds the fields already explained, so they are not reported twice. */
    private static final class Check {
        private final List<String> problems = new ArrayList<>();
        private final Set<String> failed = new HashSet<>();

        void fail(String property, String message) {
            problems.add(message);
            failed.add(property);
        }

        void skip(String property) {
            failed.add(property);
        }

        <T> void addViolations(Set<ConstraintViolation<T>> violations) {
            violations.stream()
                    .filter(v -> !failed.contains(v.getPropertyPath().toString()))
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .forEach(problems::add);
        }
    }

    /** Reads the file and checks the header. Returns null (with the reason in result) if it cannot be imported. */
    private Table read(InputStream in, List<Column> columns, Result result) throws IOException {
        List<CsvSupport.Row> all = CsvSupport.parse(in);
        if (all.isEmpty()) {
            result.error("The file is empty.");
            return null;
        }

        CsvSupport.Row head = all.get(0);
        Map<String, Integer> header = new HashMap<>();
        for (int i = 0; i < head.getCells().size(); i++) {
            header.putIfAbsent(columnKey(head.get(i)), i);
        }
        List<String> missing = columns.stream()
                .filter(Column::isRequired)
                .map(Column::getName)
                .filter(name -> !header.containsKey(name))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            result.error("Missing column(s): " + String.join(", ", missing)
                    + ". The first line of the file must contain the column names of the template.");
            return null;
        }

        List<CsvSupport.Row> rows = all.subList(1, all.size());
        result.rows = rows.size();
        if (rows.isEmpty()) {
            result.error("The file has no data below the column names.");
            return null;
        }
        if (rows.size() > MAX_ROWS) {
            result.error("The file has " + rows.size() + " rows but the limit is " + MAX_ROWS
                    + " rows per file. Split it into several files.");
            return null;
        }
        return new Table(header, rows);
    }

    /** "Full Name", "full_name" and "fullname" are the same column. */
    private static String columnKey(String header) {
        return header.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    /** Whole number, also accepts 50.0 (Excel) - null if it is not one. */
    private static Integer parseInt(String text) {
        String v = text.trim().replaceAll("[.,]0+$", "");
        return v.matches("-?\\d{1,9}") ? Integer.valueOf(v) : null;
    }

    private static LocalDate parseDate(String text) {
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(text.trim(), format);
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        return null;
    }
}
