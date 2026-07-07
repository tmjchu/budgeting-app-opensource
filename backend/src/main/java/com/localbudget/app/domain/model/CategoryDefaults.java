package com.localbudget.app.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CategoryDefaults {

    private static final Instant DEFAULT_TIMESTAMP = Instant.parse("2026-01-01T00:00:00Z");

    private static final List<String> DEFAULT_DISPLAY_NAMES =
            List.of(
                    "Auto & Transport",
                    "Bills & Utilities",
                    "Business",
                    "Cash & Checks",
                    "Charitable Donations",
                    "Credit Card Payment",
                    "Dining & Drinks",
                    "Education",
                    "Entertainment & Rec.",
                    "Family Care",
                    "Fees",
                    "Gifts",
                    "Groceries",
                    "Health & Wellness",
                    "Home & Garden",
                    "Ignore",
                    "Income",
                    "Internal Transfers",
                    "Investment",
                    "Legal",
                    "Loan Payment",
                    "Medical",
                    "Personal Care",
                    "Pets",
                    "Reimbursement",
                    "Savings Transfer",
                    "Shopping",
                    "Software & Tech",
                    "Taxes",
                    "Travel & Vacation",
                    "Uncategorized");

    private CategoryDefaults() {}

    public static List<CategoryDO> defaults() {
        return DEFAULT_DISPLAY_NAMES.stream()
                .map(
                        displayName ->
                                new CategoryDO(
                                        categoryId(displayName),
                                        displayName,
                                        null,
                                        null,
                                        false,
                                        (DEFAULT_DISPLAY_NAMES.indexOf(displayName) + 1) * 10,
                                        DEFAULT_TIMESTAMP,
                                        DEFAULT_TIMESTAMP))
                .toList();
    }

    public static Optional<String> categoryIdForDisplayName(String displayName) {
        return DEFAULT_DISPLAY_NAMES.stream()
                .filter(defaultDisplayName -> defaultDisplayName.equalsIgnoreCase(displayName))
                .findFirst()
                .map(CategoryDefaults::categoryId);
    }

    public static String categoryId(String displayName) {
        String normalized = displayName.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        boolean previousWasSeparator = true;
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (Character.isLetterOrDigit(character)) {
                builder.append(character);
                previousWasSeparator = false;
            } else if (!previousWasSeparator) {
                builder.append('-');
                previousWasSeparator = true;
            }
        }
        int length = builder.length();
        if (length > 0 && builder.charAt(length - 1) == '-') {
            builder.deleteCharAt(length - 1);
        }
        return builder.toString();
    }
}
