package com.teamhotspots.hotspots;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapHome extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    public MapHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_home, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Map settings (besides those that are set in fragment_map_home.xml)
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(false);
        tryEnablingMyLocation();

        LatLng classroom = new LatLng(39.327578, -76.619574);
        mMap.addMarker(new MarkerOptions().position(classroom).title("My Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(classroom));
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
