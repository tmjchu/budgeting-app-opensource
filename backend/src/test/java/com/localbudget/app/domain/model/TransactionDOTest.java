package com.localbudget.app.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionDOTest {

    @Test
    void effectiveCategoryUsesLegacyLocalThenPlaidThenUncategorized() {
        assertThat(transaction("local", "Legacy", null, "FOOD").effectiveCategory())
                .isEqualTo("Legacy");
        assertThat(transaction("plaid", null, null, "FOOD").effectiveCategory())
                .isEqualTo("FOOD");
        assertThat(transaction("unknown", null, null, null).effectiveCategory())
                .isEqualTo("Uncategorized");
    }

    @Test
    void withLocalCategoryIdStoresStableIdAndClearsLegacyLocalCategory() {
        TransactionDO updated = transaction("txn-1", "Legacy", null, "FOOD").withLocalCategoryId("groceries");

        assertThat(updated.localCategoryId()).isEqualTo("groceries");
        assertThat(updated.localCategory()).isNull();
        assertThat(updated.primaryCategory()).isEqualTo("FOOD");
    }

    @Test
    void withLocalCategoryIdIfUnassignedPreservesExistingAssignments() {
        TransactionDO withId = transaction("with-id", null, "groceries", "FOOD");
        TransactionDO withLegacy = transaction("with-legacy", "Legacy", null, "FOOD");
        TransactionDO unassigned = transaction("unassigned", null, null, "FOOD");

        assertThat(withId.withLocalCategoryIdIfUnassigned("shopping")).isSameAs(withId);
        assertThat(withLegacy.withLocalCategoryIdIfUnassigned("shopping")).isSameAs(withLegacy);
        assertThat(unassigned.withLocalCategoryIdIfUnassigned("shopping").localCategoryId())
                .isEqualTo("shopping");
    }

    private TransactionDO transaction(
            String id, String legacyLocalCategory, String localCategoryId, String primaryCategory) {
        return new TransactionDO(
                id,
                TestFixtures.plaidItem().plaidItemId(),
                TestFixtures.checkingAccount().accountId(),
                TestFixtures.checkingAccount().name(),
                LocalDate.parse("2026-06-01"),
                "Name",
                "Merchant",
                new BigDecimal("12.00"),
                primaryCategory,
                primaryCategory == null ? null : primaryCategory + "_DETAIL",
                legacyLocalCategory,
                localCategoryId,
                false,
                false,
                "in store");
    }
}
