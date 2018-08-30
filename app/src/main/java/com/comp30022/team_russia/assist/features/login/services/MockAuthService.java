package com.comp30022.team_russia.assist.features.login.services;

import android.util.Log;

import java.util.Random;

import java9.util.concurrent.CompletableFuture;

public class MockAuthService implements IAuthService {

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
}
