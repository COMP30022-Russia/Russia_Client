package com.comp30022.team_russia.assist.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import java.util.Objects;

/**
 * Base class for our fragments, with helper methods.
 */
public abstract class BaseFragment extends Fragment {
    /**
     * Wires up the navigateAction property to NavController.
     * Keeps ViewModel decoupled from UI.
     * @param vm The ViewModel.
     */
    protected void setupNavigationHandler(BaseViewModel vm) {
        vm.navigateAction.observe(this, eventArgs -> {
            assert eventArgs != null;
            Integer actionId = eventArgs.getActionId();
            Bundle bundle = eventArgs.getBundle();
            NavController navController = Navigation.findNavController(
                Objects.requireNonNull(getView()));

            if (eventArgs.getShouldClearStack()) {
                // Clears navigation stack (within the same Activity).
                // This is useful for cases where user clicks on a button that sends them on a
                // non-return journey.
                // Warning: because of the freshly built NavOptions here, it does not
                // respect the attributes set in the xml files (e.g. animations).
                NavOptions.Builder builder = new NavOptions.Builder();
                builder.setPopUpTo(navController.getGraph().getId(),true);
                navController.navigate(actionId, bundle, builder.build());
            }
            navController.navigate(actionId, bundle);
        });
    }
}
