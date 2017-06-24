package com.android.yahoo.sharkfeed.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
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
import com.android.yahoo.sharkfeed.model.Photo;
import com.android.yahoo.sharkfeed.util.AppUtils;
import com.android.yahoo.sharkfeed.util.EndlessRecyclerViewScrollListener;
import com.android.yahoo.sharkfeed.util.FlickrFetcher;
import com.android.yahoo.sharkfeed.util.PollService;
import com.android.yahoo.sharkfeed.util.QueryPreferences;
import com.android.yahoo.sharkfeed.util.ThumbnailDownloader;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharkFeedGalleryFragment extends VisibleFragment {


    private static final String TAG = SharkFeedGalleryFragment.class.getSimpleName();
    private RecyclerView mPhotoRecyclerView;
    private List<Photo> mPhotos = new ArrayList<>();
    private RecyclerView.Adapter mPhotoRecyclerViewAdapter = new PhotoAdapter(mPhotos);
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener mEndLessScrollListener;

    private static final String PAGE_NUMBER = "PAGE_NUMBER";
    private static Integer sPageNumber = 2;
    private static final int REQUEST_PAGE_NUMBER = 0;


    public SharkFeedGalleryFragment() {
        // Required empty public constructor
    }

    public static SharkFeedGalleryFragment newInstance(){
        return new SharkFeedGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //To retain the fragment across configuration changes
        setRetainInstance(true);

        //To respond to user input for menu items
        setHasOptionsMenu(true);

        updateItems(0, 0, false);

        //Main thread handler thread, using which we can post the updates to the UI.
        Handler responseHandler = new Handler(Looper.getMainLooper());
        mThumbnailDownloader = new ThumbnailDownloader<>(getActivity(),responseHandler);
        mThumbnailDownloader
                .setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        photoHolder.bindDrawable(drawable);
                    }
        });
        //Starts the thread
        mThumbnailDownloader.start();
        //Calls onLooperPrepared
        mThumbnailDownloader.getLooper();

    }

    @Override
    public void onResume() {
        super.onResume();
        //Checking for active network connection and displays snack bar when not available
        AppUtils.showSnackBarNetworkConnection(getActivity(),mPhotoRecyclerView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shark_feed_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.shark_feed_gallery_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragment_shark_feed_gallery_toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoRecyclerView.setLayoutManager(gridLayoutManager);
        //Sets the adapter for the recycler view
        setUpAdapter();

        //Endless scrolling implementation
        mEndLessScrollListener = new
                EndlessRecyclerViewScrollListener(gridLayoutManager, getActivity()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d(TAG, "Load More Page Number : " + page);
                sPageNumber = page;
                updateItems(page, totalItemsCount, false);
            }

            @Override
            public void preloadData(int itemPosition) {
                Photo photo = mPhotos.get(itemPosition);
                mThumbnailDownloader.preload(photo.getUrlS());
            }
        };

        if(savedInstanceState != null){
            sPageNumber = savedInstanceState.getInt(PAGE_NUMBER);
            mEndLessScrollListener.setCurrentPage(sPageNumber);
        }


        mPhotoRecyclerView.addOnScrollListener(mEndLessScrollListener);

        //To calculate the span count for the GridLayout manager based on screen density
        ViewTreeObserver viewTreeObserver = mPhotoRecyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateCellSize();
            }
        });


        //Pull to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mEndLessScrollListener
                        .resetToInitialState();
                updateItems(0, 0, true);
                AppUtils.showSnackBarNetworkConnection(getActivity(),mPhotoRecyclerView);
            }
        });

        //setting color schema for the progress symbol
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

    //converts given size in dp to pixels
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

        //User can request for shark related images using SearchView
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AppUtils.hideKeyboard(getActivity(), searchView);
                menuItem.collapseActionView();
                searchView.onActionViewCollapsed();
                QueryPreferences.setStoredQuery(getActivity(), query);
                mEndLessScrollListener.resetState();
                updateItems(0,0,true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_NUMBER , sPageNumber);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_clear_search:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems(0,0,true);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
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
        QueryPreferences.setPageNumber(getActivity(), page);
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(mPhotoRecyclerViewAdapter, totalItemCount, isSearchQuery)
                .execute(String.valueOf(page), query);
    }

    //AsynTask that loads the thumbnail images tobe displayed into the GridView of the recycler view
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
                return flickrFetcher.fetchSharkPhotos(Integer.valueOf(page[0]));
            }

        }

        @Override
        protected void onPostExecute(List<Photo> photoList) {

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

        private PhotoHolder(final View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_shark_feed_gallery_image_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Photo photo = (Photo) mImageView.getTag();

                    if(photo != null && AppUtils.isNetworkAvailableAndConnected(getActivity())){
                        ViewCompat.setTransitionName(itemView, photo.getId());
                        Fragment lightBoxFragment = LightBoxFragment.newInstance(photo ,
                                                        photo.getId(), sPageNumber);
                       /* lightBoxFragment
                                .setTargetFragment(SharkFeedGalleryFragment.this,
                                        REQUEST_PAGE_NUMBER);*/
                        getFragmentManager()
                                .beginTransaction()
                                .addSharedElement(itemView, ViewCompat.getTransitionName(itemView))
                                .addToBackStack(TAG)
                                .replace(R.id.fragment_container, lightBoxFragment)
                                .commit();
                    }else{
                        Snackbar
                                .make(mPhotoRecyclerView,R.string.please_wait,Snackbar.LENGTH_SHORT);
                    }


                }
            });
        }

        void bindDrawable(Drawable drawable){
            mImageView.setImageDrawable(drawable);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_PAGE_NUMBER){
            sPageNumber = data.getIntExtra(LightBoxFragment.EXTRA_PAGE_NUMBER,2);
            Log.d(TAG, "got page number from light box fragment: " + sPageNumber
                    + " , " + mEndLessScrollListener.getCurrentPage());

          //  mEndLessScrollListener.setCurrentPage(sPageNumber);
        }

    }
}
