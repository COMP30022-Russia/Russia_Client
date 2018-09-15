package com.comp30022.team_russia.assist.features.assoc.ui;

import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;

import javax.inject.Inject;

import java9.util.function.Consumer;

public class GenerateQRViewModel extends BaseViewModel {

    /**
     * The association token which is displayed as a QR code.
     */
    public final MutableLiveData<String> token = new MutableLiveData<>();

    public final MutableLiveData<Boolean> hasError = new MutableLiveData<>();

    private UserService userService;

    @Inject
    GenerateQRViewModel(UserService userService) {
        this.userService = userService;

        hasError.postValue(true);

        userService.getAssociateToken()
            .thenAcceptAsync(result -> {
               if (result.isSuccessful()) {
                   token.postValue(result.unwrap());
                   hasError.postValue(false);
               } else {
                   hasError.postValue(true);
               }
            });
    }


}
