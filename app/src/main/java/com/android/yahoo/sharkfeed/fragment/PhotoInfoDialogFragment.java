package com.android.yahoo.sharkfeed.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.model.PhotoInfo;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PhotoInfoDialogFragment extends DialogFragment {

    public static final String EXTRA_PHOTO_INFO = "PhotoInfoDialogFragment.EXTRA_PHOTO_INFO";

    private static final String TAG = PhotoInfoDialogFragment.class.getSimpleName();

    private PhotoInfo mPhotoInfo;

    private TextView mOwnerTv, mTitleTv, mDescTv, mViewsTv, mDatesTv;

    private String mOwner, mTitle, mDesc , mViews , mDates;

    public static PhotoInfoDialogFragment newInstance(PhotoInfo photoInfo){
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PHOTO_INFO, photoInfo);

        PhotoInfoDialogFragment dialogFragment = new PhotoInfoDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mPhotoInfo = getArguments().getParcelable(EXTRA_PHOTO_INFO);


        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View view =  inflater.inflate(R.layout.dialog_photo_info,null);
        mOwnerTv = (TextView) view.findViewById(R.id.owner_text_view);
        mTitleTv = (TextView) view.findViewById(R.id.title_text_view);
        mDescTv  = (TextView) view.findViewById(R.id.desc_text_view);
        mViewsTv = (TextView) view.findViewById(R.id.views_text_view);
        mDatesTv = (TextView) view.findViewById(R.id.date_created_text_view);

        fillTextViewContent();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.photo_info_title)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }


    private void fillTextViewContent(){
        if(mPhotoInfo != null){
            if(mPhotoInfo.getOwner().getRealname()!= null){
                mOwner = mPhotoInfo.getOwner().getRealname();
            }else if(mPhotoInfo.getOwner().getUsername() != null){
                mOwner = mPhotoInfo.getOwner().getUsername();
            }

            if(mOwner != null && mOwner.length() > 0){
                mOwnerTv.setText(mOwner);
            }else{
                mOwnerTv.setText(R.string.info_not_available);
            }

            if(mPhotoInfo.getTitle() != null){
                mTitle = mPhotoInfo.getTitle().getContent();
            }

            if(mTitle != null && mTitle.length() > 0){
                mTitleTv.setText(mTitle);
            }else{
                mTitleTv.setText(R.string.info_not_available);
            }

            if(mPhotoInfo.getDescription() != null){
                mDesc = mPhotoInfo.getDescription().getContent();
            }

            if(mDesc != null && mDesc.length() > 0){
                mDescTv.setText(mDesc);
            }else{
                mDescTv.setText(R.string.info_not_available);
            }

            if(mPhotoInfo.getDateuploaded() != null){
                mDates = mPhotoInfo.getDateuploaded();
            }

            if(mDates != null && mDates.length() > 0){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                Date date = new Date(System.currentTimeMillis() - Long.parseLong(mDates));
                mDatesTv.setText(simpleDateFormat.format(date));
            }else{
                mDatesTv.setText(R.string.info_not_available);
            }

            if(mPhotoInfo.getViews() != null){
                mViews = mPhotoInfo.getViews();
            }

            if(mViews != null && mViews.length() > 0){
                mViewsTv.setText(mViews);
            }else{
                mViewsTv.setText(R.string.info_not_available);
            }
        }
    }
}
