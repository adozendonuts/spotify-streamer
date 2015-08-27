package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by SandD on 8/27/2015.
 */
public class Utility {

    // determines whether or not an internet connection exists
    public static Boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        }
        return true;
    }

    // toast for no Internet
    public static void noInternetToast(Context context) {
        Toast toast = Toast.makeText(context.getApplicationContext(),
                "No internet!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}

