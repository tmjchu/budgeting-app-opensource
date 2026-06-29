package com.localbudget.app.converter;

import com.localbudget.app.data.model.PlaidItemCsvRecord;
import com.localbudget.app.domain.model.PlaidItem;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class PlaidItemConverter {

    public PlaidItem fromCsv(PlaidItemCsvRecord plaidItemCsvRecord) {
        return new PlaidItem(
                plaidItemCsvRecord.plaidItemId(),
                plaidItemCsvRecord.accessToken(),
                plaidItemCsvRecord.institutionName(),
                Instant.parse(plaidItemCsvRecord.createdAt()));
    }

    public PlaidItemCsvRecord toCsv(PlaidItem plaidItem) {
        return new PlaidItemCsvRecord(
                plaidItem.plaidItemId(),
                plaidItem.accessToken(),
                plaidItem.institutionName(),
                plaidItem.createdAt().toString());
    }
}
