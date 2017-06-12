package com.android.yahoo.sharkfeed.util;

import android.net.Uri;
import android.util.Log;

import com.android.yahoo.sharkfeed.model.Photo;
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
 * Created by sai pranesh on 6/12/2017.
 */

public class FlickrFetcher {

    private static final String TAG = FlickrFetcher.class.getSimpleName();
    private static final String API_KEY = "949e98778755d1982f537d56236bbb42";



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

    public static String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<Photo> fetchItems(int page){

        List<Photo> photoList = new ArrayList<>();
        try{
            String url = Uri
                    .parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method","flickr.photos.search")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("text","shark")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("page", String.valueOf(page))
                    .appendQueryParameter("extras", "url_s,url_t,url_c,url_l,url_o")
                    .build()
                    .toString();

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


    //Using GSON to parse the items.
    private void parseItems(List<Photo> photoList, String jsonString){
        Gson gson = new Gson();
        PhotoParent photoParent = gson.fromJson(jsonString, PhotoParent.class);
        List<Photo> listOfPhotos = photoParent.getPhotos().getPhoto();
        photoList.addAll(listOfPhotos);
    }

}
