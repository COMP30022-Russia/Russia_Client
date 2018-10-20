package com.comp30022.team_russia.assist.features.video.ui;

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
import com.comp30022.team_russia.assist.databinding.FragmentVideoCallBinding;

import javax.inject.Inject;

/**
 * Fragment for video call.
 */
public class VideoCallFragment extends BaseFragment {

    private VideoCallViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private FragmentVideoCallBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(VideoCallViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_video_call, container,false);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);

        return binding.getRoot();
    }

}
