package com.robsterthelobster.ucibustracker;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.robsterthelobster.ucibustracker.data.db.BusContract;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends SupportMapFragment
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>,
        GoogleMap.OnMyLocationButtonClickListener {

    private final String TAG = MapFragment.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private final int STOP_LOADER = 0;
    private final int VEHICLE_LOADER = 1;

    private final String[] STOP_COLUMNS = {
            BusContract.StopEntry.STOP_ID,
            BusContract.StopEntry.STOP_NAME,
            BusContract.StopEntry.LATITUDE,
            BusContract.StopEntry.LONGITUDE,
            BusContract.RouteEntry.TABLE_NAME + "." + BusContract.RouteEntry.COLOR
    };
    public static final int C_STOP_ID = 0;
    public static final int C_STOP_NAME = 1;
    public static final int C_STOP_LAT = 2;
    public static final int C_STOP_LONG = 3;
    public static final int C_COLOR = 4;

    private final String[] VEHICLE_COLUMNS = {
            BusContract.VehicleEntry.ROUTE_ID,
            BusContract.VehicleEntry.BUS_NAME,
            BusContract.VehicleEntry.LATITUDE,
            BusContract.VehicleEntry.LONGITUDE,
            BusContract.VehicleEntry.PERCENTAGE
    };
    public static final int C_ROUTE_ID = 0;
    public static final int C_BUS_NAME = 1;
    public static final int C_BUS_LAT = 2;
    public static final int C_BUS_LONG = 3;
    public static final int C_PERCENTAGE = 4;

    private GoogleMap mMap;
    private String routeID = "";
    private List<Marker> stopMarkers;
    private List<Marker> vehicleMarkers;

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
        stopMarkers = new ArrayList<>();
        vehicleMarkers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOP_LOADER, null, this);
        getLoaderManager().initLoader(VEHICLE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id){
            case STOP_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.StopEntry.buildStopsInRouteUri(routeID),
                        STOP_COLUMNS,
                        BusContract.RouteEntry.TABLE_NAME + "." +
                                BusContract.RouteEntry.ROUTE_ID + " = ?",
                        new String[] {routeID},
                        null
                        );
            case VEHICLE_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.VehicleEntry.buildVehiclesInRouteUri(routeID),
                        VEHICLE_COLUMNS,
                        null,
                        null,
                        null);
            default:
                Log.d(TAG, "No such id for loader");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        int id = loader.getId();
        switch(id){
            case STOP_LOADER:
                while(data.moveToNext() && mMap != null){
                    double latitude = data.getDouble(C_STOP_LAT);
                    double longitude = data.getDouble(C_STOP_LONG);
                    String stopName = data.getString(C_STOP_NAME);
                    String color = data.getString(C_COLOR);

                    LatLng latLng = new LatLng(latitude, longitude);
                    stopMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(getBitmapDescriptor(R.drawable.ic_directions_bus_24dp,
                                    Color.parseColor(color)))
                            //.icon(getBitmapDescriptor(R.drawable.bus_tracker, color))
                            .position(latLng).title(stopName)));
                }
                centerMapToMarkers(200);
                //drawRoutePath(color);
                break;
            case VEHICLE_LOADER:
                System.out.println("loader");
                while(data.moveToNext() && mMap != null){
                    double latitude = data.getDouble(C_BUS_LAT);
                    double longitude = data.getDouble(C_BUS_LONG);
                    String busName = "Bus " + data.getString(C_BUS_NAME);
                    String percentage = "Percent full: " + data.getInt(C_PERCENTAGE) + "%";

                    LatLng latLng = new LatLng(latitude, longitude);
                    vehicleMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(getBitmapDescriptor(R.drawable.bus_tracker, R.color.colorPrimary))
                            .position(latLng).title(busName).snippet(percentage)));
                }
                break;
            default:
                Log.d(TAG, "No such id for loader");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    // http://stackoverflow.com/questions/14828217/
    // android-map-v2-zoom-to-show-all-the-markers
    private void centerMapToMarkers(int padding){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : stopMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
    }

    // http://stackoverflow.com/questions/16262837/
    // how-to-draw-route-in-google-maps-api-v2-from-my-location
    private void drawRoutePath(String colorHex){
        PolylineOptions options = new PolylineOptions();

        options.color(Color.parseColor(colorHex) );
        options.width(5);
        options.visible(true);

        for ( Marker marker : stopMarkers )
        {
            options.add(marker.getPosition());
        }

        mMap.addPolyline(options);
    }

    private BitmapDescriptor getBitmapDescriptor(int id, int color) {
        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), id);
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(color != -1)
                vectorDrawable.setTint(color);
        }
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
