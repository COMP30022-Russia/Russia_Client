package com.comp30022.team_russia.assist.features.login;

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
import com.comp30022.team_russia.assist.databinding.FragmentRegisterFormBinding;


public class RegisterFormFragment extends BaseFragment {

    /**
     * The view model.
     */
    private RegisterFormViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(RegisterFormViewModel.class);

        boolean isAP = getArguments().getBoolean("isAP");
        viewModel.isAP.setValue(isAP);

        FragmentRegisterFormBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_register_form,container,false);
        // Sets our view model as a variable that can be used by the view.
        // This variable name should be the same as in the one in <data> in activity_login.xml
        binding.setViewmodel(viewModel);
        // Allows this Activity to listen for changes in the view model.
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }


}
