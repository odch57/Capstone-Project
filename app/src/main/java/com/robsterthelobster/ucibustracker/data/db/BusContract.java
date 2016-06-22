package com.robsterthelobster.ucibustracker.data.db;

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

    }

    public static final class StopEntry implements BaseColumns{

    }

    public static final class ArrivalEntry implements BaseColumns{

    }

    public static final class VehicleEntry implements BaseColumns{

    }
}
