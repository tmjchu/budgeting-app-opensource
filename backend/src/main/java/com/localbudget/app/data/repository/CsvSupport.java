package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.service.CredentialCryptoService;
import com.localbudget.app.domain.service.CsvDataEncryptionService;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import tools.jackson.databind.ObjectMapper;

abstract class CsvSupport {

    private final BudgetAppProperties properties;
    private final CsvDataEncryptionService encryptionService;

    protected CsvSupport(BudgetAppProperties properties) {
        this(
                properties,
                new CsvDataEncryptionService(
                        properties,
                        new CredentialCryptoService(),
                        new ObjectMapper(),
                        Clock.systemUTC()));
    }

    protected CsvSupport(
            BudgetAppProperties properties, CsvDataEncryptionService encryptionService) {
        this.properties = properties;
        this.encryptionService = encryptionService;
    }

    protected Path path(String fileName) {
        return properties.dataDirectory().resolve(fileName);
    }

    protected List<CSVRecord> readRecords(String fileName, String[] headers) {
        byte[] content = encryptionService.read(fileName);
        if (content == null) {
            return List.of();
        }

        try (Reader reader = new StringReader(new String(content, StandardCharsets.UTF_8));
                CSVParser parser =
                        CSVFormat.DEFAULT
                                .builder()
                                .setHeader(headers)
                                .setSkipHeaderRecord(true)
                                .build()
                                .parse(reader)) {
            return new ArrayList<>(parser.getRecords());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + path(fileName), ex);
        }
    }

    protected void writeRows(String fileName, String[] headers, List<List<String>> rows) {
        try (Writer writer = new StringWriter();
                CSVPrinter printer =
                        CSVFormat.DEFAULT.builder().setHeader(headers).build().print(writer)) {
            for (List<String> row : rows) {
                printer.printRecord(row);
            }
            printer.flush();
            encryptionService.write(fileName, writer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write " + path(fileName), ex);
        }
    }

    protected boolean exists(String fileName) {
        return encryptionService.exists(fileName);
    }

    protected static String value(CSVRecord record, String key) {
        if (!record.isMapped(key) || !record.isSet(key)) {
            return null;
        }
        String value = record.get(key);
        return value == null || value.isBlank() ? null : value;
    }

    protected static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
