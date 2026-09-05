package com.localbudget.app.domain.model;

/** Account data enriched with public institution identity, never connection credentials. */
public record AccountView(AccountDO account, String institutionId, String institutionName) {}
