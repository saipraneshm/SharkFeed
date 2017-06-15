package com.android.yahoo.sharkfeed.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sai pranesh
 * About: This class is used to add custom fragments to be displayed by a
 * fragment pager adapter.
 */



public class CustomFragmentPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentsList = new ArrayList<>();
    private final List<String>  mFragmentTitles = new ArrayList<>();

    public CustomFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title){
        mFragmentsList.add(fragment);
        mFragmentTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentsList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentsList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitles.get(position);
    }
}
