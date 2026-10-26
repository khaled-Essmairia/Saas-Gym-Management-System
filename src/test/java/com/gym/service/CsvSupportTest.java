package com.gym.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvSupportTest {

    @Test
    void readsExcelFileWithSemicolonsQuotesAndBlankLines() {
        String text = "\uFEFFsep=;\r\nfullname;username;note\r\n\"Doe, Jane\";jane;\"say \"\"hi\"\"\nbye\"\r\n\r\nBob;bob;\r\n";
        List<CsvSupport.Row> rows = CsvSupport.parse(text);

        assertEquals(3, rows.size());
        assertEquals("fullname", rows.get(0).get(0));
        assertEquals("Doe, Jane", rows.get(1).get(0));
        assertEquals("say \"hi\"\nbye", rows.get(1).get(2));
        assertEquals(3, rows.get(1).getLine());
        assertEquals(6, rows.get(2).getLine());
        assertEquals("", rows.get(2).get(2));
    }

    @Test
    void readsCommaAndTabFiles() {
        assertEquals("3", CsvSupport.parse("a,b,c\n1,2,3").get(1).get(2));
        assertEquals("2", CsvSupport.parse("a\tb\n1\t2\n").get(1).get(1));
    }

    @Test
    void writesQuotedCellsAndNeutralisesFormulas() throws IOException {
        StringWriter out = new StringWriter();
        CsvSupport.writeStart(out);
        CsvSupport.writeRow(out, Arrays.asList("a,b", "=1+1", 42, null, "x\"y", -5, "-abc"));

        assertEquals("\uFEFFsep=,\r\n\"a,b\",'=1+1,42,,\"x\"\"y\",-5,'-abc\r\n", out.toString());
    }

    @Test
    void whatIsWrittenCanBeReadBack() throws IOException {
        StringWriter out = new StringWriter();
        CsvSupport.writeStart(out);
        CsvSupport.writeRow(out, List.of("name", "note"));
        CsvSupport.writeRow(out, List.of("Doe, Jane", "line1\nline2"));

        List<CsvSupport.Row> rows = CsvSupport.parse(out.toString());
        assertEquals(2, rows.size());
        assertEquals("Doe, Jane", rows.get(1).get(0));
        assertEquals("line1\nline2", rows.get(1).get(1));
    }
}
