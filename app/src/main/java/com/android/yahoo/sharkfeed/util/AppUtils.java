package com.android.yahoo.sharkfeed.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.yahoo.sharkfeed.R;

/**
 * Created by sai pranesh on 6/14/2017.
 */

public class AppUtils {

    //scales the bitmap to the required desired width and desired height
    public static Bitmap scaledBitmap(byte[] byteArray, int destWidth, int destHeight){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        //Figuring out how much to scale down
        int inSampleSize = 1;
        if( srcHeight > destHeight || srcWidth > destWidth){
            if(srcWidth > srcHeight){
                inSampleSize = Math.round(srcHeight / destHeight);
            }else{
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = (int) (inSampleSize*1.2);

        //read in and create final bitmap
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

    }

    public static void hideKeyboard(Context context, View view) {
        if (context == null) return;
        InputMethodManager inputMethodManager= (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isNetworkAvailableAndConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void showSnackBarNetworkConnection(Context context, View view){
        if(!isNetworkAvailableAndConnected(context)){
            final Snackbar snackbar = Snackbar.make(view, R.string.no_internet_connection,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setActionTextColor(context.getResources().getColor(R.color.colorPrimary))
                    .setAction(R.string.dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    }).show();
        }
    }




}
