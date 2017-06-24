package com.android.yahoo.sharkfeed.util;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * This abstract class is used to implement endless scrolling.
 * Whenever the recycler view reaches the end of the scroll (page), we trigger a method, which
 * is then called by the recycler view, this method then perform the operation of loading or
 * appending more pages.
 */

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {


    private static final String TAG = EndlessRecyclerViewScrollListener.class.getSimpleName();
    // The minimum amount of items to have below your current scroll position
        // before loading more.
        private int visibleThreshold = 5;
        // The current offset index of data you have loaded
        private int currentPage = 1;
        // The total number of items in the dataset after the last load
        private int previousTotalItemCount = 0;
        // True if we are still waiting for the last set of data to load.
        private boolean loading = true;
        // Sets the starting page index
        private int startingPageIndex = 0;

        RecyclerView.LayoutManager mLayoutManager;

        private Context mContext;


        public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager, Context context) {
            this.mLayoutManager = layoutManager;
            visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
            mContext = context;
        }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if(newState == RecyclerView.SCROLL_STATE_IDLE){

            int lastVisibleItemPosition = ((GridLayoutManager)mLayoutManager)
                    .findLastVisibleItemPosition();
            int firstVisibleItemPosition = ((GridLayoutManager)mLayoutManager)
                    .findFirstVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();


            int difference = lastVisibleItemPosition - firstVisibleItemPosition;

            int start = firstVisibleItemPosition > difference
                    ? firstVisibleItemPosition - difference : firstVisibleItemPosition;
            int last  = lastVisibleItemPosition + difference < totalItemCount
                    ? lastVisibleItemPosition + difference
                    : lastVisibleItemPosition;

          /*  Log.d(TAG, "start " + start + ": " + " last " + ": " + last);
            Log.d(TAG, "first visible " + firstVisibleItemPosition + ": " + " last visible " + ": "
                    +  lastVisibleItemPosition);*/

            for(int i = last  ; i > start + 10; i-- ){
                preloadData(i);
            }

        }


    }


    // This happens many times a second during a scroll, so be wary of the code you place here.
        // We are given a few useful parameters to help us work out if we need to load some more data,
        // but first we check if we are waiting for the previous load to finish.
        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            currentPage = QueryPreferences.getPageNumber(mContext);

            int lastVisibleItemPosition = 0;
            int totalItemCount = mLayoutManager.getItemCount();

            lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager)
                    .findLastVisibleItemPosition();

            // If the total item count is zero and the previous isn't, assume the
            // list is invalidated and should be reset back to initial state
            if (totalItemCount < previousTotalItemCount) {
                Log.d(TAG, "tic < ptic");
                this.currentPage = this.startingPageIndex;
                this.previousTotalItemCount = totalItemCount;
                if (totalItemCount == 0) {
                    Log.d(TAG, "loading failed");
                    this.loading = true;
                }
            }
            // If it’s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.
            if (loading && (totalItemCount > previousTotalItemCount)) {
                Log.d(TAG, "updating the page count");
                loading = false;
                previousTotalItemCount = totalItemCount;
            }

            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            // threshold should reflect how many total columns there are too
            if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
                Log.d(TAG, "incrementing current page: " + currentPage);
                currentPage++;
                onLoadMore(currentPage, totalItemCount, view);
                loading = true;
            }
        }

        // Call this method whenever performing new searches
        public void resetState() {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = 0;
            this.loading = true;
        }

        //
        public void resetToInitialState(){
            this.currentPage = 0;
            this.previousTotalItemCount = 0;
            this.loading = false;
        }



        // Defines the process for actually loading more data based on page
        public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

        public abstract void preloadData(int itemPosition);

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
