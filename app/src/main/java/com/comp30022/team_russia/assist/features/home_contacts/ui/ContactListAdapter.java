package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleService;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.databinding.ItemContactListBinding;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListProfileImageWrapper;
import com.comp30022.team_russia.assist.features.home_contacts.vm.HomeContactViewModel;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for the contact list.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListViewHolder> {

    private List<ContactListItemData> contactItemList = new ArrayList<>();

    private final HomeContactViewModel viewModel;

    private final ProfileDetailsService profileService;

    private final HomeContactFragment homeContactFragment;

    /**
     * Constructor.
     */
    public ContactListAdapter(HomeContactViewModel viewModel,
                              HomeContactFragment homeContactFragment,
                              ProfileDetailsService ps) {
        this.viewModel = viewModel;
        this.homeContactFragment = homeContactFragment;
        this.profileService = ps;
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
        ContactListProfileImageWrapper p = new ContactListProfileImageWrapper();
        binding.setP(p);
        binding.setLifecycleOwner(this.homeContactFragment);
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

    @Override
    public void onBindViewHolder(ContactListViewHolder holder, int i) {
        ContactListItemData data = contactItemList.get(i);
        final ContactListProfileImageWrapper wrapper = holder.binding.getP();
        holder.binding.setData(data);
        final int userId = data.getUserId();

        profileService.getUsersProfilePicture(userId)
            .thenAcceptAsync((pr) -> {
                if (pr.isSuccessful()) {
                    wrapper.getUri().addSource(pr.unwrap(), path -> {
                        if (path != null) {
                            wrapper.getUri().postValue(Uri.parse(path));
                        }
                    });
                }
            });

        holder.binding.setLifecycleOwner(homeContactFragment);
    }

}
