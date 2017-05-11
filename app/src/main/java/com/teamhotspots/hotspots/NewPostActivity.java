package com.teamhotspots.hotspots;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.provider.MediaStore.AUTHORITY;
import static java.lang.System.exit;

public class NewPostActivity extends AppCompatActivity implements
        PhotoConfirm.OnFragmentInteractionListener
{
    private static DatabaseReference mDatabase;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private static SharedPreferences sharedPref;

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
        sharedPref = getSharedPreferences(getString(R.string.pref), MODE_PRIVATE);


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

    }

    public void writeNewPost(Post post) {
        // Push the post details
        final DatabaseReference newPost = mDatabase.child("posts").push();
        newPost.setValue(post);

        // TODO: If post is in hotspot's range, update that hotspot's list of posts

        // e.g. hotspot.child("posts").push().setValue(postId)
        final String hotspotKey = "example-hotspot"; //TODO: Change this to an actual hotspot

        // To store a hotspot's list of posts, we store the post IDs as keys and 'true'
        // as the value. This is the recommended way to lists of keys in Firebase, see:
        // https://firebase.google.com/docs/database/android/structure-data
        mDatabase.child("hotspots/" + hotspotKey + "/posts").child(newPost.getKey()).setValue(true);

        // Update user activity (stored in shared prefs)
        String created = sharedPref.getString("CREATED", "");
        SharedPreferences.Editor editor = sharedPref.edit();
        StringBuilder sb = new StringBuilder(created);
        sb.append(newPost.getKey() + ",");
        editor.putString("CREATED", sb.toString());
        editor.commit();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TextFragment extends Fragment {

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

            final Button button_cancel = (Button) rootView.findViewById(R.id.cancel);
            button_cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            final Button button_submit = (Button) rootView.findViewById(R.id.submit);
            button_submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    int psts = sharedPref.getInt("NUM_POSTS", 0);
                    psts += 1;
                    editor.putInt("NUM_POSTS", psts);
                    editor.commit();

                    //TODO update this if post becomes a hotspot centroid
                    //int htspts = sharedPref.getInt("NUM_HTSPT", 0);
                    //htspts += 1;
                    //editor.putInt("NUM_HTSPT", psts);
                    //editor.commit();


                    String username = sharedPref.getString(getString(R.string.username),
                            getString(R.string.anonymous));

                    Switch sw = (Switch) rootView.findViewById(R.id.switch1);
                    if (sw.isChecked()) {
                        username = getString(R.string.anonymous);
                    }

                    String msg = et.getText().toString();
                    String imageUrl = null;
                    //user icon path
                    String userIcon = sharedPref.getString("ICON_PATH",
                            "anonymousIcon");
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
                                .writeNewPost(new Post(username, msg, imageUrl, userIcon, timeStamp,
                                        lat, lng));
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
                    checkPermission();
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

        public void checkPermission() {
            if (ContextCompat.checkSelfPermission(getActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
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
