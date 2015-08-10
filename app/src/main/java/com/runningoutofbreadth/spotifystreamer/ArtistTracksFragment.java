package com.runningoutofbreadth.spotifystreamer;

import android.app.Fragment;
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
public class ArtistTracksFragment extends Fragment {
    private String mArtistId;
    private String artistName;
    private TracksAdapter tracksAdapter;
    private List<Track> mTracks = new ArrayList<>();

    public ArtistTracksFragment() {
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

        Intent intent = getActivity().getIntent();
        final View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            artistName = intent.getStringExtra("Artist");

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

                Intent intent = new Intent(getActivity(), TrackPlayer.class)
                        .putExtra("URL", trackPreviewUrl);
                intent.putExtra("Artist", artistName);
                intent.putExtra("Album", trackAlbum);
                intent.putExtra("Track", trackName);
                intent.putExtra("Cover", trackAlbumCover);
                startActivity(intent);
            }
        });

        FetchTracks fetchTracks = new FetchTracks();
        fetchTracks.execute(mArtistId);

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

            return spotify.getArtistTopTrack(mArtistId, options);
        }

        @Override
        public void onPostExecute(Tracks result) {
            Log.v(LOG_TAG, "M TRACKS SIZE = : " + tracksAdapter.getCount());
            tracksAdapter.clear();
            if (result.tracks != null) {
                for (Track each : result.tracks) {
                    tracksAdapter.add(each);
                }
            }

        }
    }
}
