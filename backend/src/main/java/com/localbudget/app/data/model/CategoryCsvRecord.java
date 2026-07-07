package com.localbudget.app.data.model;

public record CategoryCsvRecord(
        String categoryId,
        String displayName,
        String color,
        String parentCategoryId,
        String archived,
        String sortOrder,
        String createdAt,
        String updatedAt) {}
