package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.SyncResponse;
import com.localbudget.app.converter.SyncRunConverter;
import com.localbudget.app.domain.handler.SyncBankDataHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncBankDataHandler syncBankDataHandler;
    private final SyncRunConverter syncRunConverter;

    public SyncController(SyncBankDataHandler syncBankDataHandler, SyncRunConverter syncRunConverter) {
        this.syncBankDataHandler = syncBankDataHandler;
        this.syncRunConverter = syncRunConverter;
    }

    @PostMapping
    public SyncResponse sync() {
        return syncRunConverter.toResponse(syncBankDataHandler.handle());
    }
}
