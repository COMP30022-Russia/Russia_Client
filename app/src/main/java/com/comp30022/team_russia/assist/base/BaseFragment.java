package com.comp30022.team_russia.assist.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import androidx.navigation.Navigation;

/**
 * Base class for our fragments, with helper methods.
 */
public abstract class BaseFragment extends Fragment {
    /**
     * Wires up the navigateAction property to NavController.
     * Keeps ViewModel decoupled from UI.
     * @param vm
     */
    protected void setupNavigationHandler(BaseViewModel vm) {
        vm.navigateAction.observe(this, args -> {
            Integer actionId = args.first;
            Bundle bundle = args.second;
            if (actionId != null) {
                Navigation.findNavController(getView()).navigate(actionId, bundle);
            }
        });
    }
}
