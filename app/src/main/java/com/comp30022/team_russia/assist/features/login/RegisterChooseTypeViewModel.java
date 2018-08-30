package com.comp30022.team_russia.assist.features.login;

import android.os.Bundle;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;

/**
 * ViewModel for RegisterChooseType screen.
 */
public class RegisterChooseTypeViewModel extends BaseViewModel {

    /**
     * Constructor.
     */
    public RegisterChooseTypeViewModel() {
    }

    /**
     * Handler for when the user type is chosen.
     * @param isAP Whether the user is an AP (true) or Carer (false).
     */
    public void onUserTypeSelected(boolean isAP) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAP", isAP);
        navigateTo(R.id.action_register_typechosen, bundle);
    }
}
