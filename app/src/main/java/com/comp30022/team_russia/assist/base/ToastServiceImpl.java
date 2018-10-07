package com.comp30022.team_russia.assist.base;

import android.app.Application;
import android.os.Build;
import android.widget.Toast;

import javax.inject.Inject;

/**
 * Default implementation of {@link ToastService}.
 */
public class ToastServiceImpl implements ToastService {

    private final Application app;

    // Use a single Toast instance so that new toast messages override old.
    // Appears to be broken under Android P.
    private final Toast toast;

    @Inject
    public ToastServiceImpl(Application app) {
        this.app = app;
        toast = Toast.makeText(app, "", Toast.LENGTH_SHORT);
    }

    @Override
    public void toastShort(String message) {
        if (message != null && !message.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
            } else {
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setText(message);
                toast.show();
            }
        }
    }

    @Override
    public void toastLong(String message) {
        if (message != null && !message.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Toast.makeText(app, message, Toast.LENGTH_LONG).show();
            } else {
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setText(message);
                toast.show();
            }
        }
    }
}
