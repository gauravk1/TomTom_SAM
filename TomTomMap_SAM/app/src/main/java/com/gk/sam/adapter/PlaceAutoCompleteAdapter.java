package com.gk.sam.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gk.sam.R;
import com.google.common.collect.ImmutableList;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

public class PlaceAutoCompleteAdapter extends RecyclerView.Adapter<PlaceAutoCompleteAdapter.PlaceViewHolder> {

    public interface ClickPlaceInterface {
        void onPlaceClick(ImmutableList<FuzzySearchResult> mResultList, int position, String address);
    }

    private Context mContext;
    private ClickPlaceInterface mListener;
    private ImmutableList<FuzzySearchResult> mResultList;

    public PlaceAutoCompleteAdapter(Context context, ImmutableList<FuzzySearchResult> geocodeResults) {
        this.mContext = context;
        this.mListener = (ClickPlaceInterface) mContext;
        this.mResultList = geocodeResults;
    }

    /*
    Clear List items
     */
    public void clearList() {
        if (mResultList != null && mResultList.size() > 0) {
            mResultList = ImmutableList.of();
        }
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.view_place_search, viewGroup, false);
        PlaceViewHolder mPredictionHolder = new PlaceViewHolder(convertView);
        return mPredictionHolder;
    }


    @Override
    public void onBindViewHolder(final PlaceViewHolder mPredictionHolder, int position) {
        FuzzySearchResult fuzzySearchResult = getItem(position);
        final String full_address = fuzzySearchResult.getPoi().getName().trim() + " " + fuzzySearchResult.getAddress().getFreeformAddress().trim();
        mPredictionHolder.mAddress.setText(full_address);
        mPredictionHolder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlaceClick(mResultList, mPredictionHolder.getAdapterPosition(), mResultList.get(mPredictionHolder.getAdapterPosition()).getPoi().getName());
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mResultList != null)
            return mResultList.size();
        else
            return 0;
    }

    public FuzzySearchResult getItem(int position) {
        return mResultList.get(position);
    }


    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mParentLayout;
        public TextView mAddress;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            mParentLayout = (RelativeLayout) itemView.findViewById(R.id.predictedRow);
            mAddress = (TextView) itemView.findViewById(R.id.address);
        }
    }
}