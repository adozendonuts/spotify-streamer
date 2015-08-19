package com.runningoutofbreadth.spotifystreamer;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * Top Ten Tracks
 */
public class TopTenTracksFragment extends Fragment {
    private String mArtistId;
    private String mArtistName;
    static final String ARTIST_ID_KEY = "ID";
    static final String ARTIST_NAME_KEY = "NAME";
    private TracksAdapter tracksAdapter;
    private List<Track> mTracks = new ArrayList<>();

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
            Log.v("TOP TEN TRACKS FRAGMENT", mArtistId + " " + mArtistName);
            FetchTracks fetchTracks = new FetchTracks();
            fetchTracks.execute(mArtistId);
        }

        Intent intent = getActivity().getIntent();
        final View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            mArtistName = intent.getStringExtra("Artist");
            FetchTracks fetchTracks = new FetchTracks();
            fetchTracks.execute(mArtistId);
        }

        tracksAdapter = new TracksAdapter(rootView.getContext(), R.layout.individual_track, mTracks);

        ListView list = (ListView) rootView.findViewById(R.id.track_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String trackPreviewUrl = mTracks.get(position).preview_url;
                String trackAlbum = mTracks.get(position).album.name;
                String trackName = mTracks.get(position).name;
                String trackAlbumCover = mTracks.get(position).album.images.get(1).url;

                Intent intent = new Intent(getActivity(), TrackPlayerActivity.class)
                        .putExtra("URL", trackPreviewUrl);
                intent.putExtra("Artist", mArtistName);
                intent.putExtra("Album", trackAlbum);
                intent.putExtra("Track", trackName);
                intent.putExtra("Cover", trackAlbumCover);
                startActivity(intent);
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
            Map<String, Object> options = new HashMap<>();
            options.put("country", Locale.getDefault().getCountry());

            try {
                Log.v(LOG_TAG, spotify.getArtistTopTrack(mArtistId, options).tracks.toString());
                return spotify.getArtistTopTrack(mArtistId, options);
            } catch (NullPointerException e) {
                Log.v(LOG_TAG, mArtistId + "is what is returned.");
                return null;
            }
        }

        @Override
        public void onPostExecute(Tracks result) {
            Log.v(LOG_TAG, "M TRACKS SIZE = " + tracksAdapter.getCount());
            Log.v(LOG_TAG, "Result = " + result);

            tracksAdapter.clear();
            if (result.tracks != null) {
                for (Track each : result.tracks) {
                    tracksAdapter.add(each);
                }
            } else {
                Log.v(LOG_TAG, result.tracks.toString());
            }

        }
    }
}
