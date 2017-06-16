package com.android.yahoo.sharkfeed.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.activity.SharkFeedGalleryActivity;
import com.android.yahoo.sharkfeed.model.Photo;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sai pranesh on 6/15/2017.
 */

public class PollService extends IntentService {

    private static final String TAG = PollService.class.getSimpleName();
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15);
    private static final int JOB_ID = 1;
    public static final String ACTION_SHOW_NOTIFICATION =
            "com.android.yahoo.sharkfeed.ACTION_SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.android.yahoo.sharkfeed.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";


    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(!AppUtils.isNetworkAvailableAndConnected(this)){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<Photo> photosList;

        if(query == null){
            photosList = new FlickrFetcher().fetchSharkPhotos(0);
        }else{
            photosList = new FlickrFetcher().searchSharkPhotos(query, 0);
        }

        if(photosList.size() == 0){
            return;
        }

        String resultId = photosList.get(0).getId();
        if(resultId.equals(lastResultId)){
            Log.d(TAG, "got an old result");
        }else{
            Log.d(TAG, "got a new result");

            Resources resources = getResources();
            Intent i = SharkFeedGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i , 0);

            Notification notification =new  NotificationCompat.Builder(this)
                                .setTicker(resources.getString(R.string.new_pictures_title))
                                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                .setContentTitle(resources.getString(R.string.new_pictures_title))
                                .setContentText(resources.getString(R.string.new_pictures_text))
                                .setContentIntent(pi)
                                .setAutoCancel(true)
                                .build();
                showBackgroundNotification(0, notification);

        }

        QueryPreferences.setLastResultId(this, resultId);

    }

    private void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }


    public static void setServiceAlarm(Context context, boolean isOn){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setStartJobService(context, isOn);
        }else{
            Intent i = PollService.newIntent(context);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0 , i, 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if(isOn){
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pendingIntent );
            }else{
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }

        QueryPreferences.setPrefIsAlarmOn(context,isOn);

    }

    public static boolean isServiceAlarmOn(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return isJobServiceAlarmOn(context);
        }else{
            Intent i = PollService.newIntent(context);
            PendingIntent pendingIntent = PendingIntent
                    .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
            return pendingIntent!=null;
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isJobServiceAlarmOn(Context context){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;

        for(JobInfo jobInfo : jobScheduler.getAllPendingJobs()){
            if(jobInfo.getId() == JOB_ID){
                hasBeenScheduled = true;
            }
        }

        return hasBeenScheduled;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void setStartJobService(Context context,boolean isOn){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        if(isOn){
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(1000 * 60 * 10)
                    .setPersisted(true)
                    .build();

            jobScheduler.schedule(jobInfo);
        }else{
            jobScheduler.cancel(JOB_ID);
        }

    }
}
