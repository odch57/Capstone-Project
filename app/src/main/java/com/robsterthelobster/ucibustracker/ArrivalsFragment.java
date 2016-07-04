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

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.robsterthelobster.ucibustracker.data.ArrivalsPredictionAdapter;
import com.robsterthelobster.ucibustracker.data.db.BusContract;

public class ArrivalsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = ArrivalsFragment.class.getSimpleName();

    private final int ARRIVAL_LOADER = 0;
    private final int STOP_LOADER = 1;
    private final String[] ARRIVAL_COLUMNS = {
            BusContract.ArrivalEntry._ID,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.ROUTE_ID,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.ROUTE_NAME,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.STOP_ID,
            BusContract.ArrivalEntry.PREDICTION_TIME,
            BusContract.ArrivalEntry.MINUTES,
            BusContract.ArrivalEntry.MIN_ALT,
            BusContract.ArrivalEntry.MIN_ALT_2,
            BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL,
            BusContract.RouteEntry.TABLE_NAME + "." + BusContract.RouteEntry.COLOR,
            BusContract.StopEntry.TABLE_NAME + "." + BusContract.StopEntry.STOP_NAME,
            BusContract.FavoriteEntry.TABLE_NAME + "." + BusContract.FavoriteEntry.FAVORITE
    };
    public static final int C_ROUTE_ID = 1;
    public static final int C_ROUTE_NAME = 2;
    public static final int C_STOP_ID = 3;
    public static final int C_PREDICTION_TIME = 4;
    public static final int C_MINUTES = 5;
    public static final int C_MIN_ALT = 6;
    public static final int C_MIN_ALT2 = 7;
    public static final int C_SECONDS = 8;
    public static final int C_COLOR = 9;
    public static final int C_STOP_NAME = 10;
    public static final int C_FAVORITE = 11;

    private final String[] STOP_COLUMNS = {
            BusContract.StopEntry.STOP_ID,
            BusContract.StopEntry.STOP_NAME,
            BusContract.StopEntry.LATITUDE,
            BusContract.StopEntry.LONGITUDE,
            BusContract.RouteEntry.TABLE_NAME + "." + BusContract.RouteEntry.COLOR
    };
    public final int SC_STOP_ID = 0;
    public final int SC_STOP_NAME = 1;
    public final int SC_STOP_LAT = 2;
    public final int SC_STOP_LONG = 3;
    public final int SC_COLOR = 4;

    protected RecyclerView mRecyclerView;
    protected ArrivalsPredictionAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected TextView emptyView;

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private String routeName;
    private boolean hasRouteID = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            routeName = arguments.getString(Constants.ROUTE_NAME_KEY);
            hasRouteID = true;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ARRIVAL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_arrivals, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        mAdapter = new ArrivalsPredictionAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case ARRIVAL_LOADER:
                if(hasRouteID){
                    return new CursorLoader(getContext(),
                            BusContract.ArrivalEntry.CONTENT_URI,
                            ARRIVAL_COLUMNS,
                            BusContract.ArrivalEntry.IS_CURRENT + " = ?" +
                                    " AND " + BusContract.ArrivalEntry.TABLE_NAME + "." +
                                    BusContract.ArrivalEntry.ROUTE_NAME + " = ?",
                            new String[]{"0", routeName},
                            null);
                }else {
                    // FAVORITES, THEN ARRIVAL TIME
                    String sortOrder = BusContract.FavoriteEntry.FAVORITE + " DESC, " +
                            BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL + " ASC";
                    return new CursorLoader(getContext(),
                            BusContract.ArrivalEntry.CONTENT_URI,
                            ARRIVAL_COLUMNS,
                            BusContract.ArrivalEntry.IS_CURRENT + " = ?",
                            new String[]{"1"},
                            sortOrder);
                }
            default:
                Log.d(TAG, "Not valid id: " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        switch(id){
            case ARRIVAL_LOADER:
                if(cursor.getCount() == 0){
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    emptyView.setVisibility(View.VISIBLE);
                }else{
                    mRecyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.INVISIBLE);
                }
                mAdapter.swapCursor(cursor);
                break;
            case STOP_LOADER:
                break;
            default:
                Log.d(TAG, "Not valid id: " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.toString());
    }
}
