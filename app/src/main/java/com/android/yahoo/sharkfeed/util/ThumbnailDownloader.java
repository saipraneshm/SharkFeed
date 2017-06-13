package com.android.yahoo.sharkfeed.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sai pranesh on 6/12/2017.
 */

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = ThumbnailDownloader.class.getSimpleName();
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private Handler mResponseHandler;

    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    //Using LRU cache to cache bitmaps
    private LruCache<String, Bitmap> mMemoryCache;

    private ThumbnailDownloadListener mThumbnailDownloadListener;

    public ThumbnailDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;
        final int maxMemory = (int) Runtime.getRuntime().maxMemory(); //getting the max memory

        final int cacheSize = maxMemory / 8; //using 1/8th memory for the cache

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024; //using kilobytes to calculate the cache size
            }
        };
    }

    public void addBitmapToMemoryCache(String target, Bitmap bitmap){
        if(getBitmapFromMemoryCache(target) == null){
            mMemoryCache.put(target, bitmap);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String target){
        return mMemoryCache.get(target);
    }

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.d(TAG, " Got a request for url: " + mRequestMap.get(target) );
                    handleRequest(target);
                }
            }
        };
    }

    void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);

            if(url == null) return;
            Bitmap bitmap = getBitmapFromMemoryCache(url);

            if(bitmap == null){
                byte[] bitmapBytes = FlickrFetcher.getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                addBitmapToMemoryCache(url,bitmap);
            }
            final Bitmap postBitmap = bitmap;
            //final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.d(TAG, "Bitmap has been created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != url) return;
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, postBitmap);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error downloading image ", e);
        }
    }

    public void queueThumbnail(T target, String url){
        if(url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
