package com.comp30022.team_russia.assist.features.assoc.db.tasks;

import android.os.AsyncTask;

import com.comp30022.team_russia.assist.features.assoc.db.UserDao;
import com.comp30022.team_russia.assist.features.chat.models.Association;

/**
 * {@link AsyncTask} for inserting / updating a single {@link Association} into local cache.
 */
public final class InsertSingleAssociationAsyncTask extends AsyncTask<Association, Void, Void> {
    private final UserDao asyncTaskDao;

    public InsertSingleAssociationAsyncTask(UserDao dao) {
        asyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(Association... lists) {
        asyncTaskDao.insertOrUpdateAssociation(lists[0]);
        return null;
    }
}
