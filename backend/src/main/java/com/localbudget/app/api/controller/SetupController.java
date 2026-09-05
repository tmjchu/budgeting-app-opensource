package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.request.ConfigureCredentialsRequest;
import com.localbudget.app.api.model.request.PlaidCredentialsRequest;
import com.localbudget.app.api.model.request.UnlockRequest;
import com.localbudget.app.api.model.response.OperationResponse;
import com.localbudget.app.api.model.response.SetupStatusResponse;
import com.localbudget.app.domain.model.PlaidCredentials;
import com.localbudget.app.domain.service.SetupService;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @GetMapping("/status")
    public SetupStatusResponse status() {
        return SetupStatusResponse.from(setupService.status());
    }

    @PostMapping("/validate")
    public OperationResponse validate(@Valid @RequestBody PlaidCredentialsRequest request) {
        setupService.validate(toCredentials(request));
        return new OperationResponse("Plaid credentials are valid.");
    }

    @PostMapping("/credentials")
    public SetupStatusResponse configure(@Valid @RequestBody ConfigureCredentialsRequest request) {
        char[] password =
                request.password() == null ? new char[0] : request.password().toCharArray();
        try {
            setupService.configure(
                    new PlaidCredentials(
                            request.clientId(), request.secret(), request.environment()),
                    password,
                    request.encryptCsvData());
            return SetupStatusResponse.from(setupService.status());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @PostMapping("/unlock")
    public SetupStatusResponse unlock(@Valid @RequestBody UnlockRequest request) {
        char[] password = request.password().toCharArray();
        try {
            setupService.unlock(password);
            return SetupStatusResponse.from(setupService.status());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @PostMapping("/lock")
    public SetupStatusResponse lock() {
        setupService.lock();
        return SetupStatusResponse.from(setupService.status());
    }

    private PlaidCredentials toCredentials(PlaidCredentialsRequest request) {
        return new PlaidCredentials(request.clientId(), request.secret(), request.environment());
    }
}
