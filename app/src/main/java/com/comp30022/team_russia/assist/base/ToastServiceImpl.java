package com.comp30022.team_russia.assist.base;

import android.app.Application;
import android.widget.Toast;

import javax.inject.Inject;

/**
 * Default implementation of {@link ToastService}.
 */
public class ToastServiceImpl implements ToastService {

    private final Application app;

    @Inject
    public ToastServiceImpl(Application app) {
        this.app = app;
    }

    @Override
    public void toastShort(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void toastLong(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(app, message, Toast.LENGTH_LONG).show();
        }
    }
}
