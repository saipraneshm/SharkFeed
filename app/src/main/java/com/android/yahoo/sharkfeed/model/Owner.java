
package com.android.yahoo.sharkfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Owner implements Parcelable {

    @SerializedName("nsid")
    @Expose
    private String nsid;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("realname")
    @Expose
    private String realname;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("iconserver")
    @Expose
    private String iconserver;
    @SerializedName("iconfarm")
    @Expose
    private Integer iconfarm;
    @SerializedName("path_alias")
    @Expose
    private String pathAlias;

    public String getNsid() {
        return nsid;
    }

    public void setNsid(String nsid) {
        this.nsid = nsid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIconserver() {
        return iconserver;
    }

    public void setIconserver(String iconserver) {
        this.iconserver = iconserver;
    }

    public Integer getIconfarm() {
        return iconfarm;
    }

    public void setIconfarm(Integer iconfarm) {
        this.iconfarm = iconfarm;
    }

    public String getPathAlias() {
        return pathAlias;
    }

    public void setPathAlias(String pathAlias) {
        this.pathAlias = pathAlias;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nsid);
        dest.writeString(this.username);
        dest.writeString(this.realname);
        dest.writeString(this.location);
        dest.writeString(this.iconserver);
        dest.writeValue(this.iconfarm);
        dest.writeString(this.pathAlias);
    }

    public Owner() {
    }

    protected Owner(Parcel in) {
        this.nsid = in.readString();
        this.username = in.readString();
        this.realname = in.readString();
        this.location = in.readString();
        this.iconserver = in.readString();
        this.iconfarm = (Integer) in.readValue(Integer.class.getClassLoader());
        this.pathAlias = in.readString();
    }

    public static final Parcelable.Creator<Owner> CREATOR = new Parcelable.Creator<Owner>() {
        @Override
        public Owner createFromParcel(Parcel source) {
            return new Owner(source);
        }

        @Override
        public Owner[] newArray(int size) {
            return new Owner[size];
        }
    };
}
