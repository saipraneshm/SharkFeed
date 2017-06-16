package com.android.yahoo.sharkfeed.util;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.activity.SharkFeedGalleryActivity;
import com.android.yahoo.sharkfeed.model.Photo;

import java.util.List;

import static com.android.yahoo.sharkfeed.util.PollService.ACTION_SHOW_NOTIFICATION;
import static com.android.yahoo.sharkfeed.util.PollService.NOTIFICATION;
import static com.android.yahoo.sharkfeed.util.PollService.PERM_PRIVATE;
import static com.android.yahoo.sharkfeed.util.PollService.REQUEST_CODE;

/**
 * Another implementation of PollService using JobService
 * This is scheduled using JobScheduler only for devices running Lollipop and above.
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {

    private PollTask mCurrentTask;
    private static final String TAG = PollJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if(mCurrentTask!= null){
            mCurrentTask.cancel(true);
        }
        return true;
    }


    private class PollTask extends AsyncTask<JobParameters, Void, Void>{

        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParameters = params[0];

            String query = QueryPreferences.getStoredQuery(PollJobService.this);
            String lastResultId = QueryPreferences.getLastResultId(PollJobService.this);
            List<Photo> photosList;

            if(query == null){
                photosList = new FlickrFetcher().fetchSharkPhotos(0);
            }else{
                photosList = new FlickrFetcher().searchSharkPhotos(query, 0);
            }

            if(photosList.size() == 0){
                return null;
            }

            String resultId = photosList.get(0).getId();
            if(resultId.equals(lastResultId)){
                Log.d(TAG, "got an old result");
            }else{
                Log.d(TAG, "got a new result");

                Resources resources = getResources();
                Intent i = SharkFeedGalleryActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i , 0);

                Notification notification =new  NotificationCompat.Builder(PollJobService.this)
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                showBackgroundNotification(0, notification);
            }

            QueryPreferences.setLastResultId(PollJobService.this, resultId);

            jobFinished(jobParameters, false);
            return null;
        }

        private void showBackgroundNotification(int requestCode, Notification notification){
            Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
            i.putExtra(REQUEST_CODE, requestCode);
            i.putExtra(NOTIFICATION, notification);
            sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
        }
    }

}
