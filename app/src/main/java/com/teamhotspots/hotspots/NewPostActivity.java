package com.teamhotspots.hotspots;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
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

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;

import static java.lang.System.exit;

public class NewPostActivity extends AppCompatActivity implements
        PhotoConfirm.OnFragmentInteractionListener
{

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

        Fragment fragment = null;
        Class fragmentClass = NewPostTab.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment).addToBackStack(null).commit();

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
            View rootView = inflater.inflate(R.layout.fragment_new_post, container, false);
            EditText et = (EditText) rootView.findViewById(R.id.postText);
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
                    //TODO: package all fields into a database entry and add to database
                    //username, or anonymous
                    //user icon
                    //text field
                    //current location and which square it would map to
                    //time created

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

        private View mView;

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
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                }
            });

            LinearLayout gallery = (LinearLayout) rootView.findViewById (R.id.linearLayout2);
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    //Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    //photoPickerIntent.setType("image/*");
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
                Uri selectedImageUri = data.getData();

                Bundle args = new Bundle();
                args.putParcelable("path", selectedImageUri);

                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                //ByteArrayOutputStream bs = new ByteArrayOutputStream();
                //photo.compress(Bitmap.CompressFormat.JPEG, 100, bs);
                //Bundle args = new Bundle();
                //args.putByteArray("byteArray", bs.toByteArray());

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
