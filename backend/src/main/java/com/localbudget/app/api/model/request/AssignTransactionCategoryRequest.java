package com.localbudget.app.api.model.request;

import jakarta.validation.constraints.NotBlank;

public record AssignTransactionCategoryRequest(@NotBlank String categoryId) {}
