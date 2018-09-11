package com.comp30022.team_russia.assist.features.message;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.databinding.FragmentMessageListBinding;
import com.comp30022.team_russia.assist.features.message.MessageListAdapter;
import com.comp30022.team_russia.assist.features.message.MessageListViewModel;
import com.comp30022.team_russia.assist.features.message.models.Message;

import java.util.List;

public class MessageListFragment extends BaseFragment {

    private MessageListViewModel viewModel;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private List<Message> messageList; //TODO: link up messageList



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // using recycler view to display messages
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        mMessageRecycler = (RecyclerView) view.findViewById(R.id.reyclerViewMessageList);
        mMessageAdapter = new MessageListAdapter(getActivity(), messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));


        // setup view model binding
        viewModel = ViewModelProviders.of(this).get(MessageListViewModel.class);

        FragmentMessageListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_message_list, container,false);

        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();

    }
}

