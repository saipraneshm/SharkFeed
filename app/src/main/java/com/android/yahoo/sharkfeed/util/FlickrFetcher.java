package com.android.yahoo.sharkfeed.util;

import android.net.Uri;
import android.util.Log;

import com.android.yahoo.sharkfeed.model.Photo;
import com.android.yahoo.sharkfeed.model.PhotoInfo;
import com.android.yahoo.sharkfeed.model.PhotoInfoParent;
import com.android.yahoo.sharkfeed.model.PhotoParent;
import com.android.yahoo.sharkfeed.model.Photos;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to communicate with the FlickrApi.
 */

public class FlickrFetcher {

    private static final String TAG = FlickrFetcher.class.getSimpleName();
    private static final String API_KEY = "949e98778755d1982f537d56236bbb42";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("method","flickr.photos.search")
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s,url_t,url_c,url_l,url_o")
            .build();



    //Converts the give url to bytes array which contains the requested url page data
    public static byte[] getUrlBytes(String urlSpec) throws IOException{

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()
                        + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = inputStream.read(buffer))> 0){
                out.write(buffer, 0 , bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }

    }

    //A helper method which returns the string value of the data downloaded using getUrlBytes(url)
    public static String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //fetches items and returns the downloaded list of photos
    private List<Photo> fetchItems(String url){
        List<Photo> photoList = new ArrayList<>();
        try{
            String jsonString = getUrlString(url);
            Log.d(TAG, "Received JSON: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(photoList, jsonObject.toString());

        }catch (JSONException e) {
            Log.e(TAG, "Failed to parse json", e);
        }catch(IOException ioe){
            Log.e(TAG, "Failed to fetch items: ", ioe);
        }
        return photoList;
    }



    //Builds the url based on the query and the page passed in to the method
    private String buildUrl(String query, int page){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon();
        if(query == null){
            uriBuilder.appendQueryParameter("text", "shark");
        }else{
            uriBuilder.appendQueryParameter("text", "shark " + query);
        }

        return uriBuilder.appendQueryParameter("page", String.valueOf(page)).build().toString();
    }

    //Interface using which we fetch shark photos
    public List<Photo> fetchSharkPhotos(int page){
        String url = buildUrl(null, page);
        return fetchItems(url);
    }

    //Interface using which we fetch shark related photos, we append the query along with shark.
    public List<Photo> searchSharkPhotos(String query , int page){
        String url = buildUrl(query, page);
        Log.d(TAG, "search shark photos url : " + url);
        return fetchItems(url);
    }


    //Using GSON to parse the items.
    private void parseItems(List<Photo> photoList, String jsonString){
        Gson gson = new Gson();
        PhotoParent photoParent = gson.fromJson(jsonString, PhotoParent.class);
        List<Photo> listOfPhotos = photoParent.getPhotos().getPhoto();
        photoList.addAll(listOfPhotos);
    }

    //Method that helps to download the photo information
    public PhotoInfo fetchPhotoInfo(String photoId){

        String url = buildUrl(photoId);
        try {
            String jsonString = getUrlString(url);
            return parsePhotoInfo(jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Unable to load photo info for photo id :" + photoId, e );
        }
        return null;
    }

    //constructs url based on the photoId passed.
    private String buildUrl(String photoId){
        return Uri
                .parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("method","flickr.photos.getInfo")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("photo_id", photoId)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }

    //Parses the photoInfo obtained from the FlickrApi
    private PhotoInfo parsePhotoInfo(String jsonString){
        Gson gson = new Gson();
        PhotoInfoParent photoInfoParent = gson.fromJson(jsonString, PhotoInfoParent.class);
        return photoInfoParent.getPhotoInfo();
    }

}
