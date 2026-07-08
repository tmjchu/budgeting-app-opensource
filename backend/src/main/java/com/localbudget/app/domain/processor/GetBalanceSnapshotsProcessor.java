package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.service.BalanceSnapshotService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBalanceSnapshotsProcessor {

    private final BalanceSnapshotService balanceSnapshotService;

    public List<BalanceSnapshot> process() {
        return balanceSnapshotService.findAll();
    }
}
