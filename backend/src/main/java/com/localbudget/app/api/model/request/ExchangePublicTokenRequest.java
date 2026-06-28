package com.localbudget.app.api.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ExchangePublicTokenRequest(
        @NotBlank String publicToken,
        String institutionName,
        @NotEmpty List<@Valid SelectedAccountRequest> selectedAccounts
) {
}
