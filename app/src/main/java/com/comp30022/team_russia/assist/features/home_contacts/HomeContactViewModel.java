package com.comp30022.team_russia.assist.features.home_contacts;

import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;

import java.util.ArrayList;
import java.util.List;

public class HomeContactViewModel extends BaseViewModel {

    public final MutableLiveData<List<ContactListItemData>> contactList = new MutableLiveData<>();

    public HomeContactViewModel() {
        // dummy data for now, should load these from service
        ArrayList<ContactListItemData> c = new ArrayList<>();
        c.add(new ContactListItemData(1, "Richard", "user1", "Hi, how are you?"));
        c.add(new ContactListItemData(2, "James", "user2", "Wanna play tonight?"));
        c.add(new ContactListItemData(3,"Old man", "user3", "Help dsdfme, son!"));
        contactList.postValue(c);
    }

    public void onListItemClicked(ContactListItemData item) {
        Log.i("", item.username + " Clicked");
        // test only
        addDummyContactItem();
        Bundle bundle = new Bundle();
        navigateTo(R.id.action_view_chat, bundle);
    }

    private void addDummyContactItem() {
        ArrayList<ContactListItemData> newList = new ArrayList<>(contactList.getValue());
        int badNumber = 4;
        newList.add(new ContactListItemData(badNumber,"Someone", String.format("new_user%d",newList.size()), "message"));
        contactList.postValue(newList);
    }

    public void addPersonToChat() {
        navigateTo(R.id.action_add_person);
    }
}
