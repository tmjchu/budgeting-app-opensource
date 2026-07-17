package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.SyncResponse;
import com.localbudget.app.converter.SyncRunConverter;
import com.localbudget.app.domain.processor.SyncBankDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncBankDataProcessor syncBankDataProcessor;
    private final SyncRunConverter syncRunConverter;

    @PostMapping
    public SyncResponse sync() {
        log.info("Sync API Invoked");

        SyncResponse response = syncRunConverter.toResponse(syncBankDataProcessor.process());

        log.info("Sync API Completed");
        return response;
    }
}
