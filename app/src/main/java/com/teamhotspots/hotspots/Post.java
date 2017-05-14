package com.teamhotspots.hotspots;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kathleen on 4/7/2017.
 */

public class Post {
    private String username;
    private String msg;
    private String imageUrl;
    private String usericon;
    private String timeStamp;
    private double lat;
    private double lng;
    private String userID;
    public boolean hotspotCreated;
    private Map<String, Boolean> likedBy;

    @Exclude
    public DatabaseReference ref;

    public Post () {}

    public Post(String username, String msg, String imageUrl, String userIcon,
                String timeStamp, double lat, double lng, boolean hotspotCreated) {
        this.username = username;
        this.msg = msg;
        this.imageUrl = imageUrl;
        this.usericon = userIcon;
        this.timeStamp = timeStamp;
        this.lat = lat;
        this.lng = lng;
        this.hotspotCreated = hotspotCreated;

        this.likedBy = new HashMap<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.userID = user.getUid();
    }

    public String getUsername() {
        return username;
    }

    public boolean getHotspotCreated() {return this.hotspotCreated;}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsericon() {return this.usericon;}

    public String getMsg() {
        return msg;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getLat() {return this.lat; }

    public double getLng() {return this.lng; }

    public String getUserID() {
        return userID;
    }

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }

    @Exclude
    public int getNumLikes() {
        return likedBy == null ? 0 : likedBy.size();
    }

    @Exclude
    public void upvote() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(likedBy != null) this.likedBy.put(uid, true);
        ref.child("likedBy/" + uid).setValue(true);
        ref.getRoot().child("users/" + uid + "/likesGiven/" + ref.getKey()).setValue(true);
        ref.getRoot().child("users/" + this.userID + "/likesReceived/" + ref.getKey()).setValue(true);
    }

    @Exclude
    public void undoVote() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (likedBy != null) this.likedBy.remove(uid);
        ref.child("likedBy/" + uid).removeValue();
        ref.getRoot().child("users/" + uid + "/likesGiven/" + ref.getKey()).removeValue();
        ref.getRoot().child("users/" + this.userID + "/likesReceived/" + ref.getKey()).removeValue();
    }

    @Exclude
    public boolean isLikedBy(String userID) {
        return likedBy != null && likedBy.containsKey(userID);
    }

    @Exclude
    public boolean isLikedByMe() {
        return isLikedBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Exclude
    public boolean isPicturePost() { return imageUrl != null; }


    @Override
    @Exclude
    public boolean equals(Object obj) {
        return obj.getClass() == Post.class && ((Post) obj).ref.equals(this.ref);
    }
}
