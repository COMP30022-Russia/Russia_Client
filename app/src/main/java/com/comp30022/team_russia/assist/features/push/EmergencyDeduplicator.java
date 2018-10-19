package com.comp30022.team_russia.assist.features.push;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Deduplicator for Navigation AP location updates.
 */
public class EmergencyDeduplicator {

    private  static final String TAG = "EmergDedup";

    private static final Set<Integer> events = new HashSet<>();

    private static final Object lock = new Object();

    /**
     * Execute a runnable only if the nav session id and sync token in not out of date.
     * @param r The logic execute.
     */
    public static void ensureEmergencyNotDuplicated(int eventId, Runnable r) {
        synchronized (lock) {

            if (events.contains(eventId)) {
                Log.d(TAG,
                    String.format("Ignored seen emergency with id = %d", eventId));
                return;
            }
            events.add(eventId);
            Log.d(TAG, String.format("Added event = %d", eventId));
        }
        r.run();
    }

}
