package com.runningoutofbreadth.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends Fragment {
    private String trackPreviewUrl;
    public TrackPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final View rootView = inflater.inflate(R.layout.fragment_track_player, container, false);

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            trackPreviewUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        try {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    trackPreviewUrl, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }catch(NullPointerException e){
            Log.v("YO DAWG", "This don't got no url");
        }

        return rootView;
    }
}
