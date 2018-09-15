package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDTO;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.models.Association;
import com.shopify.livedataktx.LiveDataKt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class HomeContactViewModel extends BaseViewModel {

    public final MutableLiveData<List<ContactListItemData>> contactList
        = new MutableLiveData<>();

    public final LiveData<Boolean> isEmptyList;

    private final AuthService authService;
    private final UserService userService;

    @Inject
    public HomeContactViewModel(AuthService authService, UserService userService) {

        this.authService = authService;
        this.userService = userService;

        isEmptyList = LiveDataKt.map(contactList, value ->
            value == null || value.isEmpty()
        );

        // dummy data for now, should load these from service
        ArrayList<ContactListItemData> c = new ArrayList<>();
        /*c.add(new ContactListItemData(1, "Richard", "user1", "Hi, how are you?"));
        c.add(new ContactListItemData(2, "James", "user2", "Wanna play tonight?"));
        c.add(new ContactListItemData(3,"Old man", "user3", "Help dsdfme, son!"));*/
        contactList.postValue(c);
    }

    public void onListItemClicked(ContactListItemData item) {
        Log.i("", item.associationId + " Clicked");
        // test only
       // addDummyContactItem();
        Bundle bundle = new Bundle();
        bundle.putInt("associationId", item.associationId);
        navigateTo(R.id.action_view_chat, bundle);
    }

    public void reloadContactList() {
        if (this.authService.isLoggedInUnboxed()) {
            this.userService.getAssociatedUsers().thenAccept(associations -> {
                ArrayList<ContactListItemData> contactList = new ArrayList<>();
                for (AssociationDTO association : associations) {
                    contactList.add(new ContactListItemData(
                        association.id,
                        association.user.id,
                        association.user.name,
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
