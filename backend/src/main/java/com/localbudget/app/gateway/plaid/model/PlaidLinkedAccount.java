package com.localbudget.app.gateway.plaid.model;

public record PlaidLinkedAccount(
        String accountId, String name, String mask, String type, String subtype) {}
