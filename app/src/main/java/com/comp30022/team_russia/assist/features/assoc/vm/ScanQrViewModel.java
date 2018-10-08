package com.comp30022.team_russia.assist.features.assoc.vm;

import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.assoc.ui.ScanQrFragment;

import javax.inject.Inject;

/**
 * ViewModel for {@link ScanQrFragment}.
 */
public class ScanQrViewModel extends BaseViewModel {

    private final UserService userService;

    public final SingleLiveEvent<Void> navigateBackToHome = new SingleLiveEvent<>();

    public final MutableLiveData<Boolean> isBusy = new MutableLiveData<>();

    public final SingleLiveEvent<String> toastMessage = new SingleLiveEvent<>();

    @Inject
    ScanQrViewModel(UserService userService) {
        this.userService = userService;
        isBusy.postValue(false);
    }

    /**
     * Handle on successful scan.
     */
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
