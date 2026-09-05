package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.PlaidCredentials;

public interface PlaidCredentialsProvider {

    PlaidCredentials requireCredentials();
}
