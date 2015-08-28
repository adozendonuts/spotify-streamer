package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Helper methods
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

    // MEDIAPLAYERSERVICE - reset and repair player, load url
    public static void loadTrack(Tracks trackList, int position, MediaPlayer mPlayer){
        mPlayer.reset();
        String trackUrl = trackList.tracks.get(position).preview_url;
        try {
            mPlayer.setDataSource(trackUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.prepareAsync();
    }

}

