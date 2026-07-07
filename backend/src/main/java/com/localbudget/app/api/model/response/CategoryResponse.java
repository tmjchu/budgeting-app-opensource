package com.localbudget.app.api.model.response;

public record CategoryResponse(
        String categoryId,
        String displayName,
        String color,
        String parentCategoryId,
        boolean archived,
        int sortOrder) {}
