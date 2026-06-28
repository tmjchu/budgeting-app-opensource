package com.localbudget.app.domain.model.result;

public record TransactionMergeResult(
        int added,
        int updated,
        int unchanged
) {
}
