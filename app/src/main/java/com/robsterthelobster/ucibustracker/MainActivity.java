package com.robsterthelobster.ucibustracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.robsterthelobster.ucibustracker.data.models.*;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {

    public static final String BASE_URL = "http://www.ucishuttles.com/";
    private static final String TAG = MainActivity.class.getSimpleName();

    UCIBusApiEndpointInterface apiService;

    int routeID, stopID;

    public interface UCIBusApiEndpointInterface {
        @GET("Region/0/Routes")
        Call<List<Route>> getRoutes();

        @GET("Route/{route}/Direction/0/Stops")
        Call<List<Stop>> getStops(@Path("route") int route);

        @GET("Route/{route}/Stop/{stop}/Arrivals")
        Call<Arrivals> getArrivalTimes(@Path("route") int route, @Path("stop") int stop);

        @GET("Route/{route}/Vehicles")
        Call<List<Vehicle>> getVehicles(@Path("route") int route);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService =
                retrofit.create(UCIBusApiEndpointInterface.class);

        Call<List<Route>> routeCall = apiService.getRoutes();
        routeCall.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                List<Route> routes = response.body();
                if(routes != null){
                    Log.d(TAG, "retrofit routeCall : success");
                    for(Route route : routes){
                        Log.d(TAG, "route name: " + route.getName());
                        routeID = route.getId();
                    }
                    callStops();
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void callStops(){
        Call<List<Stop>> stopCall = apiService.getStops(routeID);
        stopCall.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {
                List<Stop> stops = response.body();
                if(stops != null){
                    for(Stop stop : stops){
                        Log.d(TAG, "Stop : " + stop.getName());
                        stopID = stop.getId();
                    }
                    callArrivals();
                }
            }

            @Override
            public void onFailure(Call<List<Stop>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void callArrivals(){
        Call<Arrivals> arrivalsCall = apiService.getArrivalTimes(routeID, stopID);
        arrivalsCall.enqueue(new Callback<Arrivals>() {
            @Override
            public void onResponse(Call<Arrivals> call, Response<Arrivals> response) {
                Arrivals arrivals = response.body();
                if(arrivals != null){
                    List<Prediction> predictions = arrivals.getPredictions();
                    for(Prediction prediction : predictions){
                        Log.d(TAG, "prediction: " + prediction.getArriveTime());
                    }
                    callVehicles();
                }
            }

            @Override
            public void onFailure(Call<Arrivals> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void callVehicles(){
        Call<List<Vehicle>> vehiclesCall = apiService.getVehicles(routeID);
        vehiclesCall.enqueue(new Callback<List<Vehicle>>() {
            @Override
            public void onResponse(Call<List<Vehicle>> call, Response<List<Vehicle>> response) {
                List<Vehicle> vehicles = response.body();
                if(vehicles != null){
                    for(Vehicle vehicle : vehicles){
                        Log.d(TAG, "Percentage: " + vehicle.getApcPercentage());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Vehicle>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }
}
