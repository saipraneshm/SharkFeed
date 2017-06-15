package com.android.yahoo.sharkfeed.activity;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.fragment.LightBoxFragment;
import com.android.yahoo.sharkfeed.fragment.SharkFeedGalleryFragment;
import com.android.yahoo.sharkfeed.fragment.SharkScreenFragment;
import com.android.yahoo.sharkfeed.util.CustomFragmentPagerAdapter;

import static android.view.Gravity.BOTTOM;

public class SharkFeedSwipeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private static boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shark_feed_swipe);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        FragmentManager fm = getSupportFragmentManager();

        CustomFragmentPagerAdapter fragmentPagerAdapter = new CustomFragmentPagerAdapter(fm);

        fragmentPagerAdapter.addFragment(new SharkScreenFragment(), "Sample 1");
        fragmentPagerAdapter.addFragment(SharkFeedGalleryFragment.newInstance(), " Sample 2");

        mViewPager.setAdapter(fragmentPagerAdapter);

    }

}
