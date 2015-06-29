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

    private ArrayAdapter<String[]> mArtistAdapter;
    private String search;

    //TODO create a settings menu layout to implement the refresh button as an updater.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String LOG_TAG = FetchArtistData.class.getSimpleName();

        final View rootView = inflater.inflate(R.layout.fragment_search_artists, container, false);

        //search field on top of screen, made so that hitting Next runs search
        final EditText editText = (EditText) rootView.findViewById(R.id.search_edit_text);

        String[][] data = {
                {"search for something"},
                {"sup"}
        };

        //create a list using data above
        List<String[]> results = new ArrayList<String[]>(Arrays.asList(data));
        mArtistAdapter = new ArrayAdapter<String[]>(getActivity(),
                R.layout.individual_artist,
                R.id.search_list_item,
                results);
        final ListView list = (ListView) rootView.findViewById(R.id.artist_list_view);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] track = mArtistAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ArtistTracks.class)
                        .putExtra(Intent.EXTRA_TEXT, track);
                startActivity(intent);
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                v = editText;
                search = v.getText().toString();
                search = "beyonce";
                //check to make sure search actually spits out as string
                Log.v(LOG_TAG, "THIS IS THE SEARCH: " + search);

                //starts up Spotify built-in http connection, returns goodies
                if (search != null) {
                    FetchArtistData fetchArtistData = new FetchArtistData();
                    fetchArtistData.execute(search);
                }
                return true;
            }

        });

        list.setAdapter(mArtistAdapter);
        return rootView;
    }


    public class FetchArtistData extends AsyncTask<String, Void, String[][]> {

        private final String LOG_TAG = FetchArtistData.class.getSimpleName();

        protected List<String[]> results;

        protected String[][] doInBackground(String... params) {
            //TODO create a new class that can contain a pic and a string

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(search);
            List<Artist> artistNameListItems = new ArrayList<Artist>(artistsPager.artists.items);

            // see how many artistResults are returned. expected 20
            int size = artistNameListItems.size();
            String[][] artistListString = new String[size][2];
            Log.v(LOG_TAG, size + " ITEMS: " + artistNameListItems.toString());

            try {

                //get smallest picture by accessing last item in images array
                String individualArtistName;
                String thumbnailUrl;

                for (Artist each : artistNameListItems) {
                    //for the Log
                    int currentIndex = artistNameListItems.indexOf(each);

                    //images
                    int lastOne;
                    int imageListSize = each.images.size();
                    if (imageListSize == 0) {
                        thumbnailUrl = "";
                    } else {
                        lastOne = imageListSize - 1;
                        thumbnailUrl = each.images.get(lastOne).url;
                    }

                    //names
                    individualArtistName = each.name;

                    //convert names and images to a list
                    String[] nameAndThumbnail = {thumbnailUrl, individualArtistName};

                    //append list to results with results.add()
                    artistListString[currentIndex] = nameAndThumbnail;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "what happened?" + e);
                return null;
            }

            try {
                return artistListString;
            } catch (Exception e) {
                //no results? oh well
                Log.e(LOG_TAG, "No results for query", e);
                e.printStackTrace();
            }
            return null;
        }

        //TODO refactor to pass in new lists as each list element
        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                mArtistAdapter.clear();
                for (String[] each : result) {
                    mArtistAdapter.add(each);
                    Log.v(LOG_TAG, "this is the mArtistAdapter entry:  " + each[0] + "    " + each[1]);
                }

            }
        }

    }
}
