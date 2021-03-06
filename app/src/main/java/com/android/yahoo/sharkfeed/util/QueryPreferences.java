package com.android.yahoo.sharkfeed.util;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * QueryPreferences class is used to store query, last result id and alarm status into the
 * preference manager. (To persist primitive data across device restarts)
 */

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";
    private static final String PREF_PAGE_NUMBER = "pageNumber";

    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putString(PREF_SEARCH_QUERY, query)
        .apply();
    }

    public static String getLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }

    public static void setLastResultId(Context context, String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply();
    }

    public static boolean isAlarmOn(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static void setPrefIsAlarmOn(Context context, boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }


    public static Integer getPageNumber(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_PAGE_NUMBER, 2);
    }

    public static void setPageNumber(Context context, Integer pageNumber){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_PAGE_NUMBER, pageNumber)
                .apply();
    }


}
