package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.databinding.ItemContactListBinding;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.vm.HomeContactViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for the contact list.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListViewHolder> {

    private List<ContactListItemData> contactItemList = new ArrayList<>();

    private final HomeContactViewModel viewModel;

    public ContactListAdapter(HomeContactViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public int getItemCount() {
        return contactItemList.size();
    }

    @NonNull
    @Override
    public ContactListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemContactListBinding binding = DataBindingUtil.inflate(layoutInflater,
            R.layout.item_contact_list,
            parent,
            false);
        binding.setViewmodel(viewModel);
        return new ContactListViewHolder(binding);
    }

    /**
     * Updates the content of the list.
     * @param contactItemList New contact list items.
     */
    public void setContactItemList(final List<ContactListItemData> contactItemList) {
        // We use DiffUtil to avoid reloading the whole list every time.
        // DiffUtil will calculate the changes, and will only make necessary
        // updates to the RecyclerView.
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return ContactListAdapter.this.contactItemList.size();
            }

            @Override
            public int getNewListSize() {
                return contactItemList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemIndex, int newItemIndex) {
                return ContactListAdapter.this.contactItemList.get(oldItemIndex).getAssociationId()
                       == contactItemList.get(newItemIndex).getAssociationId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemIndex, int newItemIndex) {
                return ContactListAdapter.this.contactItemList.get(oldItemIndex)
                    .equals(contactItemList.get(newItemIndex));
            }
        });
        this.contactItemList = contactItemList;
        result.dispatchUpdatesTo(this);
    }

    public void onBindViewHolder(ContactListViewHolder holder, int i) {
        holder.binding.setData(contactItemList.get(i));
        holder.binding.executePendingBindings();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
