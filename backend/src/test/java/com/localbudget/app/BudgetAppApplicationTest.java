package com.localbudget.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.domain.model.SetupState;
import com.localbudget.app.domain.service.SetupService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class BudgetAppApplicationTest {

    @TempDir static Path dataDirectory;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("budget.data-directory", dataDirectory::toString);
        registry.add("budget.plaid.client-id", () -> "");
        registry.add("budget.plaid.secret", () -> "");
    }

    @Autowired private SetupService setupService;

    @Test
    void contextLoadsWithoutPlaidCredentials() {
        assertThat(setupService.status().state()).isEqualTo(SetupState.NEEDS_SETUP);
    }
}
