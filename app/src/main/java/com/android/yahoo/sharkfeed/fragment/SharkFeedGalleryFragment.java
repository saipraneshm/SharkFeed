package com.android.yahoo.sharkfeed.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.model.Photo;
import com.android.yahoo.sharkfeed.util.EndlessRecyclerViewScrollListener;
import com.android.yahoo.sharkfeed.util.FlickrFetcher;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharkFeedGalleryFragment extends Fragment {


    private static final String TAG = SharkFeedGalleryFragment.class.getSimpleName();
    private RecyclerView mPhotoRecyclerView;
    private List<Photo> mPhotos = new ArrayList<>();
    private RecyclerView.OnScrollListener mEndLessScrollListener;
    private RecyclerView.Adapter mPhotoRecyclerViewAdapter = new PhotoAdapter(mPhotos);




    public SharkFeedGalleryFragment() {
        // Required empty public constructor
    }

    public static SharkFeedGalleryFragment newInstance(){
        return new SharkFeedGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shark_feed_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.shark_feed_gallery_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoRecyclerView.setLayoutManager(gridLayoutManager);
        setUpAdapter();
        mEndLessScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager){
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                new FetchItemsTask(mPhotoRecyclerViewAdapter, totalItemsCount, view)
                                .execute(page);
            }
        };
        mPhotoRecyclerView.addOnScrollListener(mEndLessScrollListener);
        return view;
    }

    private void setUpAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(mPhotoRecyclerViewAdapter);
        }
    }


    private class FetchItemsTask extends AsyncTask<Integer, Void, List<Photo>>{

        private RecyclerView.Adapter mAdapter;
        private int mTotalItemsCount;
        private RecyclerView mRecyclerView;

        FetchItemsTask(RecyclerView.Adapter adapter, int totalItemsCount, RecyclerView recyclerView){
            mAdapter = adapter;
            mTotalItemsCount = totalItemsCount;
            mRecyclerView = recyclerView;
        }

        FetchItemsTask(){}

        @Override
        protected List<Photo> doInBackground(Integer... page) {
            return new FlickrFetcher().fetchItems(page[0]);
        }

        @Override
        protected void onPostExecute(List<Photo> photoList) {
            mPhotos.addAll(photoList);
            if(mTotalItemsCount == 0){
                setUpAdapter();
            }
            else{
                int curSize = mAdapter.getItemCount();
                mAdapter.notifyItemRangeChanged(curSize, mPhotos.size() - 1);
            }

        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{

        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_shark_feed_gallery_image_view);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{


        List<Photo> mPhotos;

        PhotoAdapter(List<Photo> photosList){
            mPhotos = photosList;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item,parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Photo photo = mPhotos.get(position);

        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }
    }

}
