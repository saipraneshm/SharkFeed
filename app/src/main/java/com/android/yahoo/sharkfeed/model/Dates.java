
package com.android.yahoo.sharkfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dates implements Parcelable {

    @SerializedName("posted")
    @Expose
    private String posted;
    @SerializedName("taken")
    @Expose
    private String taken;
    @SerializedName("takengranularity")
    @Expose
    private Integer takengranularity;
    @SerializedName("takenunknown")
    @Expose
    private Integer takenunknown;
    @SerializedName("lastupdate")
    @Expose
    private String lastupdate;

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getTaken() {
        return taken;
    }

    public void setTaken(String taken) {
        this.taken = taken;
    }

    public Integer getTakengranularity() {
        return takengranularity;
    }

    public void setTakengranularity(Integer takengranularity) {
        this.takengranularity = takengranularity;
    }

    public Integer getTakenunknown() {
        return takenunknown;
    }

    public void setTakenunknown(Integer takenunknown) {
        this.takenunknown = takenunknown;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.posted);
        dest.writeString(this.taken);
        dest.writeValue(this.takengranularity);
        dest.writeValue(this.takenunknown);
        dest.writeString(this.lastupdate);
    }

    public Dates() {
    }

    protected Dates(Parcel in) {
        this.posted = in.readString();
        this.taken = in.readString();
        this.takengranularity = (Integer) in.readValue(Integer.class.getClassLoader());
        this.takenunknown = (Integer) in.readValue(Integer.class.getClassLoader());
        this.lastupdate = in.readString();
    }

    public static final Parcelable.Creator<Dates> CREATOR = new Parcelable.Creator<Dates>() {
        @Override
        public Dates createFromParcel(Parcel source) {
            return new Dates(source);
        }

        @Override
        public Dates[] newArray(int size) {
            return new Dates[size];
        }
    };
}
