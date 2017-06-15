package com.android.yahoo.sharkfeed.fragment;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.model.Photo;
import com.android.yahoo.sharkfeed.model.PhotoInfo;
import com.android.yahoo.sharkfeed.model.PhotoInfoParent;
import com.android.yahoo.sharkfeed.util.FlickrFetcher;
import com.android.yahoo.sharkfeed.util.HighQualityImageDownloader;

import org.w3c.dom.Text;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightBoxFragment extends Fragment {


    private String mDownloadUrlC;
    private String mDownloadUrlL;
    private String mDownloadUrlO;
    private String mDownloadUrlT;
    private Photo mPhoto;
    private String mTransitionName;
    private PhotoInfoParent mPhotoInfoParent = new PhotoInfoParent();

    private static final String TAG = LightBoxFragment.class.getSimpleName();
    public static final String DOWNLOAD_URL_C = "LightBoxFragment.DOWNLOAD_URL_C";
    public static final String DOWNLOAD_URL_L = "LightBoxFragment.DOWNLOAD_URL_L";
    public static final String DOWNLOAD_URL_O = "LightBoxFragment.DOWNLOAD_URL_O";
    public static final String DOWNLOAD_URL_T = "LightBoxFragment.DOWNLOAD_URL_T";
    public static final String DOWNLOAD_PHOTO = "LightBoxFragment.DOWNLOAD_PHOTO";
    public static final String EXTRA_TRANSITION_NAME = "LightBoxFragment.EXTRA_TRANSITION_NAME";

    private static final int REQUEST_IMAGE_DOWNLOAD = 45;

    private HighQualityImageDownloader<ImageView> mImageDownloader;

    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Button mDownloadButton;
    private Button mOpenFlickrButton;
    private LinearLayout mLinearLayout;
    private TextView mPhotoTitleTextView;

    private static boolean isHQImageAvailable = false;

    public LightBoxFragment() {
        // Required empty public constructor
    }

    public static LightBoxFragment newInstance(String url_c, String url_l, String url_o, String url_t){
        Bundle args = new Bundle();
        args.putString(DOWNLOAD_URL_C, url_c);
        args.putString(DOWNLOAD_URL_L, url_l);
        args.putString(DOWNLOAD_URL_O, url_o);
        args.putString(DOWNLOAD_URL_T, url_t);

        LightBoxFragment lightBoxFragment = new LightBoxFragment();
        lightBoxFragment.setArguments(args);
        return lightBoxFragment;
    }

    public static LightBoxFragment newInstance(Photo photo, String transitionName){
        Bundle args = new Bundle();
        args.putParcelable(DOWNLOAD_PHOTO, photo);
        args.putString(EXTRA_TRANSITION_NAME, transitionName);

        LightBoxFragment lightBoxFragment = new LightBoxFragment();
        lightBoxFragment.setArguments(args);
        return lightBoxFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        postponeEnterTransition();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition
                    (TransitionInflater.from(getContext())
                            .inflateTransition(android.R.transition.move));
        }

        mPhoto = getArguments().getParcelable(DOWNLOAD_PHOTO);
        mTransitionName = getArguments().getString(EXTRA_TRANSITION_NAME);

        if(mPhoto != null){
            new FetchPhotoInfo(mPhotoInfoParent).execute(mPhoto.getId());
            mDownloadUrlC = mPhoto.getUrlC();
            mDownloadUrlL = mPhoto.getUrlL();
            mDownloadUrlO = mPhoto.getUrlO();
            mDownloadUrlT = mPhoto.getUrlT();
        }



        if( mDownloadUrlO != null || mDownloadUrlL != null){
            isHQImageAvailable = true;
        }

    Log.d(TAG, "Got request in LightBoxFragment for urL : " +
                "c ->" + mDownloadUrlC +
                "/n L -> " + mDownloadUrlL +
                "/n O -> " + mDownloadUrlO);

        Handler responseHandler = new Handler();
        mImageDownloader = new HighQualityImageDownloader<>(responseHandler);
        mImageDownloader.setHQImageDownloadListener(new HighQualityImageDownloader.HighQualityImageDownload<ImageView>() {
            @Override
            public void onImageDownloaded(ImageView target, Bitmap thumbnail) {
                //Only added when the attached to the activity
                if(isAdded()){
                    Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                    startPostponedEnterTransition();
                   /* Log.d(TAG, " setting high quality image ->"
                            + mPhotoInfoParent.getPhotoInfo().getDescription().getContent());*/
                    target.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    target.setImageDrawable(drawable);
                    target.setTag("HQ");
                    if(mProgressBar.getVisibility() == View.VISIBLE){
                        mProgressBar.setVisibility(View.GONE);
                    }
                    mLinearLayout.setVisibility(View.VISIBLE);
                    setTitleText();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImageView.setTransitionName(mTransitionName);
        }
        //mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mProgressBar = (ProgressBar) view.findViewById(R.id.lightBoxImgProgressBar);
        mDownloadButton = (Button) view.findViewById(R.id.downloadImageBtn);
        mOpenFlickrButton = (Button) view.findViewById(R.id.openFlickrPgBtn);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.photoOptionsLinearLayout);
        mPhotoTitleTextView = (TextView) view.findViewById(R.id.imageTitleTextView);


        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_IMAGE_DOWNLOAD);

                }else{
                  //  Log.d(TAG, " storing data into the device");
                    Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
                  //  Log.d(TAG, "is bitmap null ? " + (bitmap == null));
                    ContentResolver cr = getActivity().getContentResolver();
                    String title = mPhoto.getTitle();
                    String description = mPhoto.getPhotoFileName();
                    String savedUrl =  MediaStore.Images.Media
                            .insertImage(cr, bitmap, title, description);
                    Toast.makeText(getActivity(), " downloaded image into " + savedUrl,
                            Toast.LENGTH_SHORT).show();
                }


            }
        });


        if(mDownloadUrlT != null){
            new BitmapDownloadTask().execute(mDownloadUrlT);
        }else if(mDownloadUrlC != null){
            new BitmapDownloadTask().execute(mDownloadUrlC);
        }

        if(mDownloadUrlO != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlO);
        }else if(mDownloadUrlL != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlL);
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
      //  Log.d(TAG, "inside request Permisson result");
       switch(requestCode){
           case REQUEST_IMAGE_DOWNLOAD:
               if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
                   ContentResolver cr = getActivity().getContentResolver();
                   String title = "first download";
                   String description = "dummy description";
                   String savedUrl =  MediaStore.Images.Media
                           .insertImage(cr, bitmap, title, description);
                   Toast.makeText(getActivity(), " downloaded image into " + savedUrl,
                           Toast.LENGTH_SHORT).show();
               }else{
                   Log.d(TAG ,"permission denied");
               }
               break;
       }



    }

    private class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap>{


        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            try {
                Log.d(TAG, "got request for url" + url[0]);
                byte[] bitmapBytes = FlickrFetcher.getUrlBytes(url[0]);
                return BitmapFactory.decodeByteArray(bitmapBytes, 0 , bitmapBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if(mImageView.getTag()== null){
                Log.d(TAG, "setting low quality image" + mImageView.getTag());
                mImageView.setImageBitmap(bitmap);
                startPostponedEnterTransition();
            }
            if(!isHQImageAvailable){
                mProgressBar.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                setTitleText();
            }

        }
    }

    private void setTitleText(){
        PhotoInfo photoInfo = mPhotoInfoParent.getPhotoInfo();
        if(photoInfo != null){
            mPhotoTitleTextView.setText(photoInfo.getTitle().getContent());
        }
    }

    private class FetchPhotoInfo extends AsyncTask<String, Void ,Void>{

        private PhotoInfoParent mPhotoInfoParent;

        FetchPhotoInfo(PhotoInfoParent photoInfoParent){
            mPhotoInfoParent = photoInfoParent;
        }

        @Override
        protected Void doInBackground(String... photoId) {
            mPhotoInfoParent
                    .setPhotoInfo(new FlickrFetcher().fetchPhotoInfo(photoId[0]));
            return null;
        }
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
