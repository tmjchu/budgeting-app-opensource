package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.service.BalanceSnapshotService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetBalanceSnapshotsProcessor {

    private final BalanceSnapshotService balanceSnapshotService;

    public GetBalanceSnapshotsProcessor(BalanceSnapshotService balanceSnapshotService) {
        this.balanceSnapshotService = balanceSnapshotService;
    }

    public List<BalanceSnapshot> handle() {
        return balanceSnapshotService.findAll();
    }
}
