package com.teamhotspots.hotspots;

import android.location.Location;
import android.provider.ContactsContract;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Matt on 5/12/2017.
 */

public abstract class AreaEventListener {

    private static DatabaseReference db;

    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    private List<DatabaseReference> hotspotSquareRefs;
    private List<DatabaseReference> postSquareRefs;
    private Map<DatabaseReference, Boolean> squareRefsFetched;

    private Map<DatabaseReference, DataSnapshot> hotspots;
    private Map<DatabaseReference, DataSnapshot> posts;

    private LatLng circleCenter;
    private int circleRadius;

    public AreaEventListener(LatLngBounds area) {
        circleCenter = null;
        initialize(area);
    }

    public AreaEventListener(LatLng circleCenter, int circleRadius) {
        this.circleCenter = circleCenter;
        this.circleRadius = circleRadius;
        initialize(getCircleBounds());
    }

    private void initialize(LatLngBounds area) {
        if (db == null) {
            db = FirebaseDatabase.getInstance().getReference();
        }

        hotspotSquareRefs = new ArrayList<>();
        postSquareRefs = new ArrayList<>();
        squareRefsFetched = new HashMap<>();
        hotspots = new HashMap<>();
        posts = new HashMap<>();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                squareRefsFetched.put(dataSnapshot.getRef(), true);

                // Call onDataChange() only after data has been received about every square
                if (allSquaresFetched()) {
                    boolean dataChangeResult = AreaEventListener.this.onDataChange(
                            hotspots.values(), posts.values());

                    if (!dataChangeResult) {
                        stopListening();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Hotspot hotspot = dataSnapshot.getValue(Hotspot.class);
                    if (!isCircle() || inCircle(hotspot)) {
                        hotspots.put(dataSnapshot.getRef(), dataSnapshot);
                        onHotspotAdded(dataSnapshot);
                    }
                } catch (Exception e) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (!isCircle() || inCircle(post)) {
                        posts.put(dataSnapshot.getRef(), dataSnapshot);
                        onPostAdded(dataSnapshot);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    Hotspot hotspot = dataSnapshot.getValue(Hotspot.class);
                    if (!isCircle() || inCircle(hotspot)) {
                        hotspots.put(dataSnapshot.getRef(), dataSnapshot);
                        onHotspotChanged(dataSnapshot);
                    }
                } catch (Exception e) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (!isCircle() || inCircle(post)) {
                        posts.put(dataSnapshot.getRef(), dataSnapshot);
                        onPostChanged(dataSnapshot);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    Hotspot hotspot = dataSnapshot.getValue(Hotspot.class);
                    if (!isCircle() || inCircle(hotspot)) {
                        hotspots.remove(dataSnapshot.getRef());
                        onHotspotRemoved(dataSnapshot);
                    }
                } catch (Exception e) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (!isCircle() || inCircle(post)) {
                        posts.remove(dataSnapshot.getRef());
                        onPostRemoved(dataSnapshot);
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        List<String> squares = getOverlappingSquares(area);
        for (String square : squares) {
            DatabaseReference hotspotSquareRef = db.child("/hotspots/" + square);
            DatabaseReference postSquareRef = db.child("/posts/" + square);

            hotspotSquareRefs.add(hotspotSquareRef);
            postSquareRefs.add(postSquareRef);

            squareRefsFetched.put(hotspotSquareRef, false);
            squareRefsFetched.put(postSquareRef, false);
        }
    }

    private boolean inCircle(Hotspot hotspot) {
        if (!isCircle()) return false;

        float[] dist = new float[1];
        Location.distanceBetween(hotspot.lat, hotspot.lng, circleCenter.latitude,
                circleCenter.longitude, dist);
        return dist[0] <= circleRadius;
    }

    private boolean inCircle(Post post) {
        if (!isCircle()) return false;

        float[] dist = new float[1];
        Location.distanceBetween(post.getLat(), post.getLng(), circleCenter.latitude,
                circleCenter.longitude, dist);
        return dist[0] <= circleRadius;
    }

    public boolean isCircle() {
        return circleCenter != null;
    }

    /**
     * Called when all squares in the area have been loaded.
     *
     * @return False if this AreaEventListener should stop listening
     * after the first call to onDataChange(), i.e. this is a single-value event listener.
     * True if it should continue listening.
     */
    public abstract boolean onDataChange(Collection<DataSnapshot> hotspots,
                                         Collection<DataSnapshot> posts);

    /** Child events **/
    public abstract void onHotspotAdded(DataSnapshot hotspot);
    public abstract void onPostAdded(DataSnapshot post);
    public abstract void onHotspotChanged(DataSnapshot hotspot);
    public abstract void onPostChanged(DataSnapshot post);
    public abstract void onHotspotRemoved(DataSnapshot hotspot);
    public abstract void onPostRemoved(DataSnapshot post);

    public void startListening() {

        for (DatabaseReference squareRef : squareRefsFetched.keySet()) {
            squareRef.addChildEventListener(childEventListener);
            squareRef.addValueEventListener(valueEventListener);
        }
    }

    public void stopListening() {
        List<DatabaseReference> squareRefs = new ArrayList<>(hotspotSquareRefs);
        squareRefs.addAll(postSquareRefs);

        for (DatabaseReference squareRef : squareRefs) {
            squareRef.removeEventListener(childEventListener);
            squareRef.removeEventListener(valueEventListener);
        }
    }

    private boolean allSquaresFetched() {
        for (boolean b : squareRefsFetched.values()) {
            if (!b) return false;
        }
        return true;
    }

    /**
     * Returns a list of "X,Y"-formatted strings representing the southwest corners
     * of all the map "squares" that overlap with the provided LatLngBounds.
     *
     * A map "square" with string "X,Y" contains all LatLngs for which lat is in [X,X+.01) and
     * lng is in [Y,Y+.01)
     */
    private static List<String> getOverlappingSquares(LatLngBounds bounds) {
        List<String> squares = new ArrayList<>();

        for (double lat = truncateToHundredths(bounds.southwest.latitude);
             lat <= truncateToHundredths(bounds.northeast.latitude);
             lat += 0.01)
        for (double lon = truncateToHundredths(bounds.southwest.longitude);
             lon <= truncateToHundredths(bounds.northeast.longitude);
             lon += 0.01)
        {
            squares.add(getSquare(lat, lon));
        }

        return squares;
    }

    public static String getSquare(double lat, double lon) {
        return String.format(Locale.US, "%.2f;%.2f", truncateToHundredths(lat),
                truncateToHundredths(lon)).replace('.', ',');
    }

    /** Returns overlapping squares of a circle rather than a rectangle **/
    private List<String> getOverlappingSquares() {
        return getOverlappingSquares(getCircleBounds());
    }

    private LatLngBounds getCircleBounds() {
        LatLng cc = circleCenter;

        float[] metersPerLat = new float[1];
        Location.distanceBetween(cc.latitude, cc.longitude, cc.latitude + 0.01, cc.longitude,
                metersPerLat);
        metersPerLat[0] *= 100;

        float[] metersPerLon = new float[1];
        Location.distanceBetween(cc.latitude, cc.longitude,
                cc.latitude, cc.longitude + 0.01, metersPerLon);
        metersPerLon[0] *= 100;

        float radiusLat = circleRadius / metersPerLat[0];
        float radiusLon = circleRadius / metersPerLon[0];

        return new LatLngBounds.Builder()
                .include(new LatLng(cc.latitude + radiusLat, cc.longitude + radiusLon))
                .include(new LatLng(cc.latitude - radiusLat, cc.longitude - radiusLon))
                .build();
    }

    private static double truncateToHundredths(double d) {
        return Math.floor(d * 100) / 100;
    }
}
