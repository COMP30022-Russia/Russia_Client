package com.comp30022.team_russia.assist.features.login.services;

import android.util.Log;

import com.comp30022.team_russia.assist.features.login.models.RegistrationDTO;
import com.comp30022.team_russia.assist.features.login.models.User;

import java.util.Random;

import java9.util.concurrent.CompletableFuture;

public class DummyAuthService implements AuthService {

    @Override
    public CompletableFuture<Boolean> login(String username, String password) {
        return CompletableFuture.supplyAsync(()->{
            Log.println(Log.INFO, "","Username = "+username+" Password = "+password);
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {

            }
            Random rand = new Random();
            return rand.nextInt(2) == 0;
        });
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public CompletableFuture logout() {
        return null;
    }

    @Override
    public String getCurrentUsername() {
        return null;
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> register(RegistrationDTO registrationInfo) {
        return null;
    }

}


