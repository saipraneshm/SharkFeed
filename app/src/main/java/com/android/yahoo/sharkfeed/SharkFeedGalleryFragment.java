package com.android.yahoo.sharkfeed;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharkFeedGalleryFragment extends Fragment {


    public SharkFeedGalleryFragment() {
        // Required empty public constructor
    }

    public static SharkFeedGalleryFragment newInstance(){
        return new SharkFeedGalleryFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shark_feed_gallery, container, false);

        return view;
    }

}
