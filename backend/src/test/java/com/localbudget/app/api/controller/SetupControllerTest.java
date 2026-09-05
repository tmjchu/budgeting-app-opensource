package com.localbudget.app.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.localbudget.app.api.model.request.ConfigureCredentialsRequest;
import com.localbudget.app.api.model.request.PlaidCredentialsRequest;
import com.localbudget.app.api.model.request.UnlockRequest;
import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.model.PlaidCredentials;
import com.localbudget.app.domain.model.SetupState;
import com.localbudget.app.domain.model.SetupStatus;
import com.localbudget.app.domain.service.SetupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SetupControllerTest {

    @Mock private SetupService setupService;

    @Test
    void returnsSetupStatusWithoutExposingCredentials() {
        when(setupService.status())
                .thenReturn(new SetupStatus(SetupState.LOCKED, true, false, false));

        var response = new SetupController(setupService).status();

        assertThat(response.state()).isEqualTo("locked");
        assertThat(response.hasEncryptedSecrets()).isTrue();
        assertThat(response.hasEnvironmentCredentials()).isFalse();
    }

    @Test
    void validatesAndConfiguresCredentials() {
        SetupController controller = new SetupController(setupService);
        PlaidCredentials expected =
                new PlaidCredentials("client", "secret", BudgetAppProperties.Environment.SANDBOX);
        when(setupService.status())
                .thenReturn(new SetupStatus(SetupState.READY, true, false, true));

        assertThat(
                        controller
                                .validate(
                                        new PlaidCredentialsRequest(
                                                "client",
                                                "secret",
                                                BudgetAppProperties.Environment.SANDBOX))
                                .message())
                .isEqualTo("Plaid credentials are valid.");
        var response =
                controller.configure(
                        new ConfigureCredentialsRequest(
                                "client",
                                "secret",
                                BudgetAppProperties.Environment.SANDBOX,
                                "a-secure-password",
                                true));

        verify(setupService).validate(expected);
        verify(setupService).configure(eq(expected), any(char[].class), eq(true));
        assertThat(response.state()).isEqualTo("ready");
        assertThat(response.csvEncryptionStatus()).isEqualTo("encrypted");
    }

    @Test
    void unlocksAndLocksSession() {
        SetupController controller = new SetupController(setupService);
        when(setupService.status())
                .thenReturn(
                        new SetupStatus(SetupState.READY, true, false, false),
                        new SetupStatus(SetupState.LOCKED, true, false, false));

        assertThat(controller.unlock(new UnlockRequest("a-secure-password")).state())
                .isEqualTo("ready");
        assertThat(controller.lock().state()).isEqualTo("locked");

        verify(setupService).unlock(any(char[].class));
        verify(setupService).lock();
    }
}
