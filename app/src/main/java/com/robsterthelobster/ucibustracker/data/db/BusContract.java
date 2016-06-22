package com.robsterthelobster.ucibustracker.data.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by robin on 6/21/2016.
 */
public class BusContract {
    public static final String CONTENT_AUTHORITY = "com.robsterthelobster.ucibustracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ROUTES = "routes";
    public static final String PATH_STOPS = "stops";
    public static final String PATH_ARRIVALS = "arrivals";
    public static final String PATH_VEHICLES = "vehicles";

    public static final class RouteEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROUTES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROUTES;

        public static final String TABLE_NAME = "routes";

        public static final String ROUTE_ID = "route_id";
        public static final String ROUTE_NAME = "route_name";
        public static final String COLOR = "color";

        public static Uri buildRouteUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class StopEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOPS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOPS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOPS;

        public static final String TABLE_NAME = "stops";

        public static final String STOP_ID = "stop_id";
        public static final String STOP_NAME = "stop_name";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";

        public static Uri buildStopUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ArrivalEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARRIVALS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARRIVALS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARRIVALS;

        public static final String TABLE_NAME = "arrivals";

        public static final String ROUTE_ID = "route_id";
        public static final String ROUTE_NAME = "route_name";
        public static final String STOP_ID = "stop_id";
        public static final String PREDICTION_TIME = "prediction_time";
        public static final String SECONDS_TO_ARRIVAL = "seconds_to_arrival";

        public static Uri buildArrivalUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class VehicleEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VEHICLES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLES;

        public static final String TABLE_NAME = "vehicles";

        public static final String ROUTE_ID = "route_id";
        public static final String BUS_NAME = "bus_name";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";
        public static final String PERCENTAGE = "percentage";

        public static Uri buildVehicle(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
