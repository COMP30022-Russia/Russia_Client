package com.comp30022.team_russia.assist.features.message.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentMessageListBinding;
import com.comp30022.team_russia.assist.features.message.vm.MessageListViewModel;

import javax.inject.Inject;

/**
 * Chat History screen.
 */
public class MessageListFragment extends BaseFragment implements Injectable {

    private MessageListViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private FragmentMessageListBinding binding;

    private MessageListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MessageListViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_message_list, container,false);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.title.observe(this, title -> {
            ((TitleChangable) getActivity()).updateTitle(title);
        });

        // using recycler view to display messages
        adapter = new MessageListAdapter();
        configureRecyclerView();
        setupNavigationHandler(viewModel);
        subscribeToListChange();

        // parse input arguments
        int associationId = getArguments().getInt("associationId");
        viewModel.setAssociationId(associationId);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hide keyboard when scrolling on recycler view
        view.findViewById(R.id.reyclerViewMessageList).setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

            EditText editText = view.findViewById(R.id.editMessageField);
            imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
            return false;
        });

        //todo change send button color when disabled
    }

    private void configureRecyclerView() {
        RecyclerView recyclerView = binding.reyclerViewMessageList;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void subscribeToListChange() {
        viewModel.messageList.observe(this, newMessageList -> {
            if (newMessageList != null) {
                adapter.setMessageList(newMessageList);
            }
            binding.executePendingBindings();
            binding.reyclerViewMessageList.scrollToPosition(adapter.getItemCount() - 1);
        });
    }
}

