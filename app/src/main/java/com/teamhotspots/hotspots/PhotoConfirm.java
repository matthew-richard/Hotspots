package com.teamhotspots.hotspots;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.exit;


public class PhotoConfirm extends Fragment {

    private Bitmap photo;
    private Uri path;

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
        View rootView = inflater.inflate(R.layout.fragment_photo_confirm, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.white));

        File myFile = new File(path.getPath());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), path);
        } catch (IOException exception) {

        }
        try {
            ExifInterface exif = new ExifInterface(myFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
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
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {

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
                //TODO: package all fields into a database entry and add to database
                //username, or anonymous
                //user icon
                //photo field
                //text field (caption)
                //current location and which square it would map to
                //time created

                //return to previous activity
                getActivity().finish();
            }
        });

        return rootView;

    }



    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
