package com.localbudget.app.domain.model;

import java.time.Instant;

public record CategoryDO(
        String categoryId,
        String displayName,
        String color,
        String parentCategoryId,
        boolean archived,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt) {}
