package com.comp30022.team_russia.assist.features.login.vm;

import android.os.Bundle;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;

import javax.inject.Inject;

/**
 * ViewModel for RegisterChooseType screen.
 */
public class RegisterChooseTypeViewModel extends BaseViewModel {
    /**
     * Constructor.
     */
    @Inject
    public RegisterChooseTypeViewModel() {
    }

    /**
     * Handler for when the user type is chosen.
     * @param isAp Whether the user is an AP (true) or Carer (false).
     */
    public void onUserTypeSelected(boolean isAp) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("apInitiated", isAp);
        navigateTo(R.id.action_register_typechosen, bundle);
    }
}
