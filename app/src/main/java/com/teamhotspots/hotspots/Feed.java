package com.teamhotspots.hotspots;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
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
    private Post itemSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_feed,
                container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Feed");

        generatePosts();
        postsListView = (ListView) view.findViewById(R.id.feed_list);
        adapter = new PostAdapter(getActivity(), R.layout.post, posts);
        postsListView.setAdapter(adapter);

        TextView community = (TextView) view.findViewById(R.id.community_name);
        community.setText("Johns Hopkins University");

        registerForContextMenu(postsListView);

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

    private void generatePosts() {
        posts.add(new Post(getString(R.string.anonymous), "My first post!", null));
        posts.add(new Post("Kathleen", "My second post!", null));
        posts.add(new Post("Kathleen", "This is a really really really really really really really " +
                "really really really really really really really really really really really really" +
                " really really LONG post!", null));
        posts.add(new Post("PAWS", "Dogs at The Beach, 3 to 5 pm!", R.drawable.husky));
        posts.add(new Post("Hoot", "Look at this bird!", R.drawable.bird_ockatiel));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Save to Gallery"); // groupId, itemId, order, title
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if (item.getTitle()=="Save to Gallery"){
            //need to get image bitmap, need to pull image from entry
            Bitmap icon = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), itemSelected.getDrawable());
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), icon, null , null);
            Toast.makeText(getActivity().getApplicationContext(),"Saved to gallery!",Toast.LENGTH_LONG).show();
        } else {
            return false;
        }
        return true;
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

                if (icon != null && p.getUsername().equals(getString(R.string.anonymous))) {
                    icon.setImageResource(R.drawable.ic_person_outline_black_24dp);
                } else if (icon != null && !p.getUsername().equals(getString(R.string.anonymous))) {
                    icon.setImageResource(R.drawable.img_bird1);
                }

                if (picture != null && p.isPicturePost()) {
                    picture.setBackgroundResource(p.getDrawable());
                    picture.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = picture.getLayoutParams();
                    params.height = dpToPx(getActivity().getApplicationContext(), 200);
                } else if (picture!= null && !p.isPicturePost()) {
                    picture.setVisibility(View.INVISIBLE);
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
