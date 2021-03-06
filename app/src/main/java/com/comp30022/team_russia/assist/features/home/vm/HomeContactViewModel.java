package com.comp30022.team_russia.assist.features.home.vm;

import static com.comp30022.team_russia.assist.base.LiveDataHelpers.combineLatest;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.DisposableCollection;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.pubsub.PubSubHub;
import com.comp30022.team_russia.assist.base.pubsub.PubSubTopics;
import com.comp30022.team_russia.assist.base.pubsub.SubscriberCallback;
import com.comp30022.team_russia.assist.features.assoc.db.UserAssociationCache;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDto;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.chat.db.MessageRepository;
import com.comp30022.team_russia.assist.features.home.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home.ui.HomeContactFragment;
import com.comp30022.team_russia.assist.features.location.services.RealTimeLocationService;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.push.models.NewMessagePushNotification;

import com.google.android.gms.maps.model.LatLng;
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
    private final RealTimeLocationService realTimeLocationService;
    private final MessageRepository messageRepository;
    private final UserAssociationCache usersCache;
    private final PubSubHub pubSubHub;
    private final LoggerInterface logger;

    private final ProfileDetailsService profileService;

    private final Observer<Boolean> loggedInStateObserver;

    private DisposableCollection subscriptions = new DisposableCollection();


    /**
     * Constructor.
     */
    @Inject
    public HomeContactViewModel(AuthService authService,
                                UserService userService,
                                ProfileDetailsService profileDetailsService,
                                RealTimeLocationService realTimeLocationService,
                                MessageRepository messageRepository,
                                LoggerFactory loggerFactory,
                                PubSubHub pubSubHub,
                                UserAssociationCache usersCache) {

        this.profileService = profileDetailsService;
        this.authService = authService;
        this.userService = userService;
        this.realTimeLocationService = realTimeLocationService;

        this.messageRepository = messageRepository;
        this.usersCache = usersCache;
        this.pubSubHub = pubSubHub;
        this.logger = loggerFactory.getLoggerForClass(this.getClass());

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
    }


    /**
     * Called when {@link HomeContactFragment} becomes visible.
     */
    public void onStart() {
        reloadContactList();
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
        AsyncTask.execute(() ->
            this.userService.getAssociatedUsers().thenAcceptAsync(associations -> {
                logger.info("Got associated users. The local cache should be updated shortly.");
                for (AssociationDto association : associations) {
                    logger.info(String.format(
                        "Triggering chat history sync for association %d (%s)",
                        association.getId(),
                        association.getUser().getName()));
                    this.messageRepository.syncMessages(association.getId());

                    profileService.getUsersProfilePicture(association.getId())
                        .thenAcceptAsync(presult -> {
                            if (presult.isSuccessful()) {
                                // hey
                            } else {
                                logger.error("failed to load profile image for user "
                                             + association.getId());
                            }
                        });
                }
            }));
    }

    /**
     * Called when the "+" floating button is clicked.
     */
    public void addPersonToChat() {
        navigateTo(R.id.action_add_person);
    }

    /**
     * Updates the AP location to server.
     * For details page AP location monitoring feature.
     * @param newApLocation The new location of the AP.
     */
    public void updateApLocation(LatLng newApLocation) {
        realTimeLocationService.updateApCurrentLocation(newApLocation).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("updateApLocation: successfully updated location of ap");
            } else {
                logger.error("updateApLocation: failed to updated location of ap");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.subscriptions.dispose();
        this.authService.isLoggedIn().removeObserver(loggedInStateObserver);
    }

}
