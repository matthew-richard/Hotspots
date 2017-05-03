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
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.location.LocationServices;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

// TODO: Add button to UI for re-centering on current location

public class MapHome extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener
{
    public static final int LOCATION_REQUEST_INTERVAL_SEC = 5;
    public static final int LOCATION_REQUEST_FASTEST_INTERVAL_SEC = 3;

    private GoogleMap mMap;
    private Location lastLocation;
    private Marker locationMarker;
    private Circle locationCircle;
    private ArrayList<Marker> hotspotMarkers;
    private ArrayList<Circle> hotspotCircles;

    private static View mapView;
    private static GoogleApiClient googleApiClient;

    public MapHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize googleApiClient */
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // TODO: handle Google Services API connection failure
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        // Start requesting location updates once we've connected
                        // to the Google Services API
                        tryRequestingLocationUpdates();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // TODO: handle Google Services API connection suspension
                    }
                })
                .addApi(LocationServices.API)
                .build();
        }

        locationMarker = null;
        locationCircle = null;
        hotspotMarkers = new ArrayList<>();
        hotspotCircles = new ArrayList<>();
    }

    public void tryRequestingLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    new LocationRequest()
                            .setInterval(LOCATION_REQUEST_INTERVAL_SEC * 1000)
                            .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL_SEC * 1000)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                    this);

            enablePermissionsErrorMessage(false);

            // TODO: Pause location requests in onStop(), resume in onStart()
            // Just make sure that only one location request is running at a time.
        }
        catch (SecurityException e) {
            // Asynchronously request location permission.
            // Callback is MainActivity.onRequestPermissionsResult
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            enablePermissionsErrorMessage(true);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        // if marker doesn't exist, create it. otherwise move it.
        if (locationMarker == null) {
            Bitmap ic1 = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_chat_black_24dp);
            Bitmap resized_ic1 = Bitmap.createScaledBitmap(ic1, 200, 200, false);

            locationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title(null)
                    .icon(BitmapDescriptorFactory.fromBitmap(resized_ic1))
                    .anchor(
                        Float.parseFloat(getString(R.string.locationIconAnchorX)),
                        Float.parseFloat(getString(R.string.locationIconAnchorY)))
            );

            locationCircle = mMap.addCircle(new CircleOptions()
                    .center(latlng)
                    .fillColor(ContextCompat.getColor(getContext(), R.color.locationCircleFill))
                    .strokeColor(ContextCompat.getColor(getContext(), R.color.locationCircleStroke))
                    .strokeWidth(Float.parseFloat(getString(R.string.locationCircleStrokeWidth)))
                    .radius(Float.parseFloat(getString(R.string.locationCircleRadius))));


            // Center camera on the first location received
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        }
        else {
            locationMarker.setPosition(latlng);
            locationCircle.setCenter(latlng);
        }
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

        mapView.findViewById(R.id.my_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerOnMyLocation();
            }
        });

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        return mapView;
    }

    public void centerOnMyLocation() {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder(mMap.getCameraPosition())
                        .bearing(0)
                        .target(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                        .build()
        ));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // TODO: Check if this is the first time onMapReady() is running before drawing markers
        // Figure out when onMapReady() even runs. It seems to run every time the fragment returns
        // to view...

        // TODO: Set Activity title to "Hotspots", with the flame icon.

        mMap = map;

        // Map settings (besides those that are set in fragment_map_home.xml)
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(false);
        mMap.setMapStyle(new MapStyleOptions(getString(R.string.mapStyle)));

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);

        // Data markers:
        // - Fetch all pins in local (relative to user) area, display in feed
        // - Fetch all hotspots visible on the map (onMapCameraMove?), display on map, with radii

        //populate map
        //for each Hotspot, if within our map bounds, then create Hotspot marker

        //for each Hotspot in Database, get lat/lng coordinates
        float lat = 0;
        float lng = 0;

        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;

        /*if (curScreen.contains(mylatlng)) {

        }*/

        MarkerOptions marker2 = new MarkerOptions().position(new LatLng(39.329159, -76.618424)).title("hotspot");
        Bitmap ic2 = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_whatshot);
        Bitmap resized_ic2 = Bitmap.createScaledBitmap(ic2, 200, 200, false);
        marker2.icon(BitmapDescriptorFactory.fromBitmap(resized_ic2))
                .anchor(Float.parseFloat(getString(R.string.hotspotIconAnchorX)),
                        Float.parseFloat(getString(R.string.hotspotIconAnchorY)));
        hotspotMarkers.add(mMap.addMarker(marker2));

        MarkerOptions marker3 = new MarkerOptions().position(new LatLng(39.326702, -76.620296)).title("hotspot2");
        Bitmap ic3 = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_whatshot);
        Bitmap resized_ic3 = Bitmap.createScaledBitmap(ic3, 200, 200, false);
        marker3.icon(BitmapDescriptorFactory.fromBitmap(resized_ic3))
               .anchor(Float.parseFloat(getString(R.string.hotspotIconAnchorX)),
                       Float.parseFloat(getString(R.string.hotspotIconAnchorY)));
        hotspotMarkers.add(mMap.addMarker(marker3));

        for (int i = 0 ; i < hotspotMarkers.size(); i++) {
            hotspotCircles.add(mMap.addCircle(new CircleOptions()
                    .center(hotspotMarkers.get(i).getPosition())
                    .fillColor(ContextCompat.getColor(getContext(), R.color.hotspotCircleFill))
                    .strokeColor(ContextCompat.getColor(getContext(), R.color.hotspotCircleStroke))
                    .strokeWidth(Float.parseFloat(getString(R.string.hotspotCircleStrokeWidth)))
                    .radius(Float.parseFloat(getString(R.string.hotspotCircleRadius)))));
        }

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


    protected void enablePermissionsErrorMessage(boolean enabled) {
        if (enabled) {
            mapView.findViewById(R.id.map).setVisibility(View.GONE);
            mapView.findViewById(R.id.new_post).setVisibility(View.GONE);
            mapView.findViewById(R.id.permissions_error_msg).setVisibility(View.VISIBLE);
        }
        else {
            mapView.findViewById(R.id.map).setVisibility(View.VISIBLE);
            mapView.findViewById(R.id.new_post).setVisibility(View.VISIBLE);
            mapView.findViewById(R.id.permissions_error_msg).setVisibility(View.GONE);
        }
    }

}
