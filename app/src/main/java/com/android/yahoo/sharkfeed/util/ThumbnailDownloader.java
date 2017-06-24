package com.android.yahoo.sharkfeed.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.BuildConfig;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Thumbnail downloader is used for
 * 1. Loading images and updating the UI using the Main thread handler
 * 2. Pre-fetching of images and saving them in the cache for faster access of images.
 * 3. Maintaining a LRU cache for caching purposes that occupies 1/8th of available memory for storage.
 * 4. Use of disk cache in-case memory-cache is full, for better performance.
 */

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = ThumbnailDownloader.class.getSimpleName();
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;


    private Handler mRequestHandler;
    private Handler mResponseHandler;

    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    //Using LRU cache to cache thumbnail bitmaps
    private LruCache<String, Bitmap> mMemoryCache;

    //Adding disk cache in-case memory cache fails during runtime configuration changes
    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;  //10MB
    private boolean mDiskCacheStarting = true;
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    private static final int DISK_CACHE_INDEX = 0;


    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public ThumbnailDownloader(Context context, Handler handler) {
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

       File cacheDir = getDiskCacheDir(context , DISK_CACHE_SUBDIR);
       new InitDiskCacheTask().execute(cacheDir);
    }

    private class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                File cacheDir = params[0];
                try {
                    mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1,DISK_CACHE_SIZE);
                } catch (IOException e) {
                    Log.e(TAG, " Failed to initialize disk cache", e);
                }
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads

            }
            return null;
        }
    }




    /**
     * Adds a bitmap to both memory and disk cache.
     * @param data Unique identifier for the bitmap to store
     * @param value The bitmap drawable to store
     */
    private void addBitmapToCache(String data, Bitmap value) {
        //BEGIN_INCLUDE(add_bitmap_to_cache)
        if (data == null || value == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null) {
            if((data.length() + mMemoryCache.size()) < mMemoryCache.maxSize())
                mMemoryCache.put(data, value);
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                }  finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG,"unable to close stream: ", e);
                    }
                }
            }
        }
        //END_INCLUDE(add_bitmap_to_cache)
    }

    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    private Bitmap getBitmapFromDiskCache(String data) {
        //BEGIN_INCLUDE(get_bitmap_from_disk_cache)
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Disk cache error: " , e);
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = BitmapFactory.decodeFileDescriptor(fd);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "bitmap cache disk problem: ", e);
                    }
                }
            }
            return bitmap;
        }
        //END_INCLUDE(get_bitmap_from_disk_cache)
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    private static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private Bitmap getBitmapFromMemoryCache(String target){
        return mMemoryCache.get(target);
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    private File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }


    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                   // Log.d(TAG, " Got a request for url: " + mRequestMap.get(target) );
                    handleRequest(target);
                }else if(msg.what == MESSAGE_PRELOAD){
                    String url = (String) msg.obj;
                    preloadBitmapsIntoCache(url);
                }
            }
        };
    }

    private void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);

            if(url == null) return;
            Bitmap bitmap = getBitmapFromMemoryCache(url);

            if(bitmap == null){
                bitmap = getBitmapFromDiskCache(url);
                if(bitmap == null){
                    byte[] bitmapBytes = FlickrFetcher.getUrlBytes(url);
                    bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    Log.d(TAG, "Bitmap has been created");
                    addBitmapToCache(url,bitmap);
                }
            }
            final Bitmap postBitmap = bitmap;
            //final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);


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

    private void preloadBitmapsIntoCache(String url){
        try{

            if(url == null) return;
            Bitmap bitmap = getBitmapFromMemoryCache(url);

            if(bitmap == null){
                bitmap = getBitmapFromDiskCache(url);
                if(bitmap == null){
                    byte[] bitmapBytes = FlickrFetcher.getUrlBytes(url);
                    bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    addBitmapToCache(url,bitmap);
                }
            }
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

    public void preload(String url){
        if(url != null){
            mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestHandler.removeMessages(MESSAGE_PRELOAD);
    }
}
