package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


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
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.individual_track, null);

                String trackTitle = tracks.get(position).name;
                String album = tracks.get(position).album.name;
                List<Image> albumImages = tracks.get(position).album.images;
                int lastOne;
                String url;

                TextView trackTitleView = (TextView) rowView.findViewById(R.id.track_title_text_view);
                TextView albumTitleView = (TextView) rowView.findViewById(R.id.album_text_view);
                ImageView albumImageView = (ImageView) rowView.findViewById(R.id.album_thumbnail);

                try {
                    if (albumImages.size() > 0) {
                        lastOne = albumImages.size() - 1;
                        url = albumImages.get(lastOne).url;
                    } else {
                        url = "";
                        albumImageView.setImageResource(R.drawable.eigth_notes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    url = "";
                    Log.v("ARTIST TRACKS LOGGING", "what the hell caused THIS?: " + e);
                }

                Picasso.with(getContext()).load(url).into(albumImageView);
                trackTitleView.setText(trackTitle);
                albumTitleView.setText(album);
            }
            return rowView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mArtistId = arguments.getString(ARTIST_ID_KEY);
            mArtistName = arguments.getString(ARTIST_NAME_KEY);
            mTwoPane = arguments.getBoolean(PANE_KEY);
            FetchTracks fetchTracks = new FetchTracks();
            fetchTracks.execute(mArtistId);
        }
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(ARTIST_ID_KEY)) {
            mArtistId = intent.getStringExtra(ARTIST_ID_KEY);
            mArtistName = intent.getStringExtra(ARTIST_NAME_KEY);
            FetchTracks fetchTracks = new FetchTracks();
            fetchTracks.execute(mArtistId);
        }

        final View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        tracksAdapter = new TracksAdapter(rootView.getContext(), R.layout.individual_track, mTracks);

        ListView list = (ListView) rootView.findViewById(R.id.track_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                args.putString(TrackPlayerFragment.ARTIST_NAME_KEY, mArtistName);
                args.putInt(TrackPlayerFragment.POSITION_KEY, position);
                args.putParcelable(TrackPlayerFragment.TRACK_LIST_KEY, mTracklist);

                TrackPlayerFragment trackPlayerFragment = new TrackPlayerFragment();
                trackPlayerFragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();
                trackPlayerFragment.show(fragmentManager, PLAYERFRAGMENT_TAG);
            }
        });

        list.setAdapter(tracksAdapter);
        return rootView;
    }

    public class FetchTracks extends AsyncTask<String, Void, Tracks> {
        private final String LOG_TAG = FetchTracks.class.getSimpleName();

        protected Tracks doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            try {
                Log.v(LOG_TAG, spotify.getArtistTopTrack(mArtistId, Locale.getDefault().getCountry()).tracks.toString());
                return spotify.getArtistTopTrack(mArtistId, Locale.getDefault().getCountry());
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onPostExecute(Tracks result) {
            tracksAdapter.clear();
            if (result.tracks != null) {
                for (Track each : result.tracks) {
                    tracksAdapter.add(each);
                }
                mTracklist = result;
            } else {
                Log.v(LOG_TAG, result.tracks.toString());
            }
        }
    }
}
