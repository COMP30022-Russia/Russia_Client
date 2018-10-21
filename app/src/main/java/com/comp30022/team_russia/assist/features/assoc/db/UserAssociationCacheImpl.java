package com.comp30022.team_russia.assist.features.assoc.db;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.ConfigurationManager;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.db.RussiaDatabase;
import com.comp30022.team_russia.assist.features.assoc.db.tasks.InsertSingleAssociationAsyncTask;
import com.comp30022.team_russia.assist.features.assoc.db.tasks.ReplaceAssociationsAsyncTask;
import com.comp30022.team_russia.assist.features.assoc.db.tasks.UpdateUserProfileAsyncTask;
import com.comp30022.team_russia.assist.features.chat.models.Association;
import com.comp30022.team_russia.assist.features.chat.models.UserContact;
import com.comp30022.team_russia.assist.features.home.models.ContactListItemData;

import com.shopify.livedataktx.LiveDataKt;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import javax.inject.Inject;

import kotlin.NotImplementedError;

/**
 * Implementation of {@link UserAssociationCache}.
 */
@SuppressWarnings("unchecked")
public class UserAssociationCacheImpl implements UserAssociationCache {

    private final UserDao userDao;
    private final LoggerInterface logger;

    private static final String CONFIG_UNREAD_INDICATOR_ENABLED = "UNREAD_INDICATOR_ENABLED";

    private final boolean featureUnreadIndicatorEnabled =
        ConfigurationManager.getInstance().getProperty(CONFIG_UNREAD_INDICATOR_ENABLED,"false")
            .equalsIgnoreCase("true");

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
        return featureUnreadIndicatorEnabled ? userDao.getContactList()
            : LiveDataKt.map(userDao.getContactList(), list -> {
                if (list == null) {
                    return null;
                }
                return StreamSupport.stream(list).map(x
                    -> new ContactListItemData(
                        x.getAssociationId(),
                        x.getUserId(),
                        x.getName(),
                        x.getLastMessage(),
                        false)).collect(Collectors.toList());
            }
        );
    }

    @Override
    public void clear() {
        logger.info("Clearing all User and Association cache.");
        throw new NotImplementedError();
    }

}
