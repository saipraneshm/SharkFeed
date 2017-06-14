package com.android.yahoo.sharkfeed.activity;

import android.support.v4.app.Fragment;

import com.android.yahoo.sharkfeed.activity.abs.SingleFragmentActivity;
import com.android.yahoo.sharkfeed.fragment.LightBoxFragment;

/**
 * Created by sai pranesh on 6/13/2017.
 */

public class LightBoxActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        if(getSupportActionBar()!= null && getSupportActionBar().isShowing()){
            getSupportActionBar().hide();
        }
        String downloadUrlC = getIntent().getStringExtra(LightBoxFragment.DOWNLOAD_URL_C);
        String downloadUrlL = getIntent().getStringExtra(LightBoxFragment.DOWNLOAD_URL_L);
        String downloadUrlO = getIntent().getStringExtra(LightBoxFragment.DOWNLOAD_URL_O);
        return LightBoxFragment.newInstance(downloadUrlC, downloadUrlL, downloadUrlO);
    }

}
