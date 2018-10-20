package com.comp30022.team_russia.assist.features.assoc.vm;

import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.assoc.ui.GenerateQrFragment;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * ViewModel for {@link GenerateQrFragment}.
 */
public class GenerateQrViewModel extends BaseViewModel {

    /**
     * The association token which is displayed as a QR code.
     */
    public final MutableLiveData<String> token = new MutableLiveData<>();

    public final MutableLiveData<Boolean> hasError = new MutableLiveData<>();

    /**
     * QR Generator View Model Constructor.
     * @param userService The user service.
     * @param executorService The executor service.
     */
    @Inject
    public GenerateQrViewModel(UserService userService, ExecutorService executorService) {
        // Initially, QR code is not loaded, so should be in error state
        hasError.postValue(true);

        userService.getAssociateToken()
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    token.postValue(result.unwrap());
                    hasError.postValue(false);
                } else {
                    hasError.postValue(true);
                }
            }, executorService);
    }
}
