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

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;

import com.robsterthelobster.ucibustracker.ArrivalsFragment;
import com.robsterthelobster.ucibustracker.R;
import com.robsterthelobster.ucibustracker.Utility;

/**
 * Created by robin
 * https://gist.github.com/ZkHaider/9bf0e1d7b8a2736fd676
 */
public class PredictionAdapter extends CursorRecyclerViewAdapter<PredictionAdapter.ViewHolder> {
    private static final String TAG = "PredictionAdapter";

    private static Context mContext;

    public PredictionAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private int originalHeight = 0;
        private int expandingHeight = 0;
        private boolean isViewExpanded = false;

        private final TextView routeView;
        private final TextView timeView;
        private final CheckBox buttonView;
        private final TextView stopView;
        private final TextView timeViewAlt;
        private final View view;

        public ViewHolder(View v) {
            super(v);

            routeView = (TextView) v.findViewById(R.id.prediction_route_name);
            timeView = (TextView) v.findViewById(R.id.prediction_arrival_time);
            timeViewAlt = (TextView) v.findViewById(R.id.prediction_arrival_time_alt);
            buttonView = (CheckBox) v.findViewById(R.id.prediction_favorite_button);
            stopView = (TextView) v.findViewById(R.id.prediction_stop_name);
            view = v;

            v.setOnClickListener(this);

            if (!isViewExpanded) {
                timeViewAlt.setVisibility(View.GONE);
                timeViewAlt.setEnabled(false);
            }
        }

        public void onClick(View v) {
            Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");

            // initialization
            if (originalHeight == 0) {
                originalHeight = view.getHeight();
                expandingHeight = (int)(originalHeight * .25);
            }

            // Declare a ValueAnimator object
            ValueAnimator valueAnimator;
            if (!isViewExpanded) {
                timeViewAlt.setVisibility(View.VISIBLE);
                timeViewAlt.setEnabled(true);
                Log.d(TAG, timeViewAlt.getHeight() + "");
                isViewExpanded = true;
                valueAnimator = ValueAnimator.ofInt(originalHeight,
                                originalHeight + expandingHeight);
            } else {
                isViewExpanded = false;
                valueAnimator = ValueAnimator.ofInt(originalHeight + expandingHeight,
                        originalHeight);

                Animation a = new AlphaAnimation(1.00f, 0.00f); // Fade out

                a.setDuration(100);
                // Set a listener to the animation and configure onAnimationEnd
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        timeViewAlt.setVisibility(View.GONE);
                        timeViewAlt.setEnabled(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                timeViewAlt.startAnimation(a);
            }
            valueAnimator.setDuration(100);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    view.getLayoutParams().height = value;
                    view.requestLayout();
                }
            });
            valueAnimator.start();
        }

        public void setBackground(String color){
            view.setBackgroundColor(Color.parseColor(color));
        }

        public TextView getRouteView() {
            return routeView;
        }

        public TextView getTimeViewAlt() {
            return timeViewAlt;
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
                .inflate(R.layout.prediction_item_expanded, viewGroup, false);

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
        double seconds = cursor.getDouble(ArrivalsFragment.C_SECONDS);
        String arrivalTime = Utility.getArrivalTime(minutes, seconds);
        String color = cursor.getString(ArrivalsFragment.C_COLOR);

        viewHolder.getButtonView().setOnCheckedChangeListener(null);

        viewHolder.setBackground(color);
        viewHolder.getRouteView().setText(routeName);
        viewHolder.getTimeView().setText(arrivalTime);
        viewHolder.getTimeViewAlt().setText(arrivalTime);
        viewHolder.getStopView().setText(stopName);
    }
}
