package com.localbudget.app.api.model.request;

import jakarta.validation.constraints.NotBlank;

public record UnlockRequest(@NotBlank String password) {}
