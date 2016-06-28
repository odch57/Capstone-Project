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

package com.robsterthelobster.ucibustracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robsterthelobster.ucibustracker.data.PredictionAdapter;
import com.robsterthelobster.ucibustracker.data.db.BusContract;

public class ArrivalsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArrivalsFragment.class.getSimpleName();
    private static final int DATASET_COUNT = 60;

    private static final int ARRIVAL_LOADER = 0;
    private static final String[] ARRIVAL_COLUMNS = {
            BusContract.ArrivalEntry._ID,
            BusContract.ArrivalEntry.ROUTE_ID,
            BusContract.ArrivalEntry.ROUTE_NAME,
            BusContract.ArrivalEntry.STOP_ID,
            BusContract.ArrivalEntry.STOP_NAME,
            BusContract.ArrivalEntry.PREDICTION_TIME,
            BusContract.ArrivalEntry.MINUTES,
            BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL
    };
    public static final int C_ROUTE_ID = 1;
    public static final int C_ROUTE_NAME = 2;
    public static final int C_STOP_ID = 3;
    public static final int C_STOP_NAME = 4;
    public static final int C_PREDICTION_TIME = 5;
    public static final int C_MINUTES = 6;
    public static final int C_SECONDS = 7;

    protected RecyclerView mRecyclerView;
    protected PredictionAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(ARRIVAL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_arrivals, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PredictionAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        switch(id){
            case ARRIVAL_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " = ?",
                        new String[]{"0"}, null);
            default:
                Log.d(TAG, "Not valid id: " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
