package com.comp30022.team_russia.assist.features.assoc.ui;
import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;

import javax.inject.Inject;

public class ScanQRViewModel extends BaseViewModel {

    private final UserService userService;

    public final SingleLiveEvent<Void> navigateBackToHome = new SingleLiveEvent<>();

    public final MutableLiveData<Boolean> isBusy = new MutableLiveData<>();

    public final SingleLiveEvent<String> toastMessage = new SingleLiveEvent<>();

    @Inject
    ScanQRViewModel(UserService userService) {
        this.userService = userService;
        isBusy.postValue(false);
    }

    public void onScanResult(String token) {
        isBusy.postValue(true);
        userService.associateWith(token).thenAcceptAsync(result -> {
            if (result.isSuccessful()) {
                navigateBackToHome.postValue(null);
                toastMessage.postValue("Success");
            } else {
                toastMessage.postValue("Error");
            }
            isBusy.postValue(false);
        });
    }

}
