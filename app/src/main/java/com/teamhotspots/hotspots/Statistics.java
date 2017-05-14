package com.teamhotspots.hotspots;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static com.teamhotspots.hotspots.R.layout.post;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Statistics.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Statistics#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Statistics extends Fragment {
    private String userID;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    public Statistics() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.userID = user.getUid();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Statistics");
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);

        userRef = FirebaseDatabase.getInstance().getReference().child("users/" + userID);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int numPostsCreated = (int) dataSnapshot.child("postsCreated").getChildrenCount();
                int numLikes = (int) dataSnapshot.child("likesReceived").getChildrenCount();
                int numHotspotsCreated = (int) dataSnapshot.child("hotspotsCreated").getChildrenCount();

                TextView points = (TextView) rootView.findViewById(R.id.stat_total_pts_num);
                points.setText(Integer.toString(numLikes));

                TextView posts = (TextView) rootView.findViewById(R.id.stat_total_posts_num);
                posts.setText(Integer.toString(numPostsCreated));

                TextView hotspotsCreated = (TextView) rootView.findViewById(R.id.stat_hotspots_num);
                hotspotsCreated.setText(Integer.toString(numHotspotsCreated));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
        userRef.addValueEventListener(userListener);

        return rootView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        userRef.removeEventListener(userListener);
    }
}
