package com.comp30022.team_russia.assist.features.push;

import android.util.Log;

/**
 * Deduplicator for Navigation start/end/switch control/new route etc.
 */
public class NavSyncTokenDeduplicator {

    private  static final String TAG = "NavSyncTokenDedup";

    private static int lastNavSessionId = -1;
    private static int lastSyncToken = -1;

    private static final Object lock = new Object();

    /**
     * Execute a runnable only if the nav session id and sync token in not out of date.
     * @param sessionId The nav session ID.
     * @param syncToken The sync token, resets per nav session.
     * @param r The logic to run.
     */
    public static void ensureNavSyncTokenValid(int sessionId, int syncToken, Runnable r) {
        synchronized (lock) {
            if ((sessionId < lastNavSessionId)
                || (sessionId == lastNavSessionId && syncToken <= lastSyncToken)) {
                Log.d(TAG,
                    String.format("Ignored outdated data message with "
                                  + "sessionId = %d, syncToken = %d",
                        sessionId, syncToken));
                return;
            }
            lastNavSessionId = sessionId;
            lastSyncToken = syncToken;
            Log.d(TAG,
                String.format("Updated sessionId = %d, syncToken = %d",
                    lastNavSessionId, lastSyncToken));
        }
        r.run();
    }

}
