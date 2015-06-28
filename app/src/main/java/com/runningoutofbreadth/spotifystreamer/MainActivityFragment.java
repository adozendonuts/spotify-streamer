package com.runningoutofbreadth.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    private ArrayAdapter<String> mArtistAdapter;
    private String search;

    //TODO create a settings menu layout to implement the refresh button as an updater.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String LOG_TAG = FetchArtistData.class.getSimpleName();

        final View rootView = inflater.inflate(R.layout.fragment_search_artists, container, false);

        //search field on top of screen
        final EditText editText = (EditText) rootView.findViewById(R.id.search_edit_text);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                v = editText;
                actionId = EditorInfo.IME_ACTION_SEARCH;
                search = v.getText().toString();

                Log.v(LOG_TAG, "THIS IS THE SEARCH: " + search);

                FetchArtistData fetchArtistData = new FetchArtistData();
                fetchArtistData.execute(search);
                return true;
            }

        });



        String[] data = {
                "search for something"
        };

        //create a list using data above
        List<String> results = new ArrayList<String>(Arrays.asList(data));
        mArtistAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.individual_artist,
                R.id.search_list_item,
                results);
        ListView list = (ListView) rootView.findViewById(R.id.artist_list_view);
        list.setAdapter(mArtistAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String track = mArtistAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ArtistTracks.class)
                        .putExtra(Intent.EXTRA_TEXT, track);
                startActivity(intent);
            }
        });

        //instantiate fetchartistdata (asynctask) and run it


        return rootView;
    }


    public class FetchArtistData extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchArtistData.class.getSimpleName();

        protected String doInBackground(String... params) {
            //create a new class that can contain a pic and a string


            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                ArtistsPager artistsPager = spotify.searchArtists(search);
                List<Artist> artistNameListItems = new ArrayList<Artist>(artistsPager.artists.items);

                // see how many artistResults are returned. expected 20
                int size = artistNameListItems.size();
                Log.v(LOG_TAG, size + " ITEMS: " + artistNameListItems.toString());

                //get smallest picture by accessing last item in images array
                String individualArtistName;
                String thumbnailUrl;
                List<String[]> results = new ArrayList<String[]>();

                for (Artist each : artistNameListItems) {
                    int currentIndex = artistNameListItems.indexOf(each);
                    individualArtistName = each.name;
                    String[] nameAndThumbnail = {individualArtistName};
                    Log.v(LOG_TAG, currentIndex + " - " + individualArtistName);
                    results.add(nameAndThumbnail);
                }

                Log.v(LOG_TAG, "here are the results: " + results);

                // TODO make it so that for each of the Artists results, this array gets added
                // Also, refactor the strings so that it's taking the position based on what is
                // being returned by the onItemClickListener position value


                //Log.v(LOG_TAG, "THIS IS THE REMIX " + results.toString());

            } catch (Exception e) {
                return null;
            }

            return null;
        }

        //TODO refactor to pass in new lists as each list element

    }
}
