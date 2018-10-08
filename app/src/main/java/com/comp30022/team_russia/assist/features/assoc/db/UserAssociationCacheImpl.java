package com.comp30022.team_russia.assist.features.assoc.db;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.db.RussiaDatabase;
import com.comp30022.team_russia.assist.features.assoc.db.tasks.InsertSingleAssociationAsyncTask;
import com.comp30022.team_russia.assist.features.assoc.db.tasks.ReplaceAssociationsAsyncTask;
import com.comp30022.team_russia.assist.features.assoc.db.tasks.UpdateUserProfileAsyncTask;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.message.models.Association;
import com.comp30022.team_russia.assist.features.message.models.UserContact;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import kotlin.NotImplementedError;

/**
 * Implementation of {@link UserAssociationCache}.
 */
@SuppressWarnings("unchecked")
public class UserAssociationCacheImpl implements UserAssociationCache {

    private final UserDao userDao;
    private final LoggerInterface logger;

    @Inject
    public UserAssociationCacheImpl(RussiaDatabase db, LoggerFactory loggerFactory) {
        userDao = db.userDao();
        logger = loggerFactory.create(this.getClass().getSimpleName());
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void replaceAssociations(List<Association> newAssociations) {
        logger.info(String.format("Replacing Association cache with %d new Associations.",
            newAssociations.size()));
        new ReplaceAssociationsAsyncTask(userDao).execute(newAssociations);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void batchUpdateUserProfiles(List<UserContact> users) {
        logger.info(String.format("Updating/inserting %d user profiles into cache.", users.size()));
        new UpdateUserProfileAsyncTask(userDao).execute(users);
    }

    @Override
    public void insertOrUpdateUserProfile(UserContact user) {
        ArrayList<UserContact> users = new ArrayList<>();
        users.add(user);
        batchUpdateUserProfiles(users);
    }

    @Override
    public void insertOrUpdateAssociation(Association association) {
        new InsertSingleAssociationAsyncTask(userDao).execute(association);
    }

    @Override
    public LiveData<List<ContactListItemData>> getContactList() {
        return userDao.getContactList();
    }

    @Override
    public void clear() {
        logger.info("Clearing all User and Association cache.");
        throw new NotImplementedError();
    }

}
