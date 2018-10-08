package com.comp30022.team_russia.assist.features.nav.ui;

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
import com.comp30022.team_russia.assist.databinding.FragmentNavSessionRequestBinding;
import com.comp30022.team_russia.assist.features.nav.vm.NavigationNotificationViewModel;

import javax.inject.Inject;

/**
 * Fragment that ask the user to join a nav session.
 */
public class NavigationNotificationFragment extends BaseFragment {

    private NavigationNotificationViewModel viewModel;

    private FragmentNavSessionRequestBinding binding;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(NavigationNotificationViewModel.class);


        viewModel.bundleMutableLiveData.setValue(getArguments());

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_nav_session_request, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        setupNavigationHandler(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.declineButton.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
    }

}