package com.localbudget.app.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.repository.EncryptedCredentialRepository;
import com.localbudget.app.data.repository.PlaintextCredentialRepository;
import com.localbudget.app.domain.model.SetupState;
import com.localbudget.app.domain.service.CredentialCryptoService;
import com.localbudget.app.domain.service.CsvDataEncryptionService;
import com.localbudget.app.domain.service.PlaidCredentialValidator;
import com.localbudget.app.domain.service.SetupService;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class SetupHttpTest {
    @TempDir Path dataDirectory;
    private MockMvc mvc;
    private SetupService service;

    @BeforeEach
    void setUp() {
        var properties =
                new BudgetAppProperties(
                        dataDirectory,
                        new BudgetAppProperties.PlaidConfig(
                                BudgetAppProperties.Environment.SANDBOX,
                                "",
                                "",
                                "Open Budget",
                                List.of("transactions"),
                                List.of("US")));
        var mapper = new ObjectMapper();
        var crypto = new CredentialCryptoService();
        var clock = Clock.systemUTC();
        service =
                new SetupService(
                        properties,
                        new EncryptedCredentialRepository(properties, mapper),
                        new PlaintextCredentialRepository(properties, mapper),
                        crypto,
                        mock(PlaidCredentialValidator.class),
                        new CsvDataEncryptionService(properties, crypto, mapper, clock),
                        mapper,
                        clock);
        mvc =
                MockMvcBuilders.standaloneSetup(new SetupController(service))
                        .setControllerAdvice(new ApiExceptionHandler())
                        .build();
    }

    @Test
    void acceptsSetupWithNoPasswordAndEncryptionOff() throws Exception {
        var response =
                mvc.perform(
                                post("/api/setup/credentials")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"clientId":"test-client","secret":"test-secret",
                                                 "environment":"SANDBOX","encryptCsvData":false}
                                                """))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(response)
                .contains(
                        "\"state\":\"ready\"",
                        "\"hasEncryptedSecrets\":false",
                        "\"csvEncryptionStatus\":\"plaintext\"")
                .doesNotContain("test-secret", "test-client");
        assertThat(dataDirectory.resolve("secrets.json")).exists();
        assertThat(dataDirectory.resolve("secrets.json.enc")).doesNotExist();
    }

    @Test
    void rejectsEncryptionWithoutPasswordThroughHttp() throws Exception {
        mvc.perform(
                        post("/api/setup/credentials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"clientId":"test-client","secret":"test-secret",
                                         "environment":"SANDBOX","encryptCsvData":true}
                                        """))
                .andExpect(status().isBadRequest());
        assertThat(service.status().state()).isEqualTo(SetupState.NEEDS_SETUP);
        assertThat(dataDirectory.resolve("secrets.json")).doesNotExist();
        assertThat(dataDirectory.resolve("secrets.json.enc")).doesNotExist();
    }

    @Test
    void acceptsEncryptionWithValidPasswordThroughHttp() throws Exception {
        mvc.perform(
                        post("/api/setup/credentials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"clientId":"test-client","secret":"test-secret",
                                         "environment":"SANDBOX","encryptCsvData":true,"password":"a-secure-password"}
                                        """))
                .andExpect(status().isOk());
        assertThat(service.status().csvDataEncrypted()).isTrue();
        assertThat(service.status().hasEncryptedSecrets()).isTrue();
        assertThat(dataDirectory.resolve("secrets.json")).doesNotExist();
    }
}
