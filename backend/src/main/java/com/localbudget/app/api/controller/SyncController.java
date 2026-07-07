package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.SyncResponse;
import com.localbudget.app.converter.SyncRunConverter;
import com.localbudget.app.domain.processor.SyncBankDataProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncBankDataProcessor syncBankDataProcessor;
    private final SyncRunConverter syncRunConverter;

    @PostMapping
    public SyncResponse sync() {
        return syncRunConverter.toResponse(syncBankDataProcessor.handle());
    }
}
