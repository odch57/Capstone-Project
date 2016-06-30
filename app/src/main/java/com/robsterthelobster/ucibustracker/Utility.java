package com.robsterthelobster.ucibustracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
            return minutes + "min";
        }else if(seconds > 0){
            return "<1 min";
        }else{
            return "0 min";
        }
    }

}
