package com.comp30022.team_russia.assist.features.push;

import android.util.Log;

/**
 * Deduplicator for Navigation AP location updates.
 */
public class NavApLocationUpdateTokenDeduplicator {

    private  static final String TAG = "NavApSyncTokenDedup";

    private static long lastSyncToken = -1L;

    private static final Object lock = new Object();

    /**
     * Execute a runnable only if the nav session id and sync token in not out of date.
     * @param syncToken The sync token (based on server timestamp).
     * @param r The logic execute.
     */
    public static void ensureApLocSyncTokenValid(long syncToken, Runnable r) {
        synchronized (lock) {
            if (syncToken <= lastSyncToken) {
                Log.d(TAG,
                    String.format("Ignored outdated data message with syncToken = %d", syncToken));
                return;
            }
            lastSyncToken = syncToken;
            Log.d(TAG, String.format("Updated syncToken = %d", lastSyncToken));
        }
        r.run();
    }

}
