package com.comp30022.team_russia.assist.features.chat.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.databinding.ItemImageReceivedBinding;
import com.comp30022.team_russia.assist.databinding.ItemImageSentBinding;
import com.comp30022.team_russia.assist.databinding.ItemMessageReceivedBinding;
import com.comp30022.team_russia.assist.databinding.ItemMessageSentBinding;
import com.comp30022.team_russia.assist.features.chat.models.MessageListItemData;
import com.comp30022.team_russia.assist.features.chat.services.ChatService;
import com.comp30022.team_russia.assist.features.chat.vm.MessageListViewModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for chat message list.
 */
public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<MessageListItemData> messageList = new ArrayList<>();

    private final MessageListViewModel vm;

    private final ChatService chatService;

    private final MessageListFragment messageListFragment;

    private final Context context;

    private static final int VIEW_TYPE_PICTURE_SENT = 3;
    private static final int VIEW_TYPE_PICTURE_RECEIVED = 4;

    /**
     * Constructor.
     */
    public MessageListAdapter(MessageListViewModel vm, ChatService chatService,
                              MessageListFragment messageListFragment, Context context) {
        this.vm = vm;
        this.chatService = chatService;
        this.messageListFragment = messageListFragment;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageListItemData message = messageList.get(position);

        if (message.isSentByMe) {

            if (message.pictureId > 0) {
                return VIEW_TYPE_PICTURE_SENT;
            } else {
                return VIEW_TYPE_MESSAGE_SENT;
            }


        } else {

            if (message.pictureId > 0) {
                return VIEW_TYPE_PICTURE_RECEIVED;
            } else {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }

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

        } else if (viewType == VIEW_TYPE_PICTURE_SENT) {
            ItemImageSentBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.item_image_sent,
                parent,
                false);
            return new SentPictureHolder(binding);

        } else if (viewType == VIEW_TYPE_PICTURE_RECEIVED) {
            ItemImageReceivedBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.item_image_received,
                parent,
                false);
            return new ReceivedPictureHolder(binding);
        }

        throw new IllegalArgumentException("Unrecognised viewType.");
    }

    /**
     * Updates the message list, replacing with new messages.
     * @param messages The new messages.
     */
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
            ((ReceivedMessageHolder) holder).binding.setViewmodel(vm);
            ((ReceivedMessageHolder) holder).binding.setLifecycleOwner(messageListFragment);

            break;


        case VIEW_TYPE_PICTURE_SENT:
            final ItemImageSentBinding itemImageSentBinding = ((SentPictureHolder) holder).binding;

            itemImageSentBinding.imageMessageBody.setOnClickListener(null);

            itemImageSentBinding.setData(message);

            chatService.getImage(vm.associationId, message.pictureId).thenAccept(result -> {
                if (result.isSuccessful()) {

                    Bitmap imageBitmap = result.unwrap().getPicture();

                    itemImageSentBinding.imageMessageBody.setImageBitmap(imageBitmap);


                    itemImageSentBinding.imageMessageBody.setOnClickListener(v -> {
                        //Convert to byte array
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        Intent intent = new Intent(context, MessageImageFullScreenViewer.class);
                        intent.putExtra("image", byteArray);
                        context.startActivity(intent);

                    });
                }
            });

            break;

        case VIEW_TYPE_PICTURE_RECEIVED:

            final ItemImageReceivedBinding itemImageReceivedBinding =
                ((ReceivedPictureHolder) holder).binding;

            itemImageReceivedBinding.imageMessageBody.setOnClickListener(null);

            itemImageReceivedBinding.setData(message);
            itemImageReceivedBinding.setViewmodel(vm);
            itemImageReceivedBinding.setLifecycleOwner(messageListFragment);


            chatService.getImage(vm.associationId, message.pictureId).thenAccept(result -> {
                if (result.isSuccessful()) {


                    Bitmap imageBitmap = result.unwrap().getPicture();

                    itemImageReceivedBinding.imageMessageBody.setImageBitmap(imageBitmap);


                    itemImageReceivedBinding.imageMessageBody.setOnClickListener(v -> {
                        //Convert to byte array
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        Intent intent = new Intent(context, MessageImageFullScreenViewer.class);
                        intent.putExtra("image", byteArray);
                        context.startActivity(intent);

                    });


                }
            });

            break;

        default:
            break;
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        final ItemMessageReceivedBinding binding;

        ReceivedMessageHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        final ItemMessageSentBinding binding;

        SentMessageHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }




    private class ReceivedPictureHolder extends RecyclerView.ViewHolder {

        final ItemImageReceivedBinding binding;

        ReceivedPictureHolder(ItemImageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // @todo display profile image
       /* Insert the profile image from the URL into the ImageView.
        Utils.displayRoundImageFromUrl(mContext,
        message.getSender().getProfileUrl(), otherUserImage);

        Utils.displayRoundImageFromUrl(mContext, message.getSender()
        .getProfileUrl(), photoAlbumButton);
        */
    }

    private class SentPictureHolder extends RecyclerView.ViewHolder {
        final ItemImageSentBinding binding;

        SentPictureHolder(ItemImageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}