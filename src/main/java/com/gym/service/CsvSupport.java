package com.gym.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Small CSV reader / writer (no extra library needed).
 *
 * Reading: UTF-8 (with or without BOM), the separator is detected from the first line
 * (comma, semicolon or tab, so files saved by Excel in any language work), quoted cells,
 * doubled quotes and line breaks inside quotes are handled. An Excel "sep=," first line is skipped.
 *
 * Writing: every file starts with a BOM and a "sep=," line so Excel opens it in columns
 * whatever the language of the computer. Text cells that start with = + - @ are prefixed with
 * an apostrophe so a spreadsheet can never run them as a formula.
 */
public final class CsvSupport {

    private CsvSupport() {
    }

    /** One data row and the line of the file where it starts (for error messages). */
    public static final class Row {
        private final int line;
        private final List<String> cells;

        Row(int line, List<String> cells) {
            this.line = line;
            this.cells = cells;
        }

        public int getLine() { return line; }
        public List<String> getCells() { return cells; }

        public String get(int index) {
            return index >= 0 && index < cells.size() ? cells.get(index).trim() : "";
        }
    }

    // ------------------------------------------------------------------ reading

    public static List<Row> parse(InputStream in) throws IOException {
        return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }

    /** Returns every non-empty row, the header row included. */
    public static List<Row> parse(String text) {
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        int lineOffset = 0;
        if (text.regionMatches(true, 0, "sep=", 0, 4)) {
            int nl = text.indexOf('\n');
            text = nl < 0 ? "" : text.substring(nl + 1);
            lineOffset = 1;
        }

        char sep = detectSeparator(text);
        List<Row> rows = new ArrayList<>();
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        int line = 1 + lineOffset;
        int rowStart = line;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    if (c == '\n') {
                        line++;
                    }
                    cell.append(c);
                }
            } else if (c == '"' && cell.toString().isBlank()) {
                cell.setLength(0);
                quoted = true;
            } else if (c == sep) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                cells.add(cell.toString());
                cell.setLength(0);
                addRow(rows, rowStart, cells);
                cells = new ArrayList<>();
                line++;
                rowStart = line;
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString());
        addRow(rows, rowStart, cells);
        return rows;
    }

    private static void addRow(List<Row> rows, int line, List<String> cells) {
        boolean empty = true;
        for (String s : cells) {
            if (!s.isBlank()) {
                empty = false;
                break;
            }
        }
        if (!empty) {
            rows.add(new Row(line, cells));
        }
    }

    private static char detectSeparator(String text) {
        int end = text.indexOf('\n');
        String first = end < 0 ? text : text.substring(0, end);
        int commas = count(first, ',');
        int semicolons = count(first, ';');
        int tabs = count(first, '\t');
        if (semicolons > commas && semicolons >= tabs) {
            return ';';
        }
        if (tabs > commas && tabs > semicolons) {
            return '\t';
        }
        return ',';
    }

    private static int count(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ writing

    /** Writes the BOM and the "sep=," line. Call once at the start of every file. */
    public static void writeStart(Writer out) throws IOException {
        out.write('\uFEFF');
        out.write("sep=,\r\n");
    }

    public static void writeRow(Writer out, List<?> values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(values.get(i)));
        }
        sb.append("\r\n");
        out.write(sb.toString());
    }

    static String escape(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof byte[]) {
            return "(binary)";
        }
        String s = value.toString();
        if (!(value instanceof Number) && !s.isEmpty()) {
            char first = s.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
                s = "'" + s;
            }
        }
        boolean needsQuotes = s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0
                || s.indexOf('\r') >= 0 || s.startsWith(" ") || s.endsWith(" ");
        if (needsQuotes) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
