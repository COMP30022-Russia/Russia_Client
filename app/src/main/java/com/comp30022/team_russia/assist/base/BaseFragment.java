package com.comp30022.team_russia.assist.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Objects;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
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
        vm.navigateAction.observe(this, eventArgs -> {
            assert eventArgs != null;
            Integer actionId = eventArgs.getActionId();
            Bundle bundle = eventArgs.getBundle();
            NavController navController = Navigation.findNavController(Objects.requireNonNull(getView()));

            NavOptions.Builder builder = new NavOptions.Builder();

            if (eventArgs.getShouldClearStack()) {
                builder.setPopUpTo(navController.getGraph().getId(),true);
            }
            navController.navigate(actionId, bundle, builder.build());

        });
    }
}
