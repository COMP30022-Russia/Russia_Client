package com.comp30022.team_russia.assist.features.message.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.os.Handler;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.db.MessageRepository;
import com.comp30022.team_russia.assist.features.message.models.Message;
import com.comp30022.team_russia.assist.features.message.models.MessageListItemData;
import com.comp30022.team_russia.assist.features.message.services.ChatService;
import com.comp30022.team_russia.assist.features.message.ui.MessageListFragment;

import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.NewMessagePushNotification;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import com.google.gson.Gson;
import com.shopify.livedataktx.LiveDataKt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ocpsoft.prettytime.PrettyTime;

/**
 * ViewModel for {@link MessageListFragment}.
 */
public class MessageListViewModel extends BaseViewModel {

    private int associationId = -1; // set to -1 to present invalid state @todo: improve

    public final MediatorLiveData<List<MessageListItemData>> messageList
        = new MediatorLiveData<>();

    public final MutableLiveData<String> composingMessage = new MutableLiveData<>();

    public final LiveData<Boolean> isSendButtonEnabled;

    private final LiveData<Boolean> isComposingMessageValid;

    public final MutableLiveData<Boolean> isSending = new MutableLiveData<>();

    public final MutableLiveData<String> title = new MutableLiveData<>();

    private final AuthService authService;
    private final ChatService chatService;
    private final UserService userService;
    private final PubSubHub pubSubHub;
    private final MessageRepository messageRepo;
    private final ToastService toastService;
    private final Gson gson = new Gson();

    private Disposable notificationSubscription = null;

    private String otherUserRealname = "User";

    @Inject
    public MessageListViewModel(AuthService authService,
                                ChatService chatService,
                                UserService userService,
                                PubSubHub notificationHub,
                                ToastService toastService,
                                MessageRepository messageRepo) {
        this.authService = authService;
        this.chatService = chatService;
        this.userService = userService;
        this.pubSubHub = notificationHub;
        this.toastService = toastService;
        this.messageRepo = messageRepo;

        isComposingMessageValid = LiveDataKt.map(composingMessage, value ->
            value != null && !value.isEmpty());
        isSendButtonEnabled = combineLatest(isComposingMessageValid, isSending,
            (valid, sending) ->
                valid != null && sending != null && valid && !sending
        );

        // initial values
        messageList.postValue(new ArrayList<>());
        composingMessage.postValue("");
        isSending.postValue(false);
        title.postValue("Message");

        this.pubSubHub.configureTopic(PubSubTopics.NEW_MESSAGE, NewMessagePushNotification.class,
            new PayloadToObjectConverter<NewMessagePushNotification>() {
                @Override
                public NewMessagePushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewMessagePushNotification.class);
                }

                @Override
                public String toString(NewMessagePushNotification payload) {
                    // not used. not implemented.
                    return null;
                }
            });
    }

    /**
     * Sets the association ID the current chat.
     *
     * @param associationId The association ID.
     */
    public void setAssociationId(int associationId) {
        this.associationId = associationId;
        // load the User
        title.postValue("Messages");
        userService.getUserFromAssociation(associationId)
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    title.postValue(result.unwrap().getRealname());
                    this.otherUserRealname = result.unwrap().getRealname();
                } else {
                    // retry
                }
            });

        // load message history once
        handler.post(this::loadMessages);

        // listen for push notification about new message arrival.
        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NEW_MESSAGE,
            new SubscriberCallback<NewMessagePushNotification>() {
                @Override
                public void onReceived(NewMessagePushNotification payload) {
                    // only sync messages if the new message is of the same association
                    // @todo: maybe if the new message is for a different chat, also load the
                    // messages
                    //        in the background
                    if (payload.getAssociationId() == MessageListViewModel.this.associationId) {
                        handler.post(MessageListViewModel.this::loadMessages);
                    }
                }
            });
    }

    /**
     * Reload the messages.
     */
    public void loadMessages() {
        if (this.associationId > 0) {
            messageList.addSource(messageRepo.getMessages(this.associationId), newMessages -> {
                int currentUserId = authService.getCurrentUser().getUserId();
                ArrayList<MessageListItemData> result = new ArrayList<>();
                if (newMessages == null) {
                    return;
                }
                for (Message message : newMessages) {
                    result.add(new MessageListItemData(
                        message.getId(),
                        message.getAuthorId() == currentUserId,
                        message.getContent(),
                        new PrettyTime().format(message.getCreatedAt()),
                        otherUserRealname));
                }
                messageList.postValue(result);
            });
        }
    }

    /**
     * Event handler for when user clicks on the Send button.
     */
    public void onSendClicked() {
        isSending.postValue(true);
        chatService.sendChatMessage(this.associationId, this.composingMessage.getValue().trim())
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    composingMessage.postValue("");
                    handler.post(this::loadMessages);
                } else {
                    toastService.toastShort("Message not sent.");
                }
                isSending.postValue(false);
            });
    }

    public void onStartNavigationClicked() {
        Bundle bundle = new Bundle();
        bundle.putInt("assocId", associationId);
        Boolean isAp = authService.getCurrentUser().getUserType() == User.UserType.AP;
        bundle.putBoolean("apInitiated", isAp);

        navigateTo(R.id.action_start_navigation, bundle);
    }

    public void onStartVideoCallClicked() {
        navigateTo(R.id.action_start_video_call);
    }

    //@todo: Remove later: hack for reloading messages

    private Handler handler = new Handler();

    @Override
    protected void onCleared() {
        super.onCleared();
        // always clean up the subscriptions in LiveModels, to prevent leaking.
        if (this.notificationSubscription != null) {
            this.notificationSubscription.dispose();
        }
    }
}