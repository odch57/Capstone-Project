package com.robsterthelobster.ucibustracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robsterthelobster.ucibustracker.data.db.BusContract;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>{

    private final String TAG = MapFragment.class.getSimpleName();
    private final int MAP_LOADER = 0;
    private final String[] STOP_COLUMNS = {
            BusContract.StopEntry.STOP_ID,
            BusContract.StopEntry.STOP_NAME,
            BusContract.StopEntry.LATITUDE,
            BusContract.StopEntry.LONGITUDE,
    };
    public static final int C_STOP_ID = 0;
    public static final int C_STOP_NAME = 1;
    public static final int C_LAT = 2;
    public static final int C_LONG = 3;

    private GoogleMap mMap;
    private String routeID = "";
    private List<Marker> markers;

    public MapFragment() {
        getMapAsync(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            routeID = arguments.getInt(Constants.ROUTE_ID_KEY) + "";
        }
        markers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MAP_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    // http://stackoverflow.com/questions/14828217/android-map-v2-zoom-to-show-all-the-markers
    private void centerMapToMarkers(int padding){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id){
            case MAP_LOADER:
                Log.d(TAG, routeID);
                String selection =
                        BusContract.RouteEntry.TABLE_NAME + "." +
                                BusContract.RouteEntry.ROUTE_ID + " = ?";
                return new CursorLoader(getContext(),
                        BusContract.StopEntry.buildStopsInRouteUri(routeID),
                        STOP_COLUMNS,
                        selection,
                        new String[] {routeID},
                        null
                        );
            default:
                Log.d(TAG, "No such id for loader");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if(loader.getId() == MAP_LOADER){
            while(data.moveToNext()){
                double latitude = data.getDouble(C_LAT);
                double longitude = data.getDouble(C_LONG);
                String stopName = data.getString(C_STOP_NAME);

                LatLng latLng = new LatLng(latitude, longitude);
                markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(stopName)));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
