package com.comp30022.team_russia.assist.features.login.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentRegisterFormBinding;
import com.comp30022.team_russia.assist.features.login.vm.RegisterFormViewModel;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Registration Form (for both AP and Carer).
 */
public class RegisterFormFragment extends BaseFragment implements Injectable {

    /**
     * The view model.
     */
    private RegisterFormViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(RegisterFormViewModel.class);

        boolean isAp = getArguments().getBoolean("isAp");
        viewModel.isAp.setValue(isAp);

        // Change toolbar title depending on the selected user type
        ((TitleChangable) Objects.requireNonNull(getActivity())).updateTitle(isAp
                ? getResources().getString(R.string.register_ap)
                : getResources().getString(R.string.register_carer));

        FragmentRegisterFormBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_register_form, container, false);
        // Sets our view model as a variable that can be used by the view.
        // This variable name should be the same as in the one in <data> in activity_login.xml
        binding.setViewModel(viewModel);
        // Allows this Activity to listen for changes in the view model.
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }
}
