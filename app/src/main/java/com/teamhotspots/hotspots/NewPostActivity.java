package com.teamhotspots.hotspots;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.MediaStore.AUTHORITY;
import static java.lang.System.exit;

public class NewPostActivity extends AppCompatActivity implements
        PhotoConfirm.OnFragmentInteractionListener
{
    private static DatabaseReference mDatabase;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private boolean permission = false;
    FirebaseUser user;
    private DatabaseReference mReference;
    private ChildEventListener hotspotsChildEventListener;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();


        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this , R.color.black));

        Fragment fragment = null;
        Class fragmentClass = NewPostTab.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }



        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();

        mRequestExternal();

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void mRequestExternal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permission = true;
        } else if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permission = true;
        } else if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(this.findViewById(android.R.id.content)
                    , R.string.permission_rationale_external, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    });
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permission = true;
            }
        }
    }


    public void writeNewPost(Post post) {
        // Push the post details
        final DatabaseReference newPost = mDatabase.child("posts/"
                + AreaEventListener.getSquare(post.getLat(), post.getLon())).push();
        newPost.setValue(post);

        final int hotspotCircleRadius = Integer.parseInt(getString(R.string.hotspotCircleRadius));
        final LatLng postLatLng = new LatLng(post.getLat(), post.getLon());

        // Generate new hotspot if necessary
        new AreaEventListener(postLatLng, hotspotCircleRadius) {
            @Override
            public boolean onDataChange(Collection<DataSnapshot> hotspots,
                                        Collection<DataSnapshot> posts)
            {
                boolean hotspotTooClose = false;
                int hotspotCreationThreshold = Integer.parseInt(getString(
                        R.string.hotspotCreationThreshold));
                float[] dist = new float[1];

                // Check if any hotspot is already within range of this new post
                for (DataSnapshot h : hotspots) {
                    Hotspot hotspot = h.getValue(Hotspot.class);
                    Location.distanceBetween(postLatLng.latitude, postLatLng.longitude, hotspot.lat,
                            hotspot.lng, dist);
                    if (dist[0] <= hotspotCircleRadius) {
                        hotspotTooClose = true;
                        break;
                    }
                }

                // If there are enough hotspots
                if (!hotspotTooClose && hotspots.size() >= hotspotCreationThreshold) {
                    // Create hotspot at new post's location
                    DatabaseReference square = mDatabase.child("hotspots/" +
                            AreaEventListener.getSquare(postLatLng.latitude, postLatLng.longitude)
                            + "/");

                    DatabaseReference newHotspot = square.push();
                    newHotspot.setValue(new Hotspot(postLatLng.latitude, postLatLng.longitude,
                            new HashMap<String, Boolean>()));
                }

                // Stop listening
                return false;
            }

            @Override
            public void onHotspotAdded(DataSnapshot hotspot) {}
            @Override
            public void onPostAdded(DataSnapshot post) {}
            @Override
            public void onHotspotChanged(DataSnapshot hotspot) {}
            @Override
            public void onPostChanged(DataSnapshot post) {}
            @Override
            public void onHotspotRemoved(DataSnapshot hotspot) {}
            @Override
            public void onPostRemoved(DataSnapshot post) {}
        };

        // Update user activity (stored in shared prefs)
        /* String created = sharedPref.getString("CREATED", "");
        SharedPreferences.Editor editor = sharedPref.edit();
        StringBuilder sb = new StringBuilder(created);
        sb.append(newPost.getKey() + ",");
        editor.putString("CREATED", sb.toString());
        editor.commit(); */

        // TODO: Update user activity
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TextFragment extends Fragment {

        FirebaseUser user;

        public TextFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TextFragment newInstance() {
            return new TextFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_new_post, container, false);
            final EditText et = (EditText) rootView.findViewById(R.id.postText);
            et.setSelection(et.getText().length());

            user = FirebaseAuth.getInstance().getCurrentUser();


            final Button button_cancel = (Button) rootView.findViewById(R.id.cancel);
            button_cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            final Button button_submit = (Button) rootView.findViewById(R.id.submit);
            button_submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //TODO update this if post becomes a hotspot centroid
                    int hotspotCreated = 0;
                    //if hotspot Created by this post, set hotspotCreated to 1 - this is for statistics

                    String username = user.getDisplayName();
                    if (username == null) {
                        username = "Anonymous";
                    }

                    //user icon path
                    String userIcon = "anonymousIcon";
                    try {
                        userIcon = user.getPhotoUrl().toString();
                    } catch (Exception e){
                    }

                    if (userIcon == null) {
                        userIcon = "anonymousIcon";
                    }


                    Switch sw = (Switch) rootView.findViewById(R.id.switch1);
                    if (sw.isChecked()) {
                        username = getString(R.string.anonymous);
                        userIcon = "anonymousIcon";
                    }

                    String msg = et.getText().toString();


                    String timeStamp = new Date().toString();

                    LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    try {
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        ((NewPostActivity) getActivity())
                                .writeNewPost(new Post(username, msg, "none", userIcon, timeStamp,
                                        lat, lng, hotspotCreated));
                    } catch (SecurityException e) {
                        Toast.makeText(getActivity(), "Should add location permission, post not uploaded.", Toast.LENGTH_LONG).show();
                    }

                    //return to previous activity
                    getActivity().finish();
                }
            });

            return rootView;
        }
    }

    public static class PhotoFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        static final int REQUEST_IMAGE_CAPTURE = 1;
        static final int REQUEST_IMAGE_PICKER = 2;
        private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
        FirebaseUser user;


        private View mView;
        Uri mPhotoUri;

        public PhotoFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PhotoFragment newInstance() {
            return new PhotoFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
            mView = rootView;
            user = FirebaseAuth.getInstance().getCurrentUser();


            if (ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Need Storage Permissions!", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }

            final Button button_cancel = (Button) rootView.findViewById(R.id.button2);
            button_cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            LinearLayout camera = (LinearLayout) rootView.findViewById (R.id.linearLayout);

            camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mPhotoUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            new ContentValues());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            });

            LinearLayout gallery = (LinearLayout) rootView.findViewById (R.id.linearLayout2);
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_IMAGE_PICKER);
                }
            });



            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            //go to photo confirm
            Fragment fragment = null;
            Class fragmentClass;

            fragmentClass = PhotoConfirm.class;

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                //Uri selectedImageUri = data.getData();
                Bundle args = new Bundle();
                args.putParcelable("path", mPhotoUri);

                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment).addToBackStack(null).commit();

            } else if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                Bundle args = new Bundle();
                args.putParcelable("path", selectedImageUri);
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment).addToBackStack(null).commit();

            }
        }

    }



    public static class NewPostTab extends Fragment {
        private PagerAdapter pagerAdapter;
        private ViewPager mViewPager;


        public NewPostTab() {
            // Required empty public constructor
        }

        public static NewPostTab newInstance(String param1, String param2) {
            NewPostTab fragment = new NewPostTab();
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
            View rootView = inflater.inflate(R.layout.fragment_new_post_tab, container, false);
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            pagerAdapter = new FixedTabsPagerAdapter(getActivity().getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) rootView.findViewById(R.id.contain);
            mViewPager.setAdapter(pagerAdapter);

            TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(mViewPager);

            return rootView;
        }

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class FixedTabsPagerAdapter extends FragmentPagerAdapter {

        public FixedTabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return TextFragment.newInstance();
                case 1:
                    return PhotoFragment.newInstance();
            }
            return TextFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "TEXT";
                case 1:
                    return "PHOTO";
            }
            return null;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //leave empty
    }
}
