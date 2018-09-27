package com.comp30022.team_russia.assist.features.message.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.db.MessageRepository;
import com.comp30022.team_russia.assist.features.message.models.Message;
import com.comp30022.team_russia.assist.features.message.models.MessageListItemData;
import com.comp30022.team_russia.assist.features.message.services.ChatService;
import com.comp30022.team_russia.assist.features.message.ui.MessageListFragment;

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

    public final SingleLiveEvent<String> toastMessage = new SingleLiveEvent<>();

    public final MutableLiveData<String> title = new MutableLiveData<>();

    private final AuthService authService;
    private final ChatService chatService;
    private final UserService userService;
    private final MessageRepository messageRepo;

    private String otherUserRealname = "User";

    @Inject
    public MessageListViewModel(AuthService authService,
                                ChatService chatService,
                                UserService userService,
                                MessageRepository messageRepo) {
        this.authService = authService;
        this.chatService = chatService;
        this.userService = userService;
        this.messageRepo = messageRepo;

        isComposingMessageValid = LiveDataKt.map(composingMessage, value ->
            value != null && !value.isEmpty());
        isSendButtonEnabled = combineLatest(isComposingMessageValid, isSending,
            (valid, sending) ->
                valid != null && sending != null && valid && !sending
        );
        messageList.postValue(new ArrayList<>());
        composingMessage.postValue("");
        isSending.postValue(false);
        title.postValue("Message");
    }

    /**
     * Sets the association ID the current chat.
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

        handler.post(new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, 2000);
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
                for (Message message: newMessages) {
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
        chatService.sendChatMessage(this.associationId, this.composingMessage.getValue())
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    composingMessage.postValue("");
                } else {
                    toastMessage.postValue("Message not sent");
                }
                isSending.postValue(false);
            });
    }

    //@todo: Remove later: hack for reloading messages

    private Handler handler = new Handler();
    //private Runnable runnable;

}
