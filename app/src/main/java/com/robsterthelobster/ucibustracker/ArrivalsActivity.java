package com.robsterthelobster.ucibustracker;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.robsterthelobster.ucibustracker.data.UciBusApiEndpointInterface;
import com.robsterthelobster.ucibustracker.data.db.BusContract;
import com.robsterthelobster.ucibustracker.data.models.Arrivals;
import com.robsterthelobster.ucibustracker.data.models.Prediction;
import com.robsterthelobster.ucibustracker.data.models.Route;
import com.robsterthelobster.ucibustracker.data.models.Stop;
import com.robsterthelobster.ucibustracker.data.models.Vehicle;

import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArrivalsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener{

    public final String BASE_URL = "http://www.ucishuttles.com/";
    private final String TAG = ArrivalsActivity.class.getSimpleName();

    private final int ROUTE_LOADER = 0;
    private final String[] ROUTE_COLUMNS = {
            BusContract.RouteEntry.ROUTE_ID,
            BusContract.RouteEntry.ROUTE_NAME,
            BusContract.RouteEntry.COLOR
    };
    private static final int C_ROUTE_ID = 0;
    private static final int C_ROUTE_NAME = 1;
    private static final int C_COLOR = 2;

    UciBusApiEndpointInterface apiService;
    NavigationView navigationView;
    DrawerLayout drawer;
    SubMenu routesMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrivals);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        initRetrofit();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ArrivalsFragment fragment = new ArrivalsFragment();
            transaction.replace(R.id.main_container, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        drawer.closeDrawer(GravityCompat.START, false);
        if(routesMenu != null){
            int size = routesMenu.size();
            for (int i = 0; i < size; i++) {
                routesMenu.getItem(i).setChecked(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.arrivals_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer.closeDrawer(GravityCompat.START, false);
        return false;
    }

    private void initRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService =
                retrofit.create(UciBusApiEndpointInterface.class);

        Call<List<Route>> routeCall = apiService.getRoutes();
        routeCall.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                List<Route> routes = response.body();
                if(routes != null){
                    Log.d(TAG, "retrofit routeCall : success");

                    Vector<ContentValues> cVVector = new Vector<ContentValues>(routes.size());
                    for(Route route : routes){
                        //Log.d(TAG, "route name: " + route.getName());
                        ContentValues routeValues = new ContentValues();

                        int routeID = route.getId();

                        routeValues.put(BusContract.RouteEntry.ROUTE_ID, routeID);
                        routeValues.put(BusContract.RouteEntry.ROUTE_NAME, route.getName());
                        routeValues.put(BusContract.RouteEntry.COLOR, route.getColor());

                        cVVector.add(routeValues);

                        callVehicles(routeID);
                        callStops(routeID);
                    }
                    if ( cVVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.RouteEntry.CONTENT_URI, cvArray);
                    }
                    startLoader();
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void startLoader() {
        getLoaderManager().initLoader(ROUTE_LOADER, null, this);
    }

    private void callStops(final int routeID){
        Call<List<Stop>> stopCall = apiService.getStops(routeID);
        stopCall.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(BusContract.ArrivalEntry.IS_CURRENT, 0);
                getContentResolver().update(BusContract.ArrivalEntry.CONTENT_URI, contentValues,
                        null, null);

                List<Stop> stops = response.body();
                if(stops != null){
                    Log.d(TAG, "retrofit stopsCall : success");
                    Vector<ContentValues> stopVector = new Vector<>(stops.size());
                    Vector<ContentValues> fVector = new Vector<>(stops.size());
                    for(Stop stop : stops){
                        //Log.d(TAG, "Stop : " + stop.getName());
                        ContentValues stopValues = new ContentValues();
                        ContentValues favoriteValues = new ContentValues();
                        int stopID = stop.getId();
                        stopValues.put(BusContract.StopEntry.ROUTE_ID, routeID);
                        stopValues.put(BusContract.StopEntry.STOP_ID, stopID);
                        stopValues.put(BusContract.StopEntry.STOP_NAME, stop.getName());
                        stopValues.put(BusContract.StopEntry.LONGITUDE, stop.getLongitude());
                        stopValues.put(BusContract.StopEntry.LATITUDE, stop.getLatitude());

                        favoriteValues.put(BusContract.FavoriteEntry.FAV_KEY,
                                routeID + "-" + stopID);
                        favoriteValues.put(BusContract.FavoriteEntry.ROUTE_ID, routeID);
                        favoriteValues.put(BusContract.FavoriteEntry.STOP_ID, stopID);
                        favoriteValues.put(BusContract.FavoriteEntry.FAVORITE, 0);

                        stopVector.add(stopValues);
                        fVector.add(favoriteValues);

                        callArrivals(routeID, stopID);
                    }
                    if ( stopVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[stopVector.size()];
                        stopVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.StopEntry.CONTENT_URI, cvArray);

                        ContentValues[] fArray = new ContentValues[fVector.size()];
                        fVector.toArray(fArray);
                        getContentResolver().bulkInsert(BusContract.FavoriteEntry.CONTENT_URI, fArray);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Stop>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void callArrivals(final int routeID, int stopID){
        Call<Arrivals> arrivalsCall = apiService.getArrivalTimes(routeID, stopID);
        arrivalsCall.enqueue(new Callback<Arrivals>() {
            @Override
            public void onResponse(Call<Arrivals> call, Response<Arrivals> response) {

                Arrivals arrivals = response.body();

                if(arrivals != null){
                    Log.d(TAG, "retrofit arrivalsCall : success");
                    List<Prediction> predictions = arrivals.getPredictions();
                    String predictionTime = arrivals.getPredictionTime();

                    int size = predictions.size();
                    ContentValues arrivalValues = new ContentValues();

                    for(int i = 0; i < size; i++){
                        Prediction prediction = predictions.get(i);
                        //Log.d(TAG, "Prediction : " + prediction.getArriveTime());

                        switch (i){
                            case 0:
                                // primary + main prediction
                                arrivalValues.put(BusContract.ArrivalEntry.ROUTE_ID, prediction.getRouteId());
                                arrivalValues.put(BusContract.ArrivalEntry.ROUTE_NAME, prediction.getRouteName());
                                arrivalValues.put(BusContract.ArrivalEntry.STOP_ID, prediction.getStopId());
                                arrivalValues.put(BusContract.ArrivalEntry.PREDICTION_TIME, predictionTime);
                                arrivalValues.put(BusContract.ArrivalEntry.MINUTES, prediction.getMinutes());
                                arrivalValues.put(BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL, prediction.getSecondsToArrival());
                                arrivalValues.put(BusContract.ArrivalEntry.IS_CURRENT, 1);
                                break;
                            case 1:
                                // get the second prediction
                                arrivalValues.put(BusContract.ArrivalEntry.MIN_ALT, prediction.getMinutes());
                                break;
                            case 2:
                                // get the third prediction
                                arrivalValues.put(BusContract.ArrivalEntry.MIN_ALT_2, prediction.getMinutes());
                                break;
                            default:
                                // do nothing
                                Log.d(TAG, "additional predictions beyond 2");
                        }
                    }
                    if(arrivalValues.size() > 0) {
                        getContentResolver().insert(BusContract.ArrivalEntry.CONTENT_URI, arrivalValues);
                    }
                }
            }

            @Override
            public void onFailure(Call<Arrivals> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void callVehicles(int routeID){
        Call<List<Vehicle>> vehiclesCall = apiService.getVehicles(routeID);
        vehiclesCall.enqueue(new Callback<List<Vehicle>>() {
            @Override
            public void onResponse(Call<List<Vehicle>> call, Response<List<Vehicle>> response) {
                List<Vehicle> vehicles = response.body();
                if(vehicles != null){
                    Vector<ContentValues> cVVector = new Vector<>(vehicles.size());
                    for(Vehicle vehicle : vehicles){
                        ContentValues vehicleValues = new ContentValues();

                        vehicleValues.put(BusContract.VehicleEntry.ROUTE_ID, vehicle.getRouteId());
                        vehicleValues.put(BusContract.VehicleEntry.BUS_NAME, vehicle.getName());
                        vehicleValues.put(BusContract.VehicleEntry.LATITUDE, vehicle.getLatitude());
                        vehicleValues.put(BusContract.VehicleEntry.LONGITUDE, vehicle.getLongitude());
                        vehicleValues.put(BusContract.VehicleEntry.PERCENTAGE, vehicle.getApcPercentage());

                        cVVector.add(vehicleValues);
                    }
                    if ( cVVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.VehicleEntry.CONTENT_URI, cvArray);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Vehicle>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch(id){
            case ROUTE_LOADER:
                return new CursorLoader(this,
                        BusContract.RouteEntry.CONTENT_URI,
                        ROUTE_COLUMNS,
                        null, null, null);
            default:
                Log.d(TAG, "Not valid id: " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();

        switch(id){
            case ROUTE_LOADER:
                Menu menu = navigationView.getMenu();
                routesMenu = menu.addSubMenu(Constants.ROUTE_NAME_KEY);
                while(data.moveToNext()){
                    String routeName = data.getString(C_ROUTE_NAME);
                    int routeID = data.getInt(C_ROUTE_ID);
                    MenuItem item = routesMenu.add(routeName);
                    item.setIcon(R.drawable.ic_directions_bus_24dp);
                    item.setCheckable(true);

                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra(Constants.ROUTE_NAME_KEY, routeName);
                    intent.putExtra(Constants.ROUTE_ID_KEY, routeID);

                    item.setIntent(intent);
                }
                getLoaderManager().destroyLoader(ROUTE_LOADER);
                break;
            default:
                Log.d(TAG, "No such id: " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
