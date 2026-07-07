package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.domain.model.TransactionDO;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CategoryMappingServiceTest {

    private final CategoryMappingService service = new CategoryMappingService();

    @ParameterizedTest
    @CsvSource({
        "TRANSFER_CREDIT_CARD_PAYMENT,TRANSFER,credit-card-payment",
        "INCOME_WAGES,INCOME,income",
        "TRANSFER_REIMBURSEMENT,TRANSFER,reimbursement",
        "TRANSFER_SAVINGS,TRANSFER,savings-transfer",
        "TRANSFER_OUT,TRANSFER,internal-transfers",
        "BANK_FEES_MONTHLY,BANK_FEES,fees",
        "LOAN_PAYMENTS_CAR_LOAN,LOAN_PAYMENTS,loan-payment",
        "TAX_PAYMENT,TAX,taxes",
        "INVESTMENT_BUY,INVESTMENT,investment",
        "FOOD_AND_DRINK_GROCERIES,FOOD_AND_DRINK,groceries",
        "FOOD_AND_DRINK_COFFEE,FOOD_AND_DRINK,dining-drinks",
        "RENT_AND_UTILITIES_GAS_AND_ELECTRICITY,RENT_AND_UTILITIES,bills-utilities",
        "TRANSPORTATION_GAS,TRANSPORTATION,auto-transport",
        "TRAVEL_FLIGHTS,TRAVEL,travel-vacation",
        "MEDICAL_PHARMACIES_AND_SUPPLEMENTS,MEDICAL,medical",
        "GENERAL_SERVICES_GYMS_AND_FITNESS_CENTERS,GENERAL_SERVICES,health-wellness",
        "ENTERTAINMENT_SPORTING_EVENTS,ENTERTAINMENT,entertainment-rec",
        "GENERAL_SERVICES_EDUCATION,GENERAL_SERVICES,education",
        "GENERAL_SERVICES_DONATIONS,GENERAL_SERVICES,charitable-donations",
        "GENERAL_MERCHANDISE_GIFTS,GENERAL_MERCHANDISE,gifts",
        "GENERAL_MERCHANDISE_PET_SUPPLIES,GENERAL_MERCHANDISE,pets",
        "PERSONAL_CARE_HAIR_AND_BEAUTY,PERSONAL_CARE,personal-care",
        "LEGAL_SERVICES,GENERAL_SERVICES,legal",
        "BUSINESS_SERVICES,GENERAL_SERVICES,business",
        "SOFTWARE_AND_TECH,GENERAL_SERVICES,software-tech",
        "HOME_IMPROVEMENT,GENERAL_MERCHANDISE,home-garden",
        "CASH_CHECK,TRANSFER,cash-checks",
        "GENERAL_MERCHANDISE_SUPERSTORES,GENERAL_MERCHANDISE,shopping",
        "UNKNOWN,UNKNOWN,uncategorized"
    })
    void defaultCategoryIdMapsPlaidCategories(
            String detailedCategory, String primaryCategory, String expectedCategoryId) {
        assertThat(service.defaultCategoryId(transaction(primaryCategory, detailedCategory)))
                .isEqualTo(expectedCategoryId);
    }

    private TransactionDO transaction(String primaryCategory, String detailedCategory) {
        return new TransactionDO(
                "txn-1",
                "item-1",
                "acc-1",
                "Checking",
                LocalDate.parse("2026-06-01"),
                "Name",
                "Merchant",
                new BigDecimal("12.00"),
                primaryCategory,
                detailedCategory,
                null,
                null,
                false,
                false,
                "in store");
    }
}
