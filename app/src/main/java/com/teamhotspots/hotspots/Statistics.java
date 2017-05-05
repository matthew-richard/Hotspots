package com.teamhotspots.hotspots;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private OnFragmentInteractionListener mListener;
    private int number_likes = 0;
    private SharedPreferences sharedPref;
    private String pts;
    private String[] parts;

    public Statistics() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static Statistics newInstance(String param1, String param2) {
        Statistics fragment = new Statistics();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Statistics");
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);

        sharedPref = getActivity().getSharedPreferences(getString(R.string.pref), MODE_PRIVATE);
        String pts = sharedPref.getString("CREATED", "");
        Log.d("created", pts);

        if (!isEmpty(pts)) {
            parts = pts.split(",");

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            for (int i = 0; i < parts.length; i++) {
                DatabaseReference postsRef = database.getReference().child("posts").child(parts[i]);
                //Log.d("created", postsRef.getKey());

                postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("created", dataSnapshot.getKey());
                        Post p = dataSnapshot.getValue(Post.class);
                        number_likes += p.getNumLikes();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
            }
        }

        int psts = sharedPref.getInt("NUM_POSTS", 0);
        int htspts = sharedPref.getInt("NUM_HTSPT", 0);

        TextView points = (TextView) rootView.findViewById(R.id.stat_total_pts_num);
        points.setText(Integer.toString(number_likes));

        TextView posts = (TextView) rootView.findViewById(R.id.stat_total_posts_num);
        posts.setText(Integer.toString(psts));

        TextView hotspots = (TextView) rootView.findViewById(R.id.stat_hotspots_num);
        hotspots.setText(Integer.toString(htspts));

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
