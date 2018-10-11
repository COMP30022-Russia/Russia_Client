package com.comp30022.team_russia.assist.features.message.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

        getActivity().getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MessageListViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_message_list, container, false);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.reyclerViewMessageList;

        // hide keyboard when scrolling on recycler view
        recyclerView.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

            EditText editText = binding.editMessageField;
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            return false;
        });

        // recycler view to show last message
        recyclerView.addOnLayoutChangeListener(
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (recyclerView.getAdapter().getItemCount() < 1) {
                    return;
                }
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(() -> {
                        int bottomPosition = recyclerView.getAdapter().getItemCount() - 1;
                        // @todo: there appears to be a bug here, causing the RecylcerView to
                        // scroll to an invalid position, and leading to a crash.
                        // Suppressing the exception for now.
                        try {
                            recyclerView.smoothScrollToPosition(bottomPosition);
                        } catch (Exception e) {
                            Log.e("MessageListFragment", "error scrolling recyclerview");
                            e.printStackTrace();
                        }
                    }, 100);
                }
            });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                try {
                    int lastCompletelyVisibleItemPosition =
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                            .findLastVisibleItemPosition();
                    viewModel.onScrolled(lastCompletelyVisibleItemPosition);
                } catch (Exception e) {
                    // do nothing
                }
            }
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