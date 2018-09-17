package com.comp30022.team_russia.assist.features.message.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.databinding.ItemMessageReceivedBinding;
import com.comp30022.team_russia.assist.databinding.ItemMessageSentBinding;
import com.comp30022.team_russia.assist.features.message.models.MessageListItemData;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<MessageListItemData> messageList = new ArrayList<>();

    public MessageListAdapter() {
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageListItemData message = messageList.get(position);

        if (message.isSentByMe) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            ItemMessageSentBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.item_message_sent,
                parent,
                false);
            return new SentMessageHolder(binding);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            ItemMessageReceivedBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.item_message_received,
                parent,
                false);
            return new ReceivedMessageHolder(binding);
        }

        throw new IllegalArgumentException("Unrecognised viewType.");
    }

    public void setMessageList(final List<MessageListItemData> messages) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return MessageListAdapter.this.messageList.size();
            }

            @Override
            public int getNewListSize() {
                return messages.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemIndex, int newItemIndex) {
                return MessageListAdapter.this.messageList.get(oldItemIndex).id
                    == messages.get(newItemIndex).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemIndex, int newItemIndex) {
                return MessageListAdapter.this.messageList.get(oldItemIndex)
                    .equals(messages.get(newItemIndex));
            }
        });
        this.messageList = messages;
        result.dispatchUpdatesTo(this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageListItemData message = messageList.get(position);

        switch (holder.getItemViewType()) {
        case VIEW_TYPE_MESSAGE_SENT:
            ((SentMessageHolder) holder).binding.setData(message);
            break;
        case VIEW_TYPE_MESSAGE_RECEIVED:
            ((ReceivedMessageHolder) holder).binding.setData(message);
            break;
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        final ItemMessageReceivedBinding binding;
        ReceivedMessageHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // @todo display profile image
       /* Insert the profile image from the URL into the ImageView.
            //        Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        */
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        final ItemMessageSentBinding binding;
        SentMessageHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}