package com.teamhotspots.hotspots;

import android.*;
import android.Manifest;
import android.content.Context;
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
import android.support.v4.content.CursorLoader;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.System.exit;


public class PhotoConfirm extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static DatabaseReference mDatabase;
    private static StorageReference mStorage;
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
    private int hotspotCreated;
    FirebaseUser user;


    public PhotoConfirm() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();


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
            ExifInterface exif = new ExifInterface(getPath(path));
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
        } catch (Exception e) {}

        bitmap = rotateImage(orientation, bitmap);

        try {
            OutputStream os= getContext().getContentResolver().openOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG,50,os);
        } catch (Exception e) {
        }


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

                //username
                username = user.getDisplayName();
                if (username == null) {
                    username = "Anonymous";
                }

                //user icon path
                usericon = null;
                try {
                    usericon = user.getPhotoUrl().toString();
                } catch (Exception e){
                }

                if (usericon == null) {
                    usericon = "anonymousIcon";
                }

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

                lat = getArguments().getDouble("latitude");
                lng = getArguments().getDouble("longitude");

                //image
                final NewPostActivity activity = (NewPostActivity) getActivity();
                StorageReference filepath = mStorage.child("Photos").child(path.getLastPathSegment());
                filepath.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageUrl = taskSnapshot.getDownloadUrl().toString();

                        activity.writeNewPost(new Post(username, msg, imageUrl, usericon, timeStamp,
                                lat, lng, false));

                    }
                });

                //return to previous activity
                getActivity().finish();
            }
        });
        return rootView;
    }

    private String getPath(Uri uri) {
        String[]  data = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getContext(), uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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
