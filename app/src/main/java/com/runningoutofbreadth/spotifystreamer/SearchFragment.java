package com.runningoutofbreadth.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {
    private ArtistAdapter mArtistAdapter;
    private String search;
    private List<Artist> mArtists = new ArrayList<Artist>();
    private List<String> urls;


    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public class ArtistAdapter extends ArrayAdapter<Artist> {
        List<Artist> items;

        //constructor.
        public ArtistAdapter(Context context, int resource, List<Artist> items) {
            super(context, resource, items);
            this.items = items;
        }

        //override getView method
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.individual_artist, parent, false);
            }

            String artistName = items.get(position).name;
            List<Image> thumbnailList = items.get(position).images;
            int lastOne = 0;
            String url;

            TextView artistNameView = (TextView) rowView.findViewById(R.id.artist_name_text_view);
            ImageView thumbnailView = (ImageView) rowView.findViewById(R.id.artist_thumbnail_image_view);

            try {
                if (thumbnailList.size() > 0) {
                    lastOne = thumbnailList.size() - 2;
                    url = thumbnailList.get(lastOne).url;
                    Picasso.with(getContext()).load(url).into(thumbnailView);
                    urls.add(url);
                } else {
                    Picasso.with(getContext()).load(R.drawable.eigth_notes).into(thumbnailView);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("ARTISTADAPTER", "what the hell caused THIS?: " + e);
            }

            artistNameView.setText(artistName);
            return rowView;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.getString(search);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_search_artists, container, false);

        //search field on top of screen, made so that hitting Next runs search
        final EditText editText = (EditText) rootView.findViewById(R.id.search_edit_text);

        mArtistAdapter = new ArtistAdapter(getActivity(), R.layout.individual_artist, mArtists);

        //instantiate listview. onClick, open up new activity
        final ListView list = (ListView) rootView.findViewById(R.id.artist_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistId = mArtists.get(position).id;
                Intent intent = new Intent(getActivity(), ArtistTracks.class)
                        .putExtra(Intent.EXTRA_TEXT, artistId);
                startActivity(intent);
            }
        });

        //once you hit enter key (done/next/etc.), Asynctask runs
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                v = editText;
                search = v.getText().toString();

                //runs once Done is pressed.
                if (!search.isEmpty()) {
                    //check for internet connection
                    ConnectivityManager cm = (ConnectivityManager) getActivity()
                            .getSystemService(Context.CONNECTIVITY_SERVICE);

                    if (cm.getActiveNetworkInfo() == null) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "No internet!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    } else {
                        //make keyboard disappear if search goes through
                        InputMethodManager inputManager = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.toggleSoftInput(0, 0);

                        FetchArtistData fetchArtistData = new FetchArtistData();
                        fetchArtistData.execute(search);
                        list.setSelectionAfterHeaderView();
                    }

                }
                return true;
            }
        });

        list.setAdapter(mArtistAdapter);
        return rootView;
    }

    public class FetchArtistData extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = FetchArtistData.class.getSimpleName();

        protected List<Artist> doInBackground(String... params) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(search);

            return artistsPager.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> result) {
            //takes all nested String arrays and adds them to mArtistAdapter
            if (result != null && !result.isEmpty()) {
                mArtistAdapter.clear();
                for (Artist each : result) {
                    mArtistAdapter.add(each);
                }
            } else {
                Toast toast = Toast.makeText(getActivity()
                                .getApplicationContext(),
                        "No results.\n" +
                                "Try using fewer letters\n" +
                                "or search for another artist.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();

            }
        }
    }
}

