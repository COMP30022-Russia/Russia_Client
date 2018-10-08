package com.comp30022.team_russia.assist.features.assoc.db.tasks;

import android.os.AsyncTask;

import com.comp30022.team_russia.assist.features.assoc.db.UserDao;
import com.comp30022.team_russia.assist.features.message.models.Association;

import java.util.List;

/**
 * {@AsyncTask} for replacing all the locally-cached {@Association}s.
 */
public final class ReplaceAssociationsAsyncTask extends AsyncTask<List<Association>, Void, Void> {

    private UserDao asyncTaskDao;

    public ReplaceAssociationsAsyncTask(UserDao dao) {
        asyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(List<Association>... lists) {
        asyncTaskDao.clearAssociations();
        for (Association association : lists[0]) {
            asyncTaskDao.insertOrUpdateAssociation(association);
        }
        return null;
    }
}