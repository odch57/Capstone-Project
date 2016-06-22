/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.robsterthelobster.ucibustracker.data;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.robsterthelobster.ucibustracker.R;


/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {
    private static final String TAG = "PredictionAdapter";

    private String[] mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView routeView;
        private final TextView timeView;
        private final CheckBox buttonView;
        private final TextView stopView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });
            routeView = (TextView) v.findViewById(R.id.prediction_route_name);
            timeView = (TextView) v.findViewById(R.id.prediction_arrival_time);
            buttonView = (CheckBox) v.findViewById(R.id.prediction_favorite_button);
            stopView = (TextView) v.findViewById(R.id.prediction_stop_name);
        }

        public TextView getRouteView() {
            return routeView;
        }

        public TextView getTimeView() {
            return timeView;
        }

        public CheckBox getButtonView() {
            return buttonView;
        }

        public TextView getStopView() {
            return stopView;
        }

    }

    public PredictionAdapter(String[] dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.prediction_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        viewHolder.getRouteView().setText(mDataSet[position]);
        viewHolder.getStopView().setText(mDataSet[position]);
        viewHolder.getTimeView().setText(mDataSet[position]);
    }

    @Override
    public int getItemCount() {
        return mDataSet.length;
    }
}
