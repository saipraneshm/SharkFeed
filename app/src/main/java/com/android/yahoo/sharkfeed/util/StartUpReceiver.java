package com.android.yahoo.sharkfeed.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Standalone receiver to start the alarm or poll service once the device restarts, without having
 * the user to open the app to start polling for new results again.
 */

public class StartUpReceiver extends BroadcastReceiver {

    private static final String TAG = StartUpReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent : " + intent.getAction());
        boolean isAlarmOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isAlarmOn);
    }

}
