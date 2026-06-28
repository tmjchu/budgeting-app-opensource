package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

abstract class CsvSupport {

    private final BudgetAppProperties properties;

    protected CsvSupport(BudgetAppProperties properties) {
        this.properties = properties;
    }

    protected Path path(String fileName) {
        return properties.dataDirectory().resolve(fileName);
    }

    protected List<CSVRecord> readRecords(String fileName, String[] headers) {
        Path file = path(fileName);
        if (!Files.exists(file)) {
            return List.of();
        }

        try (Reader reader = Files.newBufferedReader(file);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader(headers)
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {
            return new ArrayList<>(parser.getRecords());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + file, ex);
        }
    }

    protected void writeRows(String fileName, String[] headers, List<List<String>> rows) {
        Path file = path(fileName);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file);
                 CSVPrinter printer = CSVFormat.DEFAULT.builder()
                         .setHeader(headers)
                         .build()
                         .print(writer)) {
                for (List<String> row : rows) {
                    printer.printRecord(row);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write " + file, ex);
        }
    }

    protected static String value(CSVRecord record, String key) {
        String value = record.get(key);
        return value == null || value.isBlank() ? null : value;
    }

    protected static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
