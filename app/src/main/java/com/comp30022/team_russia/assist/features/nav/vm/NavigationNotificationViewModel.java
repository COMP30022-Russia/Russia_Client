package com.comp30022.team_russia.assist.features.nav.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.features.nav.ui.NavigationNotificationFragment;

import com.shopify.livedataktx.LiveDataKt;

import javax.inject.Inject;

/**
 * ViewModel for {@link NavigationNotificationFragment}.
 */
public class NavigationNotificationViewModel extends BaseViewModel {
    public final MutableLiveData<Bundle> bundleMutableLiveData = new MutableLiveData<>();
    public final LiveData<String> senderName;

    @Inject
    public NavigationNotificationViewModel() {
        senderName = LiveDataKt.map(bundleMutableLiveData,
            value -> value.getString("senderName"));
    }

    /**
     * Handle when accept button is clicked.
     */
    public void onAcceptButtonClicked() {
        navigateTo(R.id.action_show_nav_screen, bundleMutableLiveData.getValue());
    }
}
