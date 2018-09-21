package com.comp30022.team_russia.assist.features.login.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentRegisterChooseTypeBinding;

import javax.inject.Inject;

/**
 * Fragment for choosing the type of the user to register.
 */
public class RegisterChooseTypeFragment extends BaseFragment implements Injectable {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        RegisterChooseTypeViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(RegisterChooseTypeViewModel.class);
        FragmentRegisterChooseTypeBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_register_choose_type, container, false);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }
}
