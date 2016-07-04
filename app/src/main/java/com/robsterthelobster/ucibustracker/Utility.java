package com.robsterthelobster.ucibustracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by robin on 6/28/2016.
 */
public class Utility {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /*
        append minutes/seconds with min
        more detailed if given seconds
     */
    public static String getArrivalTime(int minutes, double seconds){
        if(seconds > 60){
            return getArrivalTime(minutes);
        }else if(seconds > 0){
            return "<1 min";
        }else{
            return "0 min";
        }
    }

    public static String getArrivalTime(int minutes){
        if(minutes == 0){
            return "LAST";
        }
        return minutes + " min";
    }

    /*
        convert hex string into a hue
     */
    public static float hexToHue(String colorStr) {
        colorStr = colorStr.replace("#", "");
        int color = (int)Long.parseLong(colorStr, 16);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color >> 0) & 0xFF;
        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        return hsv[0];
    }

    /*
        distance between two latlng
        implementation of haversine formula

        http://stackoverflow.com/questions/3695224/
        sqlite-getting-nearest-locations-with-latitude-and-longitude
     */
    public static double getDistanceBetweenTwoPoints(Location location, double latitude, double longitude) {
        if(location == null){
            return 0;
        }
        double EARTH_RADIUS = 6371000; // meters
        double l_lat = location.getLatitude();
        double l_long = location.getLongitude();

        double dLat = Math.toRadians(l_lat - latitude);
        double dLon = Math.toRadians(l_long - longitude);
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(l_lat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = EARTH_RADIUS * c;

        return d;
    }
}
