
package com.android.yahoo.sharkfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoInfo implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("secret")
    @Expose
    private String secret;
    @SerializedName("server")
    @Expose
    private String server;
    @SerializedName("farm")
    @Expose
    private Integer farm;
    @SerializedName("dateuploaded")
    @Expose
    private String dateuploaded;
    @SerializedName("isfavorite")
    @Expose
    private Integer isfavorite;
    @SerializedName("license")
    @Expose
    private Integer license;
    @SerializedName("safety_level")
    @Expose
    private Integer safetyLevel;
    @SerializedName("rotation")
    @Expose
    private Integer rotation;
    @SerializedName("owner")
    @Expose
    private Owner owner;
    @SerializedName("title")
    @Expose
    private Title title;
    @SerializedName("description")
    @Expose
    private Description description;
    @SerializedName("dates")
    @Expose
    private Dates dates;
    @SerializedName("views")
    @Expose
    private String views;

    @SerializedName("media")
    @Expose
    private String media;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getFarm() {
        return farm;
    }

    public void setFarm(Integer farm) {
        this.farm = farm;
    }

    public String getDateuploaded() {
        return dateuploaded;
    }

    public void setDateuploaded(String dateuploaded) {
        this.dateuploaded = dateuploaded;
    }

    public Integer getIsfavorite() {
        return isfavorite;
    }

    public void setIsfavorite(Integer isfavorite) {
        this.isfavorite = isfavorite;
    }

    public Integer getLicense() {
        return license;
    }

    public void setLicense(Integer license) {
        this.license = license;
    }

    public Integer getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(Integer safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Dates getDates() {
        return dates;
    }

    public void setDates(Dates dates) {
        this.dates = dates;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.secret);
        dest.writeString(this.server);
        dest.writeValue(this.farm);
        dest.writeString(this.dateuploaded);
        dest.writeValue(this.isfavorite);
        dest.writeValue(this.license);
        dest.writeValue(this.safetyLevel);
        dest.writeValue(this.rotation);
        dest.writeParcelable(this.owner, flags);
        dest.writeParcelable(this.title, flags);
        dest.writeParcelable(this.description, flags);
        dest.writeParcelable(this.dates, flags);
        dest.writeString(this.views);
        dest.writeString(this.media);
    }

    public PhotoInfo() {
    }

    protected PhotoInfo(Parcel in) {
        this.id = in.readString();
        this.secret = in.readString();
        this.server = in.readString();
        this.farm = (Integer) in.readValue(Integer.class.getClassLoader());
        this.dateuploaded = in.readString();
        this.isfavorite = (Integer) in.readValue(Integer.class.getClassLoader());
        this.license = (Integer) in.readValue(Integer.class.getClassLoader());
        this.safetyLevel = (Integer) in.readValue(Integer.class.getClassLoader());
        this.rotation = (Integer) in.readValue(Integer.class.getClassLoader());
        this.owner = in.readParcelable(Owner.class.getClassLoader());
        this.title = in.readParcelable(Title.class.getClassLoader());
        this.description = in.readParcelable(Description.class.getClassLoader());
        this.dates = in.readParcelable(Dates.class.getClassLoader());
        this.views = in.readString();
        this.media = in.readString();
    }

    public static final Parcelable.Creator<PhotoInfo> CREATOR = new Parcelable.Creator<PhotoInfo>() {
        @Override
        public PhotoInfo createFromParcel(Parcel source) {
            return new PhotoInfo(source);
        }

        @Override
        public PhotoInfo[] newArray(int size) {
            return new PhotoInfo[size];
        }
    };
}
