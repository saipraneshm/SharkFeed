package com.android.yahoo.sharkfeed.util;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;


import java.io.IOException;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by sai pranesh on 6/13/2017.
 * Created another HandlerThread subclass, to handle High Quality image download : instead of using
 * the same thumbnailDownloader for clarity purpose and for better performance
 * It also scales down the image to fit the screen using AppUtils.scaledBitmap
 */

public class HighQualityImageDownloader<T> extends HandlerThread {

    private static final String TAG = HighQualityImageDownloader.class.getSimpleName();
    private static final int MESSAGE_HQ_IMAGE_DOWNLOAD = 5;


    private Handler mRequestHandler;
    private Handler mResponseHandler;

    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    //Using LRU cache to cache HighQuality bitmaps
    private LruCache<String, Bitmap> mMemCacheForHQImg;



    private HighQualityImageDownload<T> mHQImageDownloadListener;

    public HighQualityImageDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;

        final int maxMemory = (int) Runtime.getRuntime().maxMemory(); //getting the max memory

        final int cacheSize = maxMemory / 8; //using 1/8th memory for the cache

        mMemCacheForHQImg = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return (value.getByteCount() / 1024);
            }
        };

    }



    private void addBitmapToMemoryCache(String target, Bitmap bitmap){
        if(getBitmapFromMemoryCache(target) == null){
            mMemCacheForHQImg.put(target, bitmap);
        }
    }



    private Bitmap getBitmapFromMemoryCache(String target){
        return mMemCacheForHQImg.get(target);
    }


    public interface HighQualityImageDownload<T>{
        void onImageDownloaded(T target, Bitmap thumbnail);
    }

    public void setHQImageDownloadListener(HighQualityImageDownload<T> HQImageDownloadListener) {
        mHQImageDownloadListener = HQImageDownloadListener;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_HQ_IMAGE_DOWNLOAD){
                    Log.d(TAG," handling request");
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }
        };
    }


    private void handleRequest(final T target){
        try{

            final String url = mRequestMap.get(target);
           /* Log.d(TAG, "handling request for url : " + url);
            Log.d(TAG," target height and width: " + ((ImageView) target).getHeight() +
                    " " + ((ImageView) target).getWidth() );*/
            if(url == null) return;
            Bitmap bitmap = getBitmapFromMemoryCache(url);


            if(bitmap == null){
                byte[] bitmapBytes = FlickrFetcher.getUrlBytes(url);
                //Scaling the picture according to the imageView height and width
                bitmap = AppUtils.scaledBitmap(bitmapBytes,((ImageView) target).getWidth()
                        , ((ImageView) target).getHeight() );
              //  bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                Log.d(TAG, "Bitmap has been created");
                addBitmapToMemoryCache(url,bitmap);
            }

            final Bitmap postBitmap = bitmap;

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                 //   if(mRequestMap.get(target) != url) return;
                    mRequestMap.remove(target);
                    mHQImageDownloadListener.onImageDownloaded(target, postBitmap);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error downloading image ", e);
        }
    }

    public void enqueueHQImageDownload(T target, String url){
        if(url == null){
            mRequestMap.remove(target);
        }else{
            Log.d(TAG, "enqueuing url :" + url + " " + target );
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_HQ_IMAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_HQ_IMAGE_DOWNLOAD);
    }
}
