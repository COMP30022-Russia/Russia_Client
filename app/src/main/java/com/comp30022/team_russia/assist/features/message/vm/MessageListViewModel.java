package com.comp30022.team_russia.assist.features.message.vm;

import static com.comp30022.team_russia.assist.features.push.NavSyncTokenDeduplicator.ensureNavSyncTokenValid;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.comp30022.team_russia.assist.ConfigurationManager;
import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.db.MessageRepository;
import com.comp30022.team_russia.assist.features.message.models.Message;
import com.comp30022.team_russia.assist.features.message.models.MessageListItemData;
import com.comp30022.team_russia.assist.features.message.models.PictureDto;
import com.comp30022.team_russia.assist.features.message.services.ChatService;
import com.comp30022.team_russia.assist.features.message.ui.MessageListFragment;

import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.NewMessagePushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewNavStartPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewPicturePushNotification;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import com.google.gson.Gson;
import com.shopify.livedataktx.LiveDataKt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ocpsoft.prettytime.PrettyTime;


/**
 * ViewModel for {@link MessageListFragment}.
 */
public class MessageListViewModel extends BaseViewModel {

    /**
     * A value for associationId that indicates the association has not be loaded yet.
     */
    private static final int NO_ASSOCIATION_YET = -1;

    private static final String CONFIG_UNREAD_INDICATOR_ENABLED = "UNREAD_INDICATOR_ENABLED";

    public int associationId = NO_ASSOCIATION_YET;
    // set to -1 to present invalid state @todo: improve

    private final boolean featureUnreadIndicatorEnabled =
        ConfigurationManager.getInstance().getProperty(CONFIG_UNREAD_INDICATOR_ENABLED, "false")
        .equalsIgnoreCase("true");

    // Public LiveData fields (for view to bind)

    public final MediatorLiveData<List<MessageListItemData>> messageList
        = new MediatorLiveData<>();

    public final MutableLiveData<String> composingMessage = new MutableLiveData<>();

    public final LiveData<Boolean> isSendButtonEnabled;

    /**
     * Whether the ViewModel is busy creating the message list.
     */
    public final MutableLiveData<Boolean> isBusy = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isSending = new MutableLiveData<>();

    /**
     * The title to be displayed on the AppBar.
     */
    public final MutableLiveData<String> title = new MutableLiveData<>();

    /**
     * Whether to show a loading spinner on the screen.
     * Only show spinner when we are loading for the first time, i.e. when the list was populating
     * from an empty state.
     */
    public final LiveData<Boolean> showSpinner;

    public final LiveData<Boolean> showStartNavButton;

    public final MediatorLiveData<Uri> otherUserProfilePicUri = new MediatorLiveData<>();

    // Private fields

    private final LiveData<Boolean> messageListEmpty = LiveDataKt.map(messageList, list ->
        list == null || list.isEmpty());

    private final LiveData<Boolean> isComposingMessageValid;

    private final AuthService authService;
    private final ChatService chatService;
    private final UserService userService;
    private final ProfileDetailsService  profileService;
    private final PubSubHub pubSubHub;
    private final MessageRepository messageRepo;
    private final NavigationService  navigationService;
    private final ToastService toastService;
    private final LoggerInterface logger;

    private final Gson gson = new Gson();

    private Disposable newMsgSubscription = null;

    private Disposable newNavSessionSubscription;

    private String otherUserRealname = "User";

    private int otherUserId = -100;

    private Observer<Uri> stupidObserver = (x) -> { };


    public MutableLiveData<List<PictureDto>> pictureDtoList = new MutableLiveData<>();

    /**
     * Message List View Model.
     * @param authService AuthService used by the Message List View Model.
     * @param chatService ChatService used by the Message List View Model.
     * @param userService UserService used by the Message List View Model.
     * @param notificationHub NotificationHub used by the Message List View Model.
     * @param toastService ToastService used by the Message List View Model.
     * @param messageRepo Message repository.
     */
    @Inject
    public MessageListViewModel(AuthService authService,
                                ChatService chatService,
                                UserService userService,
                                ProfileDetailsService profileService,
                                PubSubHub notificationHub,
                                NavigationService navigationService,
                                ToastService toastService,
                                MessageRepository messageRepo,
                                LoggerFactory loggerFactory) {

        this.authService = authService;
        this.chatService = chatService;
        this.userService = userService;
        this.profileService = profileService;
        this.navigationService = navigationService;
        this.pubSubHub = notificationHub;
        this.toastService = toastService;
        this.messageRepo = messageRepo;
        this.logger = loggerFactory.getLoggerForClass(this.getClass());

        this.showStartNavButton = LiveDataKt.map(navigationService.getCurrentNavSessionLiveData(),
            session -> {
                if (session == null) {
                    return true;
                } else if (session.getActive() == null) {
                    return true;
                } else if (!session.getActive() || session.getId() <= 0) {
                    return true;
                } else {
                    return false;
                }
            });

        isComposingMessageValid = LiveDataKt.map(composingMessage, value ->
            value != null && !value.isEmpty());
        isSendButtonEnabled = combineLatest(isComposingMessageValid, isSending,
            (valid, sending) ->
                valid != null && sending != null && valid && !sending
        );

        showSpinner = combineLatest(isBusy, messageListEmpty,
            (isBusy, messageListEmpty) ->
                isBusy != null && messageListEmpty != null && isBusy && messageListEmpty);

        // initial values
        messageList.postValue(new ArrayList<>());
        composingMessage.postValue("");
        isSending.postValue(false);
        isBusy.postValue(false);
        title.postValue("Message");

        otherUserProfilePicUri.observeForever(stupidObserver);
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
                    this.otherUserId = result.unwrap().getUserId();

                    // load profile picture uri
                    profileService.getUsersProfilePicture(otherUserId).thenAcceptAsync(presult -> {
                        if (presult.isSuccessful()) {
                            this.otherUserProfilePicUri.addSource(presult.unwrap(), path -> {
                                logger.debug("profile uri" + path);
                                if (path != null) {
                                    otherUserProfilePicUri.postValue(Uri.parse(path));
                                }
                            });
                        } else {
                            logger.error("failed to load profile image for user " + otherUserId);
                        }
                    });
                } else {
                    // todo retry (after PR143)
                }
            });



        // load message history once
        observeMessageList();

        // listen for push notification about new message arrival.
        this.newMsgSubscription = pubSubHub.subscribe(PubSubTopics.NEW_MESSAGE,
            new SubscriberCallback<NewMessagePushNotification>() {
                @Override
                public void onReceived(NewMessagePushNotification payload) {
                    // only sync messages if the new message is of the same association
                    // @todo: maybe if the new message is for a different chat, also load the
                    // messages
                    //        in the background
                    if (payload.getAssociationId() == MessageListViewModel.this.associationId) {
                        syncMessages();
                    }
                }
            });

        // listen for push notification about new picture arrival.
        this.newMsgSubscription = pubSubHub.subscribe(PubSubTopics.NEW_PICTURE,
            new SubscriberCallback<NewPicturePushNotification>() {
                @Override
                public void onReceived(NewPicturePushNotification payload) {

                    if (payload.getAssociationId() == MessageListViewModel.this.associationId) {

                        // get image
                        loadImage(payload.getAssociationId(), payload.getPictureId());
                    }
                }
            });
    }


    /**
     * Get list of PictureDto.
     * @param imageCount number of images to send
     */
    public void getPictureDtoFromServer(int imageCount) {
        chatService.getPictureIdPlaceholder(associationId, imageCount).thenAccept(result -> {
            if (result.isSuccessful()) {
                pictureDtoList.postValue(result.unwrap());
            } else {
                logger.error("could not get picture ids");
            }
        });
    }

    /**
     * Send images to server.
     * @param pictureId picture id of image
     * @param file file to send to server
     */
    public void sendImageToServer(int pictureId, File file) {
        chatService.sendImage(associationId, pictureId, file).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("successfully sent image with pictureid " + pictureId);
                syncMessages();

            } else {
                logger.error("could not send image with pictureid " + pictureId);
            }
        });
    }

    /**
     * Load image from server.
     * @param associationId association id
     * @param pictureId picture id
     */
    private void loadImage(int associationId, int pictureId) {
        chatService.getImage(associationId, pictureId).thenAccept(result -> {
            if (result.isSuccessful()) {
                //todo load image into chat bubble FOR THE RECEIVER
                result.unwrap().getPicture();

                syncMessages();

            } else {
                logger.error("could not get image from server pictureId " + pictureId);
            }
        });
    }

    /**
     * Set up the LiveData chain to observe changes in the chat history
     * (populated from server -> local DB -> view model).
     */
    private void observeMessageList() {
        if (this.associationId > 0) {
            messageList.addSource(messageRepo.getMessages(this.associationId), newMessages -> {
                isBusy.postValue(true);
                // This list transformation is a CPU heavy task.
                // Need to do it in the background to avoid blocking the UI.
                AsyncTask.execute(() -> {
                    int currentUserId = authService.getCurrentUser().getUserId();
                    ArrayList<MessageListItemData> result = new ArrayList<>();
                    if (newMessages == null) {
                        return;
                    }
                    // @todo: this can be optimized to avoid recreating the entire list every time.
                    for (Message message : newMessages) {

                        result.add(new MessageListItemData(
                            message.getId(),
                            message.getAuthorId() == currentUserId,
                            message.getContent(),
                            new PrettyTime().format(message.getCreatedAt()),
                            otherUserRealname,

                            message.getType().equals("Picture") ? message.getPictureId() : -1));
                    }
                    messageList.postValue(result);
                    isBusy.postValue(false);
                });
            });
        }
    }

    private void syncMessages() {
        if (this.associationId > 0) {
            AsyncTask.execute(() -> messageRepo.syncMessages(associationId));
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
                    syncMessages();
                } else {
                    toastService.toastShort("Message not sent.");
                }
                isSending.postValue(false);
            });
    }

    /**
     * Handle on start navigation button clicked.
     */
    public void onStartNavigationClicked() {
        Bundle bundle = new Bundle();
        bundle.putInt("assocId", associationId);
        Boolean isAp = authService.getCurrentUser().getUserType() == User.UserType.AP;
        bundle.putBoolean("apInitiated", isAp);

        navigateTo(R.id.action_start_navigation, bundle);
    }

    /**
     * Handle on start video call button clicked.
     */
    public void onStartVideoCallClicked() {
        navigateTo(R.id.action_start_video_call);
    }


    /**
     * Event handler for when user clicks on the Detail button.
     */
    public void onDetailButtonClicked() {
        Bundle bundle = new Bundle();
        bundle.putInt("assocId", associationId);
        Boolean isAp = authService.getCurrentUser().getUserType() == User.UserType.AP;
        bundle.putBoolean("apInitiated", isAp);
        bundle.putInt("otherUserId", otherUserId);

        navigateTo(R.id.action_view_detail, bundle);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // always clean up the subscriptions in LiveModels, to prevent leaking.
        if (this.newMsgSubscription != null) {
            this.newMsgSubscription.dispose();
        }
        if (this.newNavSessionSubscription != null) {
            this.newNavSessionSubscription.dispose();
        }
        this.otherUserProfilePicUri.removeObserver(stupidObserver);
    }

    /**
     * Called when the list view on the fragment is scrolled.
     * @param itemPosition The position of the last visible item.
     */
    public void onScrolled(int itemPosition) {
        if (!featureUnreadIndicatorEnabled) {
            return;
        }

        List<MessageListItemData> messages = messageList.getValue();

        if (messages != null && itemPosition >= 0
            && itemPosition < messages.size()
            && this.associationId != NO_ASSOCIATION_YET) {
            MessageListItemData lastItem = messages.get(itemPosition);
            messageRepo.updateReadPointer(this.associationId, lastItem.id);
            logger.debug("Scrolled to item " + lastItem.id + ": " + lastItem.content);
        }
    }


}