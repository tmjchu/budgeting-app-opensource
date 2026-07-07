package com.localbudget.app.domain.model.command;

public record AssignTransactionCategoryCommand(String transactionId, String categoryId) {}
