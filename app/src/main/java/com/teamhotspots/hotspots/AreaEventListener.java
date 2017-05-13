package com.teamhotspots.hotspots;

import android.provider.ContactsContract;

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

    public AreaEventListener(LatLngBounds area) {
        if (db == null) {
            db = FirebaseDatabase.getInstance().getReference();
        }

        hotspotSquareRefs = new ArrayList<>();
        postSquareRefs = new ArrayList<>();
        hotspots = new HashMap<>();
        posts = new HashMap<>();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!squareRefsFetched.get(dataSnapshot.getRef()))
                {
                    squareRefsFetched.put(dataSnapshot.getRef(), true);
                }

                // Call onDataChange() when data has been received about every square
                if (allSquaresFetched()) {
                    if (!AreaEventListener.this.onDataChange(hotspots.values(), posts.values())) {
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
                    dataSnapshot.getValue(Hotspot.class);
                    hotspots.put(dataSnapshot.getRef(), dataSnapshot);
                    onHotspotAdded(dataSnapshot);
                } catch (Exception e) {
                    posts.put(dataSnapshot.getRef(), dataSnapshot);
                    onPostAdded(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    dataSnapshot.getValue(Hotspot.class);
                    hotspots.put(dataSnapshot.getRef(), dataSnapshot);
                    onHotspotChanged(dataSnapshot);
                } catch (Exception e) {
                    posts.put(dataSnapshot.getRef(), dataSnapshot);
                    onPostChanged(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    dataSnapshot.getValue(Hotspot.class);
                    hotspots.remove(dataSnapshot.getRef());
                    onHotspotRemoved(dataSnapshot);
                } catch (Exception e) {
                    posts.remove(dataSnapshot.getRef());
                    onPostRemoved(dataSnapshot);
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
        List<DatabaseReference> squareRefs = new ArrayList<>(hotspotSquareRefs);
        squareRefs.addAll(postSquareRefs);

        for (DatabaseReference squareRef : squareRefs) {
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
            squares.add(lat + "," + lon);
        }

        return squares;
    }

    private static double truncateToHundredths(double d) {
        return Math.floor(d * 100) / 100;
    }
}
