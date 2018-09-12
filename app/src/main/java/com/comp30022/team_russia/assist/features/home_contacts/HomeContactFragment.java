package com.comp30022.team_russia.assist.features.home_contacts;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.databinding.FragmentHomeBinding;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;

import java.util.List;


public class HomeContactFragment extends BaseFragment {

    private HomeContactViewModel viewModel;
    private FragmentHomeBinding binding;
    private ContactListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        /* view model binding */
        viewModel = ViewModelProviders.of(this).get(HomeContactViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home,container,false);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        adapter = new ContactListAdapter(viewModel);
        configureRecyclerView();
        setupNavigationHandler(viewModel);
        subscribeToListChange();
        return binding.getRoot();
    }

    private void configureRecyclerView() {
        RecyclerView recyclerView = binding.contactListRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void subscribeToListChange() {
        viewModel.contactList.observe(this, new Observer<List<ContactListItemData>>() {
            @Override
            public void onChanged(@Nullable List<ContactListItemData> newContactList) {
                if (newContactList != null) {
                    adapter.setContactItemList(newContactList);
                }
                binding.executePendingBindings();
            }
        });
    }
}
