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

import android.content.ContentResolver;
import android.content.Intent;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.robsterthelobster.ucibustracker.data.ArrivalsCursorWrapper;
import com.robsterthelobster.ucibustracker.data.ArrivalsPredictionAdapter;
import com.robsterthelobster.ucibustracker.data.UciBusIntentService;
import com.robsterthelobster.ucibustracker.data.db.BusContract;

public class ArrivalsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = ArrivalsFragment.class.getSimpleName();

    private final int ARRIVAL_LOADER = 0;
    private final int STOP_ARRIVAL_LOADER = 1;
    private final int NO_LOCATION_LOADER = 2;
    private final int STOP_LOADER = 3;
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
            BusContract.FavoriteEntry.TABLE_NAME + "." + BusContract.FavoriteEntry.FAVORITE,
            BusContract.StopEntry.LATITUDE,
            BusContract.StopEntry.LONGITUDE
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
    public static final int C_LATITUDE = 12;
    public static final int C_LONGITUDE = 13;

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
    protected SwipeRefreshLayout mySwipeRefreshLayout;
    protected ArrivalsPredictionAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected TextView emptyView;

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    private Location mLocation;

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
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

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

        mySwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                        updateRouteData();
                    }
                }
        );

        mAdapter = new ArrivalsPredictionAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.arrivals_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        switch(item.getItemId()){
            case R.id.menu_refresh:
                Log.i(TAG, "Refresh menu item selected");
                mySwipeRefreshLayout.setRefreshing(true);
                updateRouteData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String location = "";
        if(mLocation != null){
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();

            double fudge = Math.pow(Math.cos(Math.toRadians(latitude)),2);

            String latOrder = "(" + latitude + " - " + BusContract.StopEntry.LATITUDE + ")";
            String longOrder = "(" + longitude + " - " + BusContract.StopEntry.LONGITUDE + ")";
            location = "(" + latOrder + "*" + latOrder +
                    "+" + longOrder + "*" + longOrder + "*" + fudge + "), ";
        }

        String sortOrder;
        switch (id) {
            case ARRIVAL_LOADER:
                // FAVORITES, THEN ARRIVAL TIME
                sortOrder = BusContract.FavoriteEntry.FAVORITE + " DESC, " +
                        location +
                        BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL + " ASC";
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " =? ",
                        new String[]{"0"},
                        sortOrder);
            case NO_LOCATION_LOADER:
                sortOrder = BusContract.FavoriteEntry.FAVORITE + " DESC, " +
                        BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL + " ASC";
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " =? ",
                        new String[]{"0"},
                        sortOrder);
            case STOP_ARRIVAL_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " = ?" +
                                " AND " + BusContract.ArrivalEntry.TABLE_NAME + "." +
                                BusContract.ArrivalEntry.ROUTE_NAME + " = ?",
                        new String[]{"0", routeName},
                        null);
            default:
                Log.d(TAG, "Not valid id: " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        if(cursor.getCount() == 0){
            mRecyclerView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
        }
        switch(id){
            /*
                Only the arrival_loader needs to have the cursor filtered
             */
            case ARRIVAL_LOADER:
                cursor = new ArrivalsCursorWrapper(cursor, mLocation,
                        getContext().getResources().getInteger(R.integer.nearby_distance));
            case STOP_ARRIVAL_LOADER:
            case NO_LOCATION_LOADER:
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
        mLocationRequest.setInterval(5000);

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
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        if(hasRouteID){
            getLoaderManager().initLoader(STOP_ARRIVAL_LOADER, null, this);
        }else if(mLocation != null){
            getLoaderManager().initLoader(ARRIVAL_LOADER, null, this);
        }else{
            getLoaderManager().initLoader(NO_LOCATION_LOADER, null, this);
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
        mLocation = location;

        updateRouteData();
    }

    private void updateRouteData() {
//        Intent alarmIntent = new Intent(getActivity(), UciBusIntentService.AlarmReceiver.class);
//
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);
//
//        AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.add(Calendar.SECOND, 0);
//        long frequency = 60 * 1000;
//
//        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pi);
        Intent intent = new Intent(getActivity(), UciBusIntentService.class);
        getActivity().startService(intent);

        if(hasRouteID){
            getLoaderManager().restartLoader(STOP_ARRIVAL_LOADER, null, this);
        }else {
            getLoaderManager().restartLoader(ARRIVAL_LOADER, null, this);
        }

        mySwipeRefreshLayout.setRefreshing(false);
        ContentResolver cr = getContext().getContentResolver();
        cr.notifyChange(BusContract.RouteEntry.CONTENT_URI, null);
    }
}
