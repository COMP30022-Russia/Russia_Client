package com.comp30022.team_russia.assist.features.home_contacts.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDto;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.ui.HomeContactFragment;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;
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
    private final PubSubHub pubSubHub;
    private final LoggerInterface logger;

    private Disposable notificationSubscription;

    /**
     * Constructor.
     * @param authService Instance of {@link AuthService}.
     * @param userService Instance of {@link UserService}.
     */
    @Inject
    public HomeContactViewModel(AuthService authService, UserService userService,
                                LoggerFactory loggerFactory, PubSubHub pubSubHub) {

        this.authService = authService;
        this.userService = userService;
        this.pubSubHub = pubSubHub;
        this.logger = loggerFactory.create(this.getClass().getSimpleName());

        isEmptyList = LiveDataKt.map(contactList, value ->
            value == null || value.isEmpty()
        );
        // initial values
        contactList.postValue(new ArrayList<>());
        contactList.addSource(this.authService.isLoggedIn(), loggedIn -> {
            if (loggedIn) {
                this.reloadContactList();
            }
        });

        this.pubSubHub.configureTopic(PubSubTopics.NEW_ASSOCIATION, Void.class,
            new PayloadToObjectConverter<Void>() {
                @Override
                public Void fromString(String payloadStr) {
                    return null;
                }

                @Override
                public String toString(Void payload) {
                    return "";
                }
            });

        this.notificationSubscription = this.pubSubHub.subscribe(
            PubSubTopics.NEW_ASSOCIATION,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    logger.info("Someone associated with me. Refreshing...");
                    HomeContactViewModel.this.reloadContactList();
                }
            });

    }

    /**
     * Event handler for when a contact list item is clicked.
     * @param item The contact list item being clicked.
     */
    public void onListItemClicked(ContactListItemData item) {
        Log.i("", item.associationId + " Clicked");

        Bundle bundle = new Bundle();
        bundle.putInt("associationId", item.associationId);
        navigateTo(R.id.action_view_chat, bundle);
    }

    /**
     * Refreshes the contact list.
     */
    private void reloadContactList() {
        HomeContactViewModel.this.userService.getAssociatedUsers().thenAccept(
            associations -> {
                ArrayList<ContactListItemData> contactList = new ArrayList<>();
                for (AssociationDto association : associations) {
                    contactList.add(new ContactListItemData(
                        association.getId(),
                        association.getUser().getId(),
                        association.getUser().getName(),
                        "No message")
                    );
                }
                HomeContactViewModel.this.contactList.postValue(contactList);
            });

    }

    public void addPersonToChat() {
        navigateTo(R.id.action_add_person);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (this.notificationSubscription != null) {
            this.notificationSubscription.dispose();
        }
    }
}
