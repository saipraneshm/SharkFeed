package com.android.yahoo.sharkfeed.fragment;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.util.HighQualityImageDownloader;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightBoxFragment extends Fragment {


    private String mDownloadUrlC;
    private String mDownloadUrlL;
    private String mDownloadUrlO;

    private static final String TAG = LightBoxFragment.class.getSimpleName();
    public static final String DOWNLOAD_URL_C = "LightBoxFragment.DOWNLOAD_URL_C";
    public static final String DOWNLOAD_URL_L = "LightBoxFragment.DOWNLOAD_URL_L";
    public static final String DOWNLOAD_URL_O = "LightBoxFragment.DOWNLOAD_URL_O";

    private HighQualityImageDownloader<ImageView> mImageDownloader;

    private ImageView mImageView;
    private ProgressBar mProgressBar;

    public LightBoxFragment() {
        // Required empty public constructor
    }

    public static LightBoxFragment newInstance(String url_c, String url_l, String url_o){
        Bundle args = new Bundle();
        args.putString(DOWNLOAD_URL_C, url_c);
        args.putString(DOWNLOAD_URL_L, url_l);
        args.putString(DOWNLOAD_URL_O, url_o);

        LightBoxFragment lightBoxFragment = new LightBoxFragment();
        lightBoxFragment.setArguments(args);
        return lightBoxFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mDownloadUrlC = getArguments().getString(DOWNLOAD_URL_C);
        mDownloadUrlL = getArguments().getString(DOWNLOAD_URL_L);
        mDownloadUrlO = getArguments().getString(DOWNLOAD_URL_O);

        /*Log.d(TAG, "Got request in LightBoxFragment for urL : " +
                "c ->" + mDownloadUrlC +
                "/n L -> " + mDownloadUrlL +
                "/n O -> " + mDownloadUrlO);*/

        Handler responseHandler = new Handler();
        mImageDownloader = new HighQualityImageDownloader<>(responseHandler);
        mImageDownloader.setHQImageDownloadListener(new HighQualityImageDownloader.HighQualityImageDownload<ImageView>() {
            @Override
            public void onImageDownloaded(ImageView target, Bitmap thumbnail) {
                //Only added when the attached to the activity
                if(isAdded()){
                    Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                    target.setImageDrawable(drawable);
                //    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mImageDownloader.start();
        mImageDownloader.getLooper();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light_box, container, false);
        mImageView = (ImageView) view.findViewById(R.id.highQualityImageView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

     //   mProgressBar.setVisibility(View.VISIBLE);

        if(mDownloadUrlC != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlC);
        }

        if(mDownloadUrlL != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlL);
        }

        if(mDownloadUrlO != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlO);
        }

        return view;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageDownloader.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mImageDownloader.clearQueue();
    }
}
