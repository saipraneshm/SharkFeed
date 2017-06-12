package com.android.yahoo.sharkfeed.util;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sai pranesh on 6/12/2017.
 */

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = ThumbnailDownloader.class.getSimpleName();

    private Handler mRequestHandler;

    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    public ThumbnailDownloader() {
        super(TAG);
    }


    public void queueThumbnail(T target, String url){
        if(url == null){
            mRequestMap.remove(target);
        }else{

        }
    }
}
