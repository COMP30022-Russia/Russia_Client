package com.comp30022.team_russia.assist.features.login.services;

import java9.util.concurrent.CompletableFuture;

/**
 * Authentication (Login and Registration) Service
 */
public interface IAuthService {
    CompletableFuture<Boolean> login(String username, String password);
    //CompletableFuture<Boolean> register();
}
