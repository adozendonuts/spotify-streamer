package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistTracksFragment extends Fragment {
    private String mArtistId;
    private Intent mIntent;
    private ArrayAdapter<String> mTrackAdapter;
    private TracksAdapter adapter;
    private List<Track> mTracks = new ArrayList<>();
    final String LOG_TAG = "ARTIST TRACKS LOGGING";

    public ArtistTracksFragment() {
    }

    public class TracksAdapter extends ArrayAdapter {

        public TracksAdapter(Context context, int resource, List<Track> tracks) {
            super(context, resource, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.individual_track, null);
                Log.v(LOG_TAG, "this is the rowView" + rowView.toString());
            }
            return rowView;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mIntent = getActivity().getIntent();
        final View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);

        if (mIntent != null && mIntent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistId = mIntent.getStringExtra(Intent.EXTRA_TEXT);
            mArtistId = "12Chz98pHFMPJEknJQMWvI";
            Log.v(LOG_TAG, "this is mArtistId: " + mArtistId);
        }

        adapter = new TracksAdapter(rootView.getContext(), R.layout.individual_track, mTracks);
//        mTrackAdapter = new ArrayAdapter<String>(rootView.getContext(),
//                R.layout.fragment_artist_tracks,
//                R.layout.individual_track,
//                testArray);
        ListView list = (ListView) rootView.findViewById(R.id.track_list_view);
        FetchTracks fetchTracks = new FetchTracks();
        fetchTracks.execute(mArtistId);

        list.setAdapter(adapter);
        return rootView;
    }

    public class FetchTracks extends AsyncTask<String, Void, Tracks> {
        private final String LOG_TAG = FetchTracks.class.getSimpleName();


        protected Tracks doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> options = new HashMap<>();
            options.put("country", Locale.getDefault().getCountry());
            Tracks artistTopTracks = spotify.getArtistTopTrack(mArtistId, options);
            Log.v(LOG_TAG, "here are the tracks " + artistTopTracks);

            return artistTopTracks;
        }

        @Override
        public void onPostExecute(Tracks result) {
            Log.v(LOG_TAG, "M TRACKS SIZE = : " + adapter.getCount());
            adapter.clear();
            if (result.tracks != null) {
                for (Track each : result.tracks) {
                    adapter.add(each);
                }
            }
            Log.v(LOG_TAG, "here are the results" + result.tracks);
            Log.v(LOG_TAG, "Adapter's NEW SIZE = : " + adapter.getCount());
        }
    }
}
