package com.android.yahoo.sharkfeed.activity;

import android.support.v4.app.Fragment;

import com.android.yahoo.sharkfeed.fragment.SharkFeedGalleryFragment;
import com.android.yahoo.sharkfeed.activity.abs.SingleFragmentActivity;

public class SharkFeedGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return SharkFeedGalleryFragment.newInstance();
    }
}
