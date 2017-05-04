package com.teamhotspots.hotspots;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kathleen on 4/7/2017.
 */

public class Feed extends Fragment {
    private ListView postsListView;
    private PostAdapter posts;
    private Post itemSelected;
    private DatabaseReference mReference;

    private ValueEventListener hotspotValueListener;
    private ValueEventListener postsValueListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_feed,
                container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Feed");

        // generatePosts();
        mReference = FirebaseDatabase.getInstance().getReference();
        postsListView = (ListView) view.findViewById(R.id.feed_list);
        posts = new PostAdapter(getActivity(), R.layout.post, new ArrayList<Post>());
        postsListView.setAdapter(posts);
        registerForContextMenu(postsListView);

        //for now we won't worry about populating this
        //TextView community = (TextView) view.findViewById(R.id.community_name);
        //community.setText("Johns Hopkins University");

        // TODO: Get hotspot key from bundled arguments
        //Bundle b = getArguments();
        //String hotspotKey = b.getString("hotspotKey");
        String hotspotKey = "example-hotspot";
        final List<String> postKeys = new ArrayList<>();

        hotspotValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Hotspot h = dataSnapshot.getValue(Hotspot.class);
                postKeys.clear();
                postKeys.addAll(h.posts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mReference.child("hotspots").child(hotspotKey).addValueEventListener(hotspotValueListener);

        // TODO: Switch to ChildEventListener to detect when posts are created/removed/edited
        // This avoids iterating through every post. Do a SingleValueEventListener when the feed
        // is created

        postsValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (postKeys.contains(d.getKey())) {
                        Post p = d.getValue(Post.class);
                        posts.add(p);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.child("posts").orderByKey().addValueEventListener(postsValueListener);

        postsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Post p = (Post) postsListView.getItemAtPosition(position);
                itemSelected = p;
                if (p.isPicturePost()) postsListView.showContextMenu();
                return true;
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Remove Firebase ValueEventListeners, or else the fragment will continue listening
        // after being detached from the activity.
        mReference.child("posts").removeEventListener(postsValueListener);
        mReference.child("hotspots").removeEventListener(hotspotValueListener);
    }
/*
    private void generatePosts() {
        posts.add(new Post(getString(R.string.anonymous), "My first post!"));
        posts.add(new Post("Kathleen", "My second post!"));
        posts.add(new Post("Kathleen", "This is a really really really really really really really " +
                "really really really really really really really really really really really really" +
                " really really LONG post!"));
        posts.add(new Post("PAWS", "Dogs at The Beach, 3 to 5 pm!"));
        posts.add(new Post("Hoot", "Look at this bird!"));
    }*/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Save to Gallery"); // groupId, itemId, order, title
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if (item.getTitle()=="Save to Gallery") {
            //need to get image bitmap, need to pull image from entry
            Bitmap b = getBitmapFromURL(itemSelected.getImageUrl());
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), b, null, null);
            Toast.makeText(getActivity().getApplicationContext(),"Saved to gallery!",Toast.LENGTH_LONG).show();
        } else {
            return false;
        }
        return true;
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
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
                ImageView icon = (ImageView) view.findViewById(R.id.user_icon);
                TextView message = (TextView) view.findViewById(R.id.message);
                TextView likes = (TextView) view.findViewById(R.id.likes);
                if (username != null) {
                    username.setText(p.getUsername());
                }

                if (p.getUsername().equals(getString(R.string.anonymous)) || p.getUsericon().equals("anonymousIcon")) {
                    icon.setImageResource(R.drawable.ic_person_outline_black_24dp);
                } else {
                    //may need to format size
                    Picasso.with(getContext()).load(p.getUsericon()).into(icon);
                }

                if (picture != null && p.isPicturePost()) {
                    Picasso.with(getContext()).load(p.getImageUrl()).into(picture);
                    picture.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = picture.getLayoutParams();
                    params.height = dpToPx(getActivity().getApplicationContext(), 200);
                } else if (picture!= null && !p.isPicturePost()) {
                    picture.setVisibility(View.GONE);
                    picture.setBackgroundResource(0);
                    ViewGroup.LayoutParams params = picture.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }

                if (message != null) {
                    message.setText(p.getMsg());
                }

                if (likes != null) {
                    likes.setText("" + p.getNumLikes());
                }

                ImageView thumbIcon = (ImageView) view.findViewById(R.id.like_icon);
                thumbIcon.setOnClickListener(new ThumbIconOnClickListener(p, thumbIcon, likes));
            }
            return view;
        }
    }

    private class ThumbIconOnClickListener implements View.OnClickListener {
        private Post post;
        private TextView likes;
        private ImageView thumbIcon;
        private boolean pressed = false;

        public ThumbIconOnClickListener(Post post, ImageView thumbIcon, TextView likes) {
            this.post = post;
            this.thumbIcon = thumbIcon;
            this.likes = likes;
        }

        @Override
        public void onClick(View view) {
            if (!pressed) {
                this.pressed = true;
                post.upvote();
                thumbIcon.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
            } else {
                this.pressed = false;
                post.undoVote();
                thumbIcon.clearColorFilter();
            }

            this.likes.setText("" + post.getNumLikes());
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


}
