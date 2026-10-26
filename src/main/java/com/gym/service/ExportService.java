package com.gym.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exports database tables to CSV files (one table, or all of them in a ZIP).
 *
 * The list of tables is read from the database itself, so a table added later is exported
 * without any change here, and a table name coming from the browser is only used if it is
 * in that list. Password columns are never written.
 */
@Service
public class ExportService {

    private static final Set<String> SECRET_COLUMNS = Set.of("password", "passwd", "pwd");

    private final DataSource dataSource;

    public ExportService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** A table and its number of rows, for the Reporting page. */
    public static final class TableInfo {
        private final String name;
        private final long rows;

        TableInfo(String name, long rows) {
            this.name = name;
            this.rows = rows;
        }

        public String getName() { return name; }
        public long getRows() { return rows; }
    }

    public List<TableInfo> tables() throws SQLException {
        List<TableInfo> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String table : tableNames(connection)) {
                try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + quote(table))) {
                    rs.next();
                    result.add(new TableInfo(table, rs.getLong(1)));
                }
            }
        }
        return result;
    }

    public boolean exists(String table) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return tableNames(connection).contains(table);
        }
    }

    /** Writes one table as CSV. The caller checks exists() first and closes the stream. */
    public void exportTable(String table, OutputStream out) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableNames(connection).contains(table)) {
                throw new IllegalArgumentException("Unknown table: " + table);
            }
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writeTable(connection, table, writer);
            writer.flush();
        }
    }

    /** Writes every table as its own CSV file inside one ZIP. The caller closes the stream. */
    public void exportAll(OutputStream out) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection()) {
            ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
            Writer writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
            for (String table : tableNames(connection)) {
                zip.putNextEntry(new ZipEntry(table + ".csv"));
                writeTable(connection, table, writer);
                writer.flush();
                zip.closeEntry();
            }
            zip.finish();
        }
    }

    // ------------------------------------------------------------------ internals

    private static void writeTable(Connection connection, String table, Writer writer) throws SQLException, IOException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + quote(table))) {
            ResultSetMetaData meta = rs.getMetaData();
            List<Integer> columns = new ArrayList<>();
            List<String> header = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String label = meta.getColumnLabel(i);
                if (!SECRET_COLUMNS.contains(label.toLowerCase(Locale.ROOT))) {
                    columns.add(i);
                    header.add(label);
                }
            }

            CsvSupport.writeStart(writer);
            CsvSupport.writeRow(writer, header);

            List<Object> row = new ArrayList<>(columns.size());
            while (rs.next()) {
                row.clear();
                for (int column : columns) {
                    Object value = rs.getObject(column);
                    row.add(value instanceof Blob ? "(binary)" : value);
                }
                CsvSupport.writeRow(writer, row);
            }
        }
    }

    private static List<String> tableNames(Connection connection) throws SQLException {
        List<String> names = new ArrayList<>();
        DatabaseMetaData meta = connection.getMetaData();
        // catalog = the database (MySQL); schema is null on MySQL and set on other databases
        try (ResultSet rs = meta.getTables(connection.getCatalog(), connection.getSchema(), "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }
        Collections.sort(names);
        return names;
    }

    private static String quote(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }
}
