package com.robsterthelobster.ucibustracker;

import android.content.Context;
import android.graphics.Color;
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
        // minutes passed from (minutes, seconds) won't be 0
        // if 0, it means there's no more arrivals
        if(minutes == 0){
            return "LAST";
        }
        return minutes + " min";
    }

    public static float hexToFloat(String colorStr) {
        colorStr = colorStr.replace("#", "");
        int color = (int)Long.parseLong(colorStr, 16);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color >> 0) & 0xFF;
        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        return hsv[0];
    }
}
