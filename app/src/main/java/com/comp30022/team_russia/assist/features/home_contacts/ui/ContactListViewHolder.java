package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.support.v7.widget.RecyclerView;
import com.comp30022.team_russia.assist.databinding.ItemContactListBinding;

/**
 * ViewHolder for an item in the contact list.
 */
public class ContactListViewHolder extends RecyclerView.ViewHolder {

    final ItemContactListBinding binding;

    public ContactListViewHolder(ItemContactListBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
