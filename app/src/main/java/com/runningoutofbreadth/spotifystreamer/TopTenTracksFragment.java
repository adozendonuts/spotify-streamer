package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * Top Ten Tracks
 */
public class TopTenTracksFragment extends Fragment {
    String PLAYERFRAGMENT_TAG = "TFTAG";
    private String mArtistId;
    private String mArtistName;
    static final String ARTIST_ID_KEY = "ID";
    static final String ARTIST_NAME_KEY = "NAME";
    static final String PANE_KEY = "PANES";
    boolean mTwoPane;
    private TracksAdapter tracksAdapter;
    private List<Track> mTracks = new ArrayList<>();
    private Tracks mTracklist;

    public TopTenTracksFragment() {
    }

    public class TracksAdapter extends ArrayAdapter<Track> {
        private List<Track> tracks;

        public TracksAdapter(Context context, int resource, List<Track> tracks) {
            super(context, resource, tracks);
            this.tracks = tracks;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TopTenViewHolder topTenViewHolder;

            // if no existing view available to recycle, inflate a new one
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.individual_track, parent, false);
                topTenViewHolder = new TopTenViewHolder();
                topTenViewHolder.trackTitleView = (TextView) convertView.findViewById(R.id.track_title_text_view);
                topTenViewHolder.albumTitleView = (TextView) convertView.findViewById(R.id.album_text_view);
                topTenViewHolder.albumImageView = (ImageView) convertView.findViewById(R.id.album_thumbnail);
                convertView.setTag(topTenViewHolder);
            } else {
                topTenViewHolder = (TopTenViewHolder) convertView.getTag();
            }

            String trackTitle = tracks.get(position).name;
            String album = tracks.get(position).album.name;
            List<Image> albumImages = tracks.get(position).album.images;
            int lastOne;
            String url;

            try {
                // if the album item contains pictures
                // get smallest image available to conserve data usage
                if (albumImages.size() > 0) {
                    lastOne = albumImages.size() - 1;
                    url = albumImages.get(lastOne).url;
                    Picasso.with(getContext())
                            .load(url)
                            .placeholder(R.drawable.eigth_notes)
                            .error(R.drawable.eigth_notes)
                            .into(topTenViewHolder.albumImageView);
                } else {
                    // if there are no thumbnails, use a default local image
                    Picasso.with(getContext())
                            .load(R.drawable.eigth_notes)
                            .into(topTenViewHolder.albumImageView);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("ARTIST TRACKS LOGGING", "what the hell caused THIS?: " + e);
            }
            topTenViewHolder.trackTitleView.setText(trackTitle);
            topTenViewHolder.albumTitleView.setText(album);
            return convertView;
        }
    }

    static class TopTenViewHolder {
        TextView trackTitleView;
        TextView albumTitleView;
        ImageView albumImageView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            mArtistId = savedInstanceState.getString(ARTIST_ID_KEY);
            mArtistName = savedInstanceState.getString(ARTIST_NAME_KEY);
            mTwoPane = savedInstanceState.getBoolean(PANE_KEY);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mArtistId = arguments.getString(ARTIST_ID_KEY);
                mArtistName = arguments.getString(ARTIST_NAME_KEY);
                mTwoPane = arguments.getBoolean(PANE_KEY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String LOG_TAG = TopTenTracksFragment.class.getSimpleName();

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(ARTIST_ID_KEY)) {
            mArtistId = intent.getStringExtra(ARTIST_ID_KEY);
            mArtistName = intent.getStringExtra(ARTIST_NAME_KEY);
        }

        if (savedInstanceState == null && mArtistId != null) {
            FetchTracks fetchTracks = new FetchTracks();
            fetchTracks.execute(mArtistId);
        } else {
            Log.v(LOG_TAG, "Missing Artist ID" + mArtistId);
        }

        final View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        tracksAdapter = new TracksAdapter(rootView.getContext(), R.layout.individual_track, mTracks);

        ListView list = (ListView) rootView.findViewById(R.id.track_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!Utility.hasInternet(getActivity())){
                    Utility.noInternetToast(getActivity());
                }else {
                    Bundle args = new Bundle();
                    args.putString(TrackPlayerFragment.ARTIST_NAME_KEY, mArtistName);
                    args.putInt(TrackPlayerFragment.POSITION_KEY, position);
                    args.putParcelable(TrackPlayerFragment.TRACK_LIST_KEY, mTracklist);
                    showPlayer(args);
                }
            }
        });

        list.setAdapter(tracksAdapter);
        return rootView;
    }

    // loads the dialogfragment that contains the player UI
    public void showPlayer(Bundle args) {
        TrackPlayerFragment trackPlayerFragment = new TrackPlayerFragment();
        FragmentManager fragmentManager = getFragmentManager();
        trackPlayerFragment.setArguments(args);

        if (mTwoPane) {
            // show small dialog
            trackPlayerFragment.show(fragmentManager, PLAYERFRAGMENT_TAG);
        } else {
            // open fragment fullscreen
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.add(android.R.id.content, trackPlayerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public class FetchTracks extends AsyncTask<String, Void, Tracks> {
//        private final String LOG_TAG = FetchTracks.class.getSimpleName();
        private Toast toast;

        protected Tracks doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            if (Utility.hasInternet(getActivity())) {
                try {
                    return spotify.getArtistTopTrack(mArtistId, Locale.getDefault().getCountry());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return null;
                } catch (RetrofitError r) {
                    r.printStackTrace();
                    return null;
                }
            } else {
                Utility.noInternetToast(getActivity());
                return null;
            }
        }

        @Override
        public void onPostExecute(Tracks result) {
            tracksAdapter.clear();
            if (result != null && result.tracks != null) {
                if (toast != null) {
                    toast.cancel();
                }
                for (Track each : result.tracks) {
                    tracksAdapter.add(each);
                }
                mTracklist = result;
            } else {
                toast = Toast.makeText(getActivity()
                                .getApplicationContext(),
                        "No results.\n" +
                                "It seems this artist\n" +
                                "does not have any tracks.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        }
    }
}
