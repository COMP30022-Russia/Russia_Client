package com.comp30022.team_russia.assist.features.assoc.db.tasks;

import android.os.AsyncTask;

import com.comp30022.team_russia.assist.features.assoc.db.UserDao;
import com.comp30022.team_russia.assist.features.message.models.UserContact;

import java.util.List;

/**
 * {@link AsyncTask} for inserting / updating {@link UserContact} in batch into local cache.
 */
public final class UpdateUserProfileAsyncTask extends AsyncTask<List<UserContact>, Void, Void> {

    private UserDao asyncTaskDao;

    public UpdateUserProfileAsyncTask(UserDao dao) {
        asyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(List<UserContact>... lists) {
        for (UserContact user : lists[0]) {
            asyncTaskDao.insertOrUpdateUserProfile(user);
        }
        return null;
    }
}