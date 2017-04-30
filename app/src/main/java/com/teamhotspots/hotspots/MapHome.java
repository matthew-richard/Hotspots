package com.teamhotspots.hotspots;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class MapHome extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static View mapView;

    public MapHome() {
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
        if (mapView != null) {
            ViewGroup parent = (ViewGroup) mapView.getParent();
            if (parent != null)
                parent.removeView(mapView);
        }
        try {
            mapView = inflater.inflate(R.layout.fragment_map_home, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        return mapView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Map settings (besides those that are set in fragment_map_home.xml)
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(false);
        tryEnablingMyLocation();

        LatLng curr_location = new LatLng(39.327578, -76.619574); //get current location

        MarkerOptions marker = new MarkerOptions().position(curr_location).title(null);

        Bitmap ic1 = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_chat_black_24dp);
        Bitmap resized_ic1 = Bitmap.createScaledBitmap(ic1, 200, 200, false);
        marker.icon(BitmapDescriptorFactory.fromBitmap(resized_ic1));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curr_location));

        //populate map
        //for each Hotspot, if within our map bounds, then create Hotspot marker

        //for each Hotspot in Database, get lat/lng coordinates
        float lat = 0;
        float lng = 0;

        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;

        /*
        if (curScreen.contains(null)) {

        }*/

        MarkerOptions marker2 = new MarkerOptions().position(new LatLng(39.329159, -76.618424)).title("hotspot");
        Bitmap ic2 = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_whatshot);
        Bitmap resized_ic2 = Bitmap.createScaledBitmap(ic2, 200, 200, false);
        marker2.icon(BitmapDescriptorFactory.fromBitmap(resized_ic2));

        MarkerOptions marker3 = new MarkerOptions().position(new LatLng(39.326702, -76.620296)).title("hotspot2");
        Bitmap ic3 = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_whatshot);
        Bitmap resized_ic3 = Bitmap.createScaledBitmap(ic3, 200, 200, false);
        marker3.icon(BitmapDescriptorFactory.fromBitmap(resized_ic3));

        mMap.addMarker(marker);
        mMap.addMarker(marker2);
        mMap.addMarker(marker3);

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        //go to feed

        //get marker lat/lng
        Fragment fragment = null;
        Class fragmentClass = null;

        fragmentClass = Feed.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        return false;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void tryEnablingMyLocation() {
        try { mMap.setMyLocationEnabled(true); }
        catch (SecurityException e) {
            // Asynchronously request location permission.
            // Calls MainActivity.onRequestPermissionsResult
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            /*TODO: Have map display error message if no permissions */
            /* E.g "Hotspots requires location permissions to work." */

            /*TODO: Check for permissions on every user interaction with map */
        }
    }
}
