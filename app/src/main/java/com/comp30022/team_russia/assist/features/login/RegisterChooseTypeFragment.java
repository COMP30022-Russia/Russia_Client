package com.comp30022.team_russia.assist.features.login;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.databinding.FragmentRegisterChooseTypeBinding;

/**
 * Fragment for choosing the type of the user to register.
 */
public class RegisterChooseTypeFragment extends BaseFragment {

    private RegisterChooseTypeViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(RegisterChooseTypeViewModel.class);

        FragmentRegisterChooseTypeBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_register_choose_type,container,false);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }

}
