package com.teamhotspots.hotspots;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.System.exit;


public class PhotoConfirm extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static DatabaseReference mDatabase;
    private static StorageReference mStorage;
    private static SharedPreferences sharedPref;
    private boolean mLocationPermissionGranted = true;
    private View rootView;
    private Bitmap photo;
    private Uri path;
    private String imageUrl;
    private double lng;
    private double lat;
    private String username;
    private String usericon;
    private String msg;
    private String timeStamp;

    public PhotoConfirm() {
        // Required empty public constructor
    }

    public static PhotoConfirm newInstance(String param1, String param2) {
        PhotoConfirm fragment = new PhotoConfirm();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        sharedPref = getActivity().getPreferences(MODE_PRIVATE);

        if (getArguments() != null) {
            path = getArguments().getParcelable("path");
        } else {
            exit(1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_photo_confirm, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.white));

        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), path);
        } catch (IOException exception) {

        }

        int orientation = 0;

        try {
            ExifInterface exif = new ExifInterface(path.getPath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
        } catch (Exception e) {}

        bitmap = rotateImage(orientation, bitmap);

        ImageView photoView = (ImageView) rootView.findViewById(R.id.imageView6);
        //photoView.setImageURI(path);
        photoView.setImageBitmap(bitmap);

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

                //username
                username = sharedPref.getString(getString(R.string.username),
                        getString(R.string.anonymous));

                //user icon
                usericon = sharedPref.getString("userIcon",
                        "anonymousIcon");

                //anonymous or not
                Switch sw = (Switch) rootView.findViewById(R.id.switch1);
                if (sw.isChecked()) {
                    username = getString(R.string.anonymous);
                    usericon = "anonymousIcon";
                }

                //caption
                final EditText et = (EditText) rootView.findViewById(R.id.caption);
                msg = et.getText().toString();

                timeStamp = new Date().toString();

                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION )
                                != PackageManager.PERMISSION_GRANTED) {

                }

                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                    @Override
                    public void onStatusChanged(String dks, int nds, Bundle fdksl) { }

                    @Override
                    public void onProviderEnabled(String temp) { }

                    @Override
                    public void onProviderDisabled(String temp) { }

                };
                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
                Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lng = location.getLongitude();
                lat = location.getLatitude();

                //image
                StorageReference filepath = mStorage.child("Photos").child(path.getLastPathSegment());
                filepath.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageUrl = taskSnapshot.getDownloadUrl().toString();
                        writeNewPost(new Post(username, msg, imageUrl, usericon, timeStamp, lat, lng));

                        //to load onto feed
                        //Picasso.with(getContext()).load(downloadUri).into(imageView);
                    }
                });

                //return to previous activity
                getActivity().finish();
            }
        });
        return rootView;
    }


    public void writeNewPost(Post post) {
        String key = mDatabase.child("posts").push().getKey();
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }


    private int getImageOrientation(){
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION };
        final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageColumns, null, null, imageOrderBy);

        if(cursor.moveToFirst()){
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
            cursor.close();
            return orientation;
        } else {
            return 0;
        }
    }

    public Bitmap rotateImage(int orientation, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        }
        else if (orientation == 3) {
            matrix.postRotate(180);
        }
        else if (orientation == 8) {
            matrix.postRotate(270);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
