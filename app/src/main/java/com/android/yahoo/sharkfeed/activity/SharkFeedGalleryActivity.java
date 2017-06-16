package com.android.yahoo.sharkfeed.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.android.yahoo.sharkfeed.fragment.SharkFeedGalleryFragment;
import com.android.yahoo.sharkfeed.activity.abs.SingleFragmentActivity;

public class SharkFeedGalleryActivity extends SingleFragmentActivity {


    public static Intent newIntent(Context context){
        return new Intent(context, SharkFeedGalleryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return SharkFeedGalleryFragment.newInstance();
    }
}
