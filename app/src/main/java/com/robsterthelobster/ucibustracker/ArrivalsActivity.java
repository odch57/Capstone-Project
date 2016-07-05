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
import com.robsterthelobster.ucibustracker.data.UciBusIntentService;
import com.robsterthelobster.ucibustracker.data.db.BusContract;
import com.robsterthelobster.ucibustracker.data.models.Route;

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

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ArrivalsFragment fragment = new ArrivalsFragment();
            transaction.replace(R.id.main_container, fragment);
            transaction.commit();
        }

        fetchRoutes();
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

    public void restartLoader() {
        Loader loader = getLoaderManager().getLoader(ROUTE_LOADER);

        if(loader != null){
            getLoaderManager().restartLoader(ROUTE_LOADER, null, this);
        }else{
            getLoaderManager().initLoader(ROUTE_LOADER, null, this);
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer.closeDrawer(GravityCompat.START, false);
        return false;
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
                if(data != null && routesMenu == null) {
                    routesMenu = menu.addSubMenu(R.string.nav_submenu_title);
                    while (data.moveToNext()) {
                        String routeName = data.getString(C_ROUTE_NAME);
                        int routeID = data.getInt(C_ROUTE_ID);
                        MenuItem item = routesMenu.add(routeName);
                        item.setIcon(R.drawable.ic_directions_bus_black_24dp);
                        item.setCheckable(true);

                        Intent intent = new Intent(this, DetailActivity.class);
                        intent.putExtra(Constants.ROUTE_NAME_KEY, routeName);
                        intent.putExtra(Constants.ROUTE_ID_KEY, routeID);

                        item.setIntent(intent);
                    }
                }
                getLoaderManager().destroyLoader(ROUTE_LOADER);
                break;
            default:
                Log.d(TAG, "No such id: " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    /*
        This is separate as the nav bar needs this right away
        to initiate the loader
     */
    private void fetchRoutes(){
        String BASE_URL = "http://www.ucishuttles.com/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UciBusApiEndpointInterface apiService =
                retrofit.create(UciBusApiEndpointInterface.class);

        Call<List<Route>> routeCall = apiService.getRoutes();
        routeCall.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                List<Route> routes = response.body();
                if(routes != null){
                    Vector<ContentValues> cVVector = new Vector<>(routes.size());
                    for(Route route : routes){
                        //Log.d(TAG, "route name: " + route.getName());
                        ContentValues routeValues = new ContentValues();

                        int routeID = route.getId();

                        routeValues.put(BusContract.RouteEntry.ROUTE_ID, routeID);
                        routeValues.put(BusContract.RouteEntry.ROUTE_NAME, route.getName());
                        routeValues.put(BusContract.RouteEntry.COLOR, route.getColor());

                        cVVector.add(routeValues);
                    }
                    if ( cVVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.RouteEntry.CONTENT_URI, cvArray);
                    }
                }
                restartLoader();
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.d(TAG, t.getMessage().toString());
            }
        });
    }
}
