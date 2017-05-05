package com.teamhotspots.hotspots;

import android.*;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        Settings.OnFragmentInteractionListener,
        Statistics.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    /* For requesting location permissions at runtime */
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private NavigationView navigationView;
    private MapHome mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                SharedPreferences sharedPref =  getSharedPreferences("PREF", MODE_PRIVATE);
                String username = sharedPref.getString(getString(R.string.username), "John Doe");
                TextView usernameTV = (TextView) findViewById(R.id.username);
                usernameTV.setText(username);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapHome();
        fragmentManager.beginTransaction().replace(R.id.content_frame, mapFragment).commit();

        setTitle("Home");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;
        boolean newActivityStarted = false;
        boolean addToBackStack = true;

        if (id == R.id.nav_home) {
            fragmentClass = MapHome.class;
            addToBackStack = false;
        } else if (id == R.id.nav_new_pin) {
            Intent intent = new Intent(this, NewPostActivity.class);
            startActivity(intent);
            newActivityStarted = true;
        } else if (id == R.id.nav_statistics) {
            fragmentClass = Statistics.class;
        } else if (id == R.id.nav_settings) {
            fragmentClass = Settings.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {

            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        if (fragmentClass != null) {
            setTitle(item.getTitle());

            FragmentManager fragmentManager = getSupportFragmentManager();
            if (addToBackStack) {
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .addToBackStack(fragmentClass.getName()).commit();
            }
            else {
                // Pop entire back stack (only occurs when when "home" is selected)
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }

        // Close the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Don't display navigation item as selected if a new activity was started
        return !newActivityStarted;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //leave empty
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle user's decision to grant or deny location permissions to the app.
        // See https://developer.android.com/training/permissions/requesting.html
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mapFragment.tryRequestingLocationUpdates();
                }
            }
            // TODO: Handle permissions result for external storage
        }

        return;
    }

    // launch new post activity
    public void launchPost(View view) {
        Intent intent = new Intent(this, NewPostActivity.class);
        startActivity(intent);
    }

}
