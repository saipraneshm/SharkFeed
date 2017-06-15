package com.android.yahoo.sharkfeed.activity;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.activity.abs.SingleFragmentActivity;
import com.android.yahoo.sharkfeed.fragment.LightBoxFragment;
import com.android.yahoo.sharkfeed.model.Photo;

/**
 * Created by sai pranesh on 6/13/2017.
 */

public class LightBoxActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        if(getSupportActionBar()!= null && getSupportActionBar().isShowing()){
            getSupportActionBar().hide();
        }
        return null;
       /* Photo photo = getIntent().getParcelableExtra(LightBoxFragment.DOWNLOAD_PHOTO);
        return LightBoxFragment.newInstance(photo);*/
    }



}
