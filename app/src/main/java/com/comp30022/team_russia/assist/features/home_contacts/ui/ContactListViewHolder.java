package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.databinding.ContactListItemBinding;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;

public class ContactListViewHolder extends RecyclerView.ViewHolder {

    final ContactListItemBinding binding;

    public ContactListViewHolder(ContactListItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
