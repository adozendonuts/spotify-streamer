package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
public class MainActivityFragment extends Fragment {
    private ArtistAdapter mArtistAdapter;
    private String search;
    private List<Artist> mArtists = new ArrayList<Artist>();

    public MainActivityFragment() {
    }

    public class ArtistAdapter extends ArrayAdapter {
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
                } else {
                    url = "";
                    Picasso.with(getContext()).load(R.drawable.eigth_notes).into(thumbnailView);
                    Log.v("ARTISTADAPTER", "Why is it choosing this as the picture?");
                }
            } catch (Exception e) {
                url = "";
                e.printStackTrace();
                Log.v("ARTISTADAPTER", "what the hell caused THIS?: " + e);
            }

            artistNameView.setText(artistName);
            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String LOG_TAG = FetchArtistData.class.getSimpleName();
        final View rootView = inflater.inflate(R.layout.fragment_search_artists, container, false);

        //search field on top of screen, made so that hitting Next runs search
        final EditText editText = (EditText) rootView.findViewById(R.id.search_edit_text);

        mArtistAdapter = new ArtistAdapter(getActivity(), R.layout.individual_artist, mArtists);

        //onClick, open up new activity
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
                search = "muse";
                //check to make sure search actually spits out as string
//                Log.v(LOG_TAG, "THIS IS THE SEARCH: " + search);

                //starts up Spotify built-in http connection, returns goodies
                if (!search.isEmpty()) {
                    FetchArtistData fetchArtistData = new FetchArtistData();
                    fetchArtistData.execute(search);
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
            if (result != null) {
                for (Artist each : result) {
                    mArtistAdapter.add(each);
                }
            }
        }
    }
}

