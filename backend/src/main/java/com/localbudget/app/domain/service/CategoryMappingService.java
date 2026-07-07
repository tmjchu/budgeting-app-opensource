package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.TransactionDO;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CategoryMappingService {

    public String defaultCategoryId(TransactionDO transaction) {
        String primary = normalize(transaction.primaryCategory());
        String detailed = normalize(transaction.detailedCategory());
        String combined = detailed + " " + primary;

        if (contains(combined, "CREDIT_CARD")) {
            return "credit-card-payment";
        }
        if (contains(combined, "PAYROLL", "INCOME", "WAGES")) {
            return "income";
        }
        if (contains(combined, "REIMBURSE")) {
            return "reimbursement";
        }
        if (contains(combined, "SAVINGS")) {
            return "savings-transfer";
        }
        if (contains(combined, "CASH", "CHECK")) {
            return "cash-checks";
        }
        if (contains(combined, "TRANSFER")) {
            return "internal-transfers";
        }
        if (contains(combined, "BANK_FEES", "FEE")) {
            return "fees";
        }
        if (contains(combined, "LOAN")) {
            return "loan-payment";
        }
        if (contains(combined, "TAX")) {
            return "taxes";
        }
        if (contains(combined, "INVEST", "BROKERAGE", "SECURITIES")) {
            return "investment";
        }
        if (contains(combined, "GROCERY", "GROCERIES", "SUPERMARKET")) {
            return "groceries";
        }
        if (contains(
                combined, "RESTAURANT", "COFFEE", "FAST_FOOD", "BAR", "FOOD_AND_DRINK")) {
            return "dining-drinks";
        }
        if (contains(combined, "UTILITY", "UTILITIES", "RENT_AND_UTILITIES")) {
            return "bills-utilities";
        }
        if (contains(
                combined,
                "AUTOMOTIVE",
                "TRANSPORTATION",
                "PARKING",
                "GAS",
                "PUBLIC_TRANSIT",
                "TAXI")) {
            return "auto-transport";
        }
        if (contains(combined, "TRAVEL", "HOTEL", "AIRLINE")) {
            return "travel-vacation";
        }
        if (contains(combined, "MEDICAL", "DOCTOR", "DENTIST", "PHARMACY", "HOSPITAL")) {
            return "medical";
        }
        if (contains(combined, "GYM", "HEALTH", "WELLNESS")) {
            return "health-wellness";
        }
        if (contains(combined, "ENTERTAINMENT", "RECREATION", "SPORT")) {
            return "entertainment-rec";
        }
        if (contains(combined, "EDUCATION", "SCHOOL", "TUITION")) {
            return "education";
        }
        if (contains(combined, "DONATION", "CHARITY", "NON_PROFIT")) {
            return "charitable-donations";
        }
        if (contains(combined, "GIFT")) {
            return "gifts";
        }
        if (contains(combined, "PET")) {
            return "pets";
        }
        if (contains(combined, "PERSONAL_CARE")) {
            return "personal-care";
        }
        if (contains(combined, "LEGAL", "LAWYER")) {
            return "legal";
        }
        if (contains(combined, "BUSINESS")) {
            return "business";
        }
        if (contains(combined, "SOFTWARE", "TECH")) {
            return "software-tech";
        }
        if (contains(combined, "HOME", "GARDEN")) {
            return "home-garden";
        }
        if (contains(combined, "GENERAL_MERCHANDISE", "SHOPPING")) {
            return "shopping";
        }
        return "uncategorized";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private static boolean contains(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
