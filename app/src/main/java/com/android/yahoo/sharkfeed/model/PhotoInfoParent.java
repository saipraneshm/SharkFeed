package com.android.yahoo.sharkfeed.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by sai pranesh on 6/15/2017.
 */

public class PhotoInfoParent {

    @SerializedName("photo")
    @Expose
    private PhotoInfo mPhotoInfo;

    public PhotoInfo getPhotoInfo() {
        return mPhotoInfo;
    }

    public void setPhotoInfo(PhotoInfo photoInfo) {
        mPhotoInfo = photoInfo;
    }
}
