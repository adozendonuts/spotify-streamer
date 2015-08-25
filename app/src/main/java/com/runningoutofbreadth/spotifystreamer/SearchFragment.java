package com.runningoutofbreadth.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
 * The fragment within the main activity. Has a search bar on top and results underneath
 */
public class SearchFragment extends Fragment {
    private ArtistAdapter mArtistAdapter;
    TopTenTracksCallback mTopTenTracksCallback;
    private String search;
    private List<Artist> mArtists = new ArrayList<Artist>();

    public SearchFragment() {
    }

    public interface TopTenTracksCallback {
        void onItemSelected(String artistId, String artistName);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mTopTenTracksCallback = (TopTenTracksCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onItemSelected");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    static class SearchViewHolder {
        TextView artistNameView;
        ImageView thumbnailView;
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
            SearchViewHolder searchViewHolder;

            //if there is no existing row view, inflate a new view
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.individual_artist, parent, false);
                searchViewHolder = new SearchViewHolder();
                searchViewHolder.artistNameView = (TextView) convertView.findViewById(R.id.artist_name_text_view);
                searchViewHolder.thumbnailView = (ImageView) convertView.findViewById(R.id.artist_thumbnail_image_view);
                convertView.setTag(searchViewHolder);
            } else {
                // reuse "scrapview" with the same tag.
                searchViewHolder = (SearchViewHolder) convertView.getTag();
            }

            String artistName = items.get(position).name;
            List<Image> thumbnailList = items.get(position).images;
            int lastOne = 0;
            String url;

            try {
                // if the artist has thumbnail pictures for this entry
                // get smallest thumbnail available to conserve data usage
                if (thumbnailList.size() > 0) {
                    lastOne = thumbnailList.size() - 1;
                    url = thumbnailList.get(lastOne).url;
                    Picasso.with(getContext())
                            .load(url)
                            .placeholder(R.drawable.eigth_notes)
                            .error(R.drawable.eigth_notes)
                            .into(searchViewHolder.thumbnailView);
                } else {
                    // if there are no thumbnails, use a default local image
                    Picasso.with(getContext())
                            .load(R.drawable.eigth_notes)
                            .into(searchViewHolder.thumbnailView);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("ARTISTADAPTER", "what the hell caused THIS?: " + e);
            }
            searchViewHolder.artistNameView.setText(artistName);
            return convertView;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        //search field on top of screen, made so that hitting Next runs search
        final EditText editText = (EditText) rootView.findViewById(R.id.search_edit_text);

        mArtistAdapter = new ArtistAdapter(getActivity(), R.layout.individual_artist, mArtists);

        //instantiate listview. onClick, talk to MainActivity
        final ListView list = (ListView) rootView.findViewById(R.id.artist_list_view);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //check if internet is connected. Toast if not.
                if (cm.getActiveNetworkInfo() == null) {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            "No internet!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                } else {
                    // send info to mainactivity and have the onItemSelected method handle
                    // whether it should open a new activity or load a fragment
                    String artistId = mArtists.get(position).id;
                    String artistName = mArtists.get(position).name;
                    ((TopTenTracksCallback) getActivity()).onItemSelected(artistId, artistName);
                }
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
        private Toast toast;

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
                if (toast != null) {
                    toast.cancel();
                }
                mArtistAdapter.clear();
                for (Artist each : result) {
                    mArtistAdapter.add(each);
                }
            } else {
                toast = Toast.makeText(getActivity()
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

