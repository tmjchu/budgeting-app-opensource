package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.BalanceSnapshotResponse;
import com.localbudget.app.converter.BalanceSnapshotConverter;
import com.localbudget.app.domain.handler.GetBalanceSnapshotsHandler;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {

    private final GetBalanceSnapshotsHandler getBalanceSnapshotsHandler;
    private final BalanceSnapshotConverter balanceSnapshotConverter;

    public BalanceController(
            GetBalanceSnapshotsHandler getBalanceSnapshotsHandler,
            BalanceSnapshotConverter balanceSnapshotConverter) {
        this.getBalanceSnapshotsHandler = getBalanceSnapshotsHandler;
        this.balanceSnapshotConverter = balanceSnapshotConverter;
    }

    @GetMapping("/snapshots")
    public List<BalanceSnapshotResponse> getBalanceSnapshots() {
        return getBalanceSnapshotsHandler.handle().stream()
                .map(balanceSnapshotConverter::toResponse)
                .toList();
    }
}
