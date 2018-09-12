package com.comp30022.team_russia.assist.features.assoc;

import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.BaseViewModel;

public class GenerateQRViewModel extends BaseViewModel {
    // Stores the token which is displayed as a QR code
    final MutableLiveData<String> token = new MutableLiveData<>();

    GenerateQRViewModel() {
        // Initial request
        token.setValue("initial");
    }
}
