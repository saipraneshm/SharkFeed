package com.android.yahoo.sharkfeed.fragment;


import android.Manifest;
import android.content.ContentResolver;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.model.Photo;
import com.android.yahoo.sharkfeed.model.PhotoInfo;
import com.android.yahoo.sharkfeed.model.PhotoInfoParent;
import com.android.yahoo.sharkfeed.util.AppUtils;
import com.android.yahoo.sharkfeed.util.FlickrFetcher;
import com.android.yahoo.sharkfeed.util.HighQualityImageDownloader;

import java.io.IOException;

/**
 * This fragment is responsible to show the images to the user and provides them option to,
 * download the image and view them in flickr page, along the additional information about the
 * picture to the user.
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

    public static final String EXTRA_DOWNLOAD_PHOTO = "LightBoxFragment.EXTRA_DOWNLOAD_PHOTO";
    public static final String EXTRA_TRANSITION_NAME = "LightBoxFragment.EXTRA_TRANSITION_NAME";
    public static final String DIALOG_PHOTO_INFO = "LightBoxFragment.DIALOG_PHOTO_INFO";

    private static final int REQUEST_IMAGE_DOWNLOAD = 45;

    //Handler thread
    private HighQualityImageDownloader<ImageView> mImageDownloader;

    private ImageView mImageView, mPhotoInfoImageView;
    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayout;
    private TextView mPhotoTitleTextView;
    private FrameLayout mFrameLayout;

    private static boolean isHQImageAvailable = false;

    public LightBoxFragment() {
        // Required empty public constructor
    }


    //An interface to get an instance of fragment
    public static LightBoxFragment newInstance(Photo photo, String transitionName){
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DOWNLOAD_PHOTO, photo);
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

        mPhoto = getArguments().getParcelable(EXTRA_DOWNLOAD_PHOTO);
        mTransitionName = getArguments().getString(EXTRA_TRANSITION_NAME);

        if(AppUtils.isNetworkAvailableAndConnected(getActivity())){
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

            Handler responseHandler = new Handler();
            mImageDownloader = new HighQualityImageDownloader<>(responseHandler);
            mImageDownloader.setHQImageDownloadListener(new HighQualityImageDownloader.HighQualityImageDownload<ImageView>() {
                @Override
                public void onImageDownloaded(ImageView target, Bitmap thumbnail) {
                    //Only added when the attached to the activity
                    if(isAdded()){
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        startPostponedEnterTransition();
                        target.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        target.setImageDrawable(drawable);
                        target.setTag("HQ");
                        updateLayoutAfterDownload();

                    }
                }
            });
            mImageDownloader.start();
            mImageDownloader.getLooper();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //Displaying the snackbar in-case there is no network.
        AppUtils.showSnackBarNetworkConnection(getActivity(),mFrameLayout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light_box, container, false);
        mImageView = (ImageView) view.findViewById(R.id.high_quality_image_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.light_box_image_progress_bar);
        Button downloadButton = (Button) view.findViewById(R.id.download_image_button);
        Button openFlickrButton = (Button) view.findViewById(R.id.open_flickr_page_button);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.photo_options_linear_layout);
        mPhotoTitleTextView = (TextView) view.findViewById(R.id.image_title_text_view);
        mPhotoInfoImageView = (ImageView) view.findViewById(R.id.photo_info_image_view);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.light_box_fragment_frame_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImageView.setTransitionName(mTransitionName);
        }

        openFlickrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, mPhoto.getPhotoPageUri());
                startActivity(intent);
            }
        });

        mPhotoInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                PhotoInfoDialogFragment dialog = PhotoInfoDialogFragment
                        .newInstance(mPhotoInfoParent.getPhotoInfo());
                dialog.show(fragmentManager, DIALOG_PHOTO_INFO);
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_IMAGE_DOWNLOAD);

                }else{
                    saveBitmapToPictures();
                }


            }
        });


        //Initially loading the low quality images using AysncTask
        if(mDownloadUrlT != null){
            new BitmapDownloadTask().execute(mDownloadUrlT);
        }else if(mDownloadUrlC != null){
            new BitmapDownloadTask().execute(mDownloadUrlC);
        }

        //Loading and Scaling the images to fit the screen using handler thread
        if(mDownloadUrlO != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlO);
        }else if(mDownloadUrlL != null){
            mImageDownloader.enqueueHQImageDownload(mImageView, mDownloadUrlL);
        }

        return view;
    }

    //Saves the bitmap to pictures directory of the phone.
    private void saveBitmapToPictures(){
        Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        ContentResolver cr = getActivity().getContentResolver();
        String title = mPhoto.getTitle();
        String description = mPhoto.getPhotoFileName();
        final String savedUrl =  MediaStore.Images.Media
                .insertImage(cr, bitmap, title, description);
        Snackbar.make(mFrameLayout, R.string.image_downloaded,Snackbar.LENGTH_SHORT)
                .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                .setAction(R.string.view_pictures, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_VIEW)
                                .setData(Uri.parse(savedUrl));
                        startActivity(i);

                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

       switch(requestCode){
           case REQUEST_IMAGE_DOWNLOAD:
               if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                  saveBitmapToPictures();
               }else{
                   Log.d(TAG ,"permission denied");
               }
               break;
       }



    }

    //AsyncTask to download low quality image
    private class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            try {
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
                mImageView.setImageBitmap(bitmap);
                startPostponedEnterTransition();
            }
            if(!isHQImageAvailable){
                updateLayoutAfterDownload();
            }

        }
    }

    //Displaying photo's title to the user
    private void setTitleText(){
        PhotoInfo photoInfo = mPhotoInfoParent.getPhotoInfo();
        if(photoInfo != null){
            String title = photoInfo.getTitle().getContent();
            if(title == null || title.length()== 0 ){
                title = getResources().getString(R.string.click_on_info_btn);
            }
            if(title.length() > 120){
                title = title.substring(0,120) + "...";
            }
            mPhotoTitleTextView.setText(title);
        }
    }

    //Handling progress bar and other layout visibility
    private void updateLayoutAfterDownload(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.GONE);
        }
        mLinearLayout.setVisibility(View.VISIBLE);
        mPhotoInfoImageView.setVisibility(View.VISIBLE);
        setTitleText();
    }

    //Downloads the photo information
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


    //Destroying the thread when the fragment gets destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageDownloader.quit();
    }

    //Clearing the contents of the queue when the fragments view gets destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mImageDownloader.clearQueue();
    }

}
