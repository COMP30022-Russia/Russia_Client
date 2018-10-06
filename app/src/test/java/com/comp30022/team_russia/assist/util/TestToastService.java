package com.comp30022.team_russia.assist.util;

import com.comp30022.team_russia.assist.base.ToastService;

public class TestToastService implements ToastService {
    @Override
    public void toastShort(String message) {
        System.out.println(String.format("Toast Message (Short): %s", message));
    }

    @Override
    public void toastLong(String message) {
        System.out.println(String.format("Toast Message (Long): %s", message));
    }
}
