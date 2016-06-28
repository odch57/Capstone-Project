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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.robsterthelobster.ucibustracker.ArrivalsFragment;
import com.robsterthelobster.ucibustracker.R;
import com.robsterthelobster.ucibustracker.data.models.Arrivals;

public class PredictionAdapter extends CursorRecyclerViewAdapter<PredictionAdapter.ViewHolder> {
    private static final String TAG = "PredictionAdapter";

    private static Context mContext;

    public PredictionAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView routeView;
        private final TextView timeView;
        private final CheckBox buttonView;
        private final TextView stopView;
        private final View view;

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
            view = v;
        }

        public void setBackground(String color){
            view.setBackgroundColor(Color.parseColor(color));
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.prediction_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String routeName = cursor.getString(ArrivalsFragment.C_ROUTE_NAME);
        String stopName = cursor.getString(ArrivalsFragment.C_STOP_NAME);
        int minutes = cursor.getInt(ArrivalsFragment.C_MINUTES);
        String arrivalTime = minutes + " minutes";

        viewHolder.setBackground("#F0649E");
        viewHolder.getRouteView().setText(routeName);
        viewHolder.getTimeView().setText(arrivalTime);
        viewHolder.getStopView().setText(stopName);
    }
}
