package com.teamhotspots.hotspots;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kathleen on 4/7/2017.
 */

public class Feed extends Fragment {
    private ListView postsListView;
    private List<Post> posts = new ArrayList<Post>();
    private PostAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_feed,
                container, false);

        generatePosts();
        postsListView = (ListView) view.findViewById(R.id.feed_list);
        adapter = new PostAdapter(getActivity(), R.layout.post, posts);
        postsListView.setAdapter(adapter);
/*
        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter,View v, int position, long id){
                Post item = (Post) adapter.getItemAtPosition(position);

                EditPost fragment = new EditPost();
                Bundle args = new Bundle();
                args.putLong(EditPost.DATE_MS, item.getDate().getTime());
                args.putDouble(EditPost.HOURS, item.getHours());
                args.putBoolean(EditPost.DAY, item.getDay());
                args.putInt(EditPost.Post_TYPE, item.getPostType());
                args.putInt(EditPost.WEATHER, item.getWeather());

                fragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        });*/

        return view;
    }

    private void generatePosts() {
        posts.add(new Post(getString(R.string.anonymous), "My first post!", null));
        posts.add(new Post("Kathleen", "My second post!", null));
    }

    private class PostAdapter extends ArrayAdapter<Post> {
        private Context context;
        private List<Post> items;

        public PostAdapter(Context context, int textViewResourceId, List<Post> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.post, null);
            }

            Post p = items.get(position);
            if (p != null) {
                TextView username = (TextView) view.findViewById(R.id.username);
                ImageView picture = (ImageView) view.findViewById(R.id.picture);
                TextView message = (TextView) view.findViewById(R.id.message);
                TextView likes = (TextView) view.findViewById(R.id.likes);
                if (username != null) {
                    username.setText(p.getUsername());
                }

                if (picture != null && p.getDrawable() != null) {
                    picture.setBackgroundResource(p.getDrawable());
                }

                if (message != null) {
                    message.setText(p.getMsg());
                }

                if (likes != null) {
                    likes.setText("" + p.getNumLikes());
                }
            }
            return view;
        }
    }
}
