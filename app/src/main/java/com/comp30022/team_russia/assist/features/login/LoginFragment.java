package com.comp30022.team_russia.assist.features.login;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.databinding.FragmentLoginBinding;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class LoginFragment extends BaseFragment {

    /**
     * The view model.
     */
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        FragmentLoginBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_login,container,false);
        // Sets our view model as a variable that can be used by the view.
        // This variable name should be the same as in the one in <data> in activity_login.xml
        binding.setViewmodel(viewModel);
        // Allows this Activity to listen for changes in the view model.
        binding.setLifecycleOwner(this);

        viewModel.toastMessage.observe(this, message -> {
            if (!message.isEmpty()) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }


}
