package com.android.yahoo.sharkfeed.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by sai pranesh on 6/15/2017.
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
