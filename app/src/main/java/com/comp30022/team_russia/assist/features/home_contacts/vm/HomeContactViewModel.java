package com.comp30022.team_russia.assist.features.home_contacts.vm;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.DisposableCollection;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.assoc.db.UserAssociationCache;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDto;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.ui.HomeContactFragment;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.db.MessageRepository;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.NewMessagePushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewNavStartPushNotification;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;
import com.google.gson.Gson;
import com.shopify.livedataktx.LiveDataKt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * ViewModel for {@link HomeContactFragment}.
 */
public class HomeContactViewModel extends BaseViewModel {

    public final MediatorLiveData<List<ContactListItemData>> contactList
        = new MediatorLiveData<>();

    public final LiveData<Boolean> isEmptyList;

    private final AuthService authService;
    private final UserService userService;
    private final MessageRepository messageRepository;
    private final UserAssociationCache usersCache;
    private final PubSubHub pubSubHub;
    private final LoggerInterface logger;
    private final Gson gson = new Gson();


    private final Observer<Boolean> loggedInStateObserver;
    // @todo: use DisposableCollection after PR 128
    private DisposableCollection subscriptions = new DisposableCollection();


    @Inject
    public HomeContactViewModel(AuthService authService,
                                UserService userService,
                                MessageRepository messageRepository,
                                LoggerFactory loggerFactory,
                                PubSubHub pubSubHub,
                                UserAssociationCache usersCache) {

        this.authService = authService;
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.usersCache = usersCache;
        this.pubSubHub = pubSubHub;
        this.logger = loggerFactory.create(this.getClass().getSimpleName());

        isEmptyList = LiveDataKt.map(contactList, value ->
            value == null || value.isEmpty()
        );

        // initial values
        contactList.postValue(new ArrayList<>());

        // Wire up contactList to the local cache.
        contactList.addSource(combineLatest(this.authService.isLoggedIn(),
            usersCache.getContactList(),
            (loggedIn, newList) -> {
                if (loggedIn != null && loggedIn) {
                    return newList;
                } else {
                    return new ArrayList<>();
                }
            }), this.contactList::postValue);

        loggedInStateObserver = (loggedIn) -> {
            if (loggedIn != null && loggedIn) {
                this.reloadContactList();
            }
        };

        authService.isLoggedIn().observeForever(loggedInStateObserver);

        setUpPubSubSubscriptions();

    }

    private void setUpPubSubSubscriptions() {
        // Listener for new association
        subscriptions.add(pubSubHub.subscribe(
            PubSubTopics.NEW_ASSOCIATION,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    logger.info("Someone associated with me. Refreshing...");
                    HomeContactViewModel.this.reloadContactList();
                }
            }));


        // Listener for new message
        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NEW_MESSAGE,
            new SubscriberCallback<NewMessagePushNotification>() {
                @Override
                public void onReceived(NewMessagePushNotification payload) {
                    messageRepository.syncMessages(payload.getAssociationId());
                }
            })
        );

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.LOGGED_IN,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    reloadContactList();
                }
            })
        );


        // TODO: move this to activity and use deeplink to link to navigation screen
        // todo to preserve backstack
        // Listener for start of nav session

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_START,
            new SubscriberCallback<NewNavStartPushNotification>() {
                @Override
                public void onReceived(NewNavStartPushNotification payload) {
                    // start nav session
                    Bundle bundle = new Bundle();
                    bundle.putInt("assocId", payload.getAssociationId());
                    bundle.putInt("sessionId", payload.getSessionId());
                    Boolean isAp = authService.getCurrentUser().getUserType() != User.UserType.AP;
                    bundle.putBoolean("apInitiated", isAp);

                    navigateTo(R.id.action_show_nav_request_from_home, bundle);
                }
            }));
    }


    /**
     * Event handler for when a contact list item is clicked.
     * @param item The contact list item being clicked.
     */
    public void onListItemClicked(ContactListItemData item) {
        Log.i("", item.getAssociationId() + " Clicked");

        Bundle bundle = new Bundle();
        bundle.putInt("associationId", item.getAssociationId());
        navigateTo(R.id.action_view_chat, bundle);
    }

    /**
     * Explicitly refreshes the contact list.
     */
    @SuppressLint("DefaultLocale")
    private void reloadContactList() {
        this.userService.getAssociatedUsers().thenAcceptAsync(associations -> {
            logger.info("Got associated users. The local cache should be updated shortly.");
            for (AssociationDto association : associations) {
                logger.info(String.format(
                    "Triggering chat history sync for association %d (%s)",
                    association.getId(),
                    association.getUser().getName()));
                this.messageRepository.syncMessages(association.getId());
            }
        });
    }

    public void addPersonToChat() {
        navigateTo(R.id.action_add_person);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.subscriptions.dispose();
        this.authService.isLoggedIn().removeObserver(loggedInStateObserver);
    }

}

