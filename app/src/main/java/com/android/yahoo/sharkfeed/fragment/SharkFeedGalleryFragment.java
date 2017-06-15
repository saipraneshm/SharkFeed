package com.android.yahoo.sharkfeed.fragment;


import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.android.yahoo.sharkfeed.R;
import com.android.yahoo.sharkfeed.activity.LightBoxActivity;
import com.android.yahoo.sharkfeed.model.Photo;
import com.android.yahoo.sharkfeed.util.AppUtils;
import com.android.yahoo.sharkfeed.util.EndlessRecyclerViewScrollListener;
import com.android.yahoo.sharkfeed.util.FlickrFetcher;
import com.android.yahoo.sharkfeed.util.QueryPreferences;
import com.android.yahoo.sharkfeed.util.ThumbnailDownloader;

import java.io.File;
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
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private SwipeRefreshLayout mSwipeRefreshLayout;


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
        setHasOptionsMenu(true);

        updateItems(0, 0, false);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(getActivity(),responseHandler);
        mThumbnailDownloader
                .setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        photoHolder.bindDrawable(drawable);
                    }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.d(TAG, "Background thread started");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shark_feed_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.shark_feed_gallery_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragment_shark_feed_gallery_toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoRecyclerView.setLayoutManager(gridLayoutManager);
        setUpAdapter();
        mEndLessScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager){
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                updateItems(page, totalItemsCount, false);
            }

            @Override
            public void preloadData(int itemPosition) {
                Photo photo = mPhotos.get(itemPosition);
                mThumbnailDownloader.preload(photo.getUrlS());
              //  Log.d(TAG," got url's for position " + itemPosition + ", url ->" + photo.getUrlS());
            }
        };
        mPhotoRecyclerView.addOnScrollListener(mEndLessScrollListener);
        ViewTreeObserver viewTreeObserver = mPhotoRecyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateCellSize();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateItems(0, 0, true);
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        return view;
    }

    private static final int sColumnWidth = 120;

    //method to dynamically calculate the column span based on device.
    private void calculateCellSize(){
        int spanCount = (int) Math.ceil( mPhotoRecyclerView.getWidth() / convertDPToPixels(sColumnWidth));
        ((GridLayoutManager) mPhotoRecyclerView.getLayoutManager() ).setSpanCount(spanCount);
    }

    private float convertDPToPixels(int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return dp * logicalDensity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_shark_feed_menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.menu_item_search);

        final SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "query submitted : " + query);
                AppUtils.hideKeyboard(getActivity(), searchView);
                menuItem.collapseActionView();
                searchView.onActionViewCollapsed();
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems(0,0,true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

       /* searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                Log.d(TAG, "stored  query: " + query);
                searchView.setQuery(query, false);
            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_clear_search:
               // Log.d(TAG, " clicked on clear search ");
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems(0,0,true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.d(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private void setUpAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(mPhotoRecyclerViewAdapter);
        }
    }

    private void updateItems(int page, int totalItemCount, boolean isSearchQuery){
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, " update items called with "+ query + " " + page);
        new FetchItemsTask(mPhotoRecyclerViewAdapter, totalItemCount, isSearchQuery)
                .execute(String.valueOf(page), query);
    }


    private class FetchItemsTask extends AsyncTask<String, Void, List<Photo>>{

        private RecyclerView.Adapter mAdapter;
        private int mTotalItemsCount;
        private boolean isSearchQuery = false;

        FetchItemsTask(RecyclerView.Adapter adapter, int totalItemsCount, boolean isSearchQuery){
            mAdapter = adapter;
            mTotalItemsCount = totalItemsCount;
            this.isSearchQuery = isSearchQuery;
        }

        @Override
        protected List<Photo> doInBackground(String... page) {

            FlickrFetcher flickrFetcher = new FlickrFetcher();

            if(page.length > 1 && page[1] != null){
                return flickrFetcher.searchSharkPhotos(page[1], Integer.valueOf(page[0]));
            }else{
                Log.d(TAG, "default called");
                return flickrFetcher.fetchSharkPhotos(Integer.valueOf(page[0]));
            }

        }

        @Override
        protected void onPostExecute(List<Photo> photoList) {

            Log.d(TAG, " on post execute called");
            if(isSearchQuery){
                mPhotos.clear();
                mPhotos.addAll(photoList);
                mAdapter.notifyDataSetChanged();
                isSearchQuery = false;
                mSwipeRefreshLayout.setRefreshing(false);
                return;
            }

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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Photo photo = (Photo) mImageView.getTag();
                    Intent intent = new Intent(getActivity(), LightBoxActivity.class);
                    intent.putExtra(LightBoxFragment.DOWNLOAD_URL_C, photo.getUrlC());
                    intent.putExtra(LightBoxFragment.DOWNLOAD_URL_L, photo.getUrlL());
                    intent.putExtra(LightBoxFragment.DOWNLOAD_URL_O, photo.getUrlO());
                    startActivity(intent);
                    Log.d(TAG, "clicked on the image with title " + photo.getTitle());
                }
            });
        }

        void bindDrawable(Drawable drawable){
            mImageView.setImageDrawable(drawable);
        }

        /*private File getPhotoFile(Photo photo){
            File externalFileDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if(externalFileDir == null) return null;

            return new File(externalFileDir , photo.getPhotoFileName());
        }*/

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
            if(photo.getUrlS() != null && (photo.getUrlC() != null
                    || photo.getUrlL() != null || photo.getUrlO() != null)){
                mThumbnailDownloader.queueThumbnail(holder, photo.getUrlS());
                holder.mImageView.setTag(photo);
            }


        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }
    }

}
