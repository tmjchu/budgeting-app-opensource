package com.localbudget.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {"budget.plaid.client-id=test-client-id", "budget.plaid.secret=test-secret"})
class BudgetAppApplicationTest {

    @Test
    void contextLoads() {}
}
