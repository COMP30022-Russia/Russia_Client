package com.comp30022.team_russia.assist.features.home_contacts.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDto;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.ui.HomeContactFragment;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import com.shopify.livedataktx.LiveDataKt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * ViewModel for {@link HomeContactFragment}.
 */
public class HomeContactViewModel extends BaseViewModel {

    public final MutableLiveData<List<ContactListItemData>> contactList
        = new MutableLiveData<>();

    public final LiveData<Boolean> isEmptyList;

    private final AuthService authService;
    private final UserService userService;

    /**
     * Constructor.
     * @param authService Instance of {@link AuthService}.
     * @param userService Instance of {@link UserService}.
     */
    @Inject
    public HomeContactViewModel(AuthService authService, UserService userService) {

        this.authService = authService;
        this.userService = userService;

        isEmptyList = LiveDataKt.map(contactList, value ->
            value == null || value.isEmpty()
        );

        contactList.postValue(new ArrayList<>());
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
    public void reloadContactList() {
        if (this.authService.isLoggedInUnboxed()) {
            this.userService.getAssociatedUsers().thenAccept(associations -> {
                ArrayList<ContactListItemData> contactList = new ArrayList<>();
                for (AssociationDto association : associations) {
                    contactList.add(new ContactListItemData(
                        association.getId(),
                        association.getUser().getId(),
                        association.getUser().getName(),
                        "No message")
                    );
                }
                this.contactList.postValue(contactList);
            });
        }
    }

    public void addPersonToChat() {
        navigateTo(R.id.action_add_person);
    }
}
