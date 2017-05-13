package com.teamhotspots.hotspots;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kathleen on 4/7/2017.
 */

public class Post {
    private String username;
    private String msg;
    private String imageUrl;
    private String userIcon;
    private int numLikes;
    private String timeStamp;
    private double lat;
    private double lng;
    private String userID;
    private int hotspotCreated;

    public Post() {}
    public Post(String username, String msg, String imageUrl, String userIcon,
                String timeStamp, double lat, double lng, int hotspotCreated) {
        this.username = username;
        this.msg = msg;
        this.imageUrl = imageUrl;
        this.userIcon = userIcon;
        this.timeStamp = timeStamp;
        this.lat = lat;
        this.lng = lng;
        this.numLikes = 0;
        this.hotspotCreated = hotspotCreated;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) this.userID = user.getUid();
        else this.userID = "lnOu8CBcUKQKl3q9HoLr3nGsG532";  // TODO: remove this once login page is working. For now, use John's
    }

    public String getUsername() {
        return username;
    }

    public int getHotspotCreated() {return this.hotspotCreated;}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsericon() {return userIcon;}

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getNumLikes() {
        return this.numLikes;
    }

    public void upvote() {
        this.numLikes++;
    }

    public void undoVote() { this.numLikes--; }

    public boolean isPicturePost() { return imageUrl != null; }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("msg", msg);
        result.put("imageUrl", imageUrl);
        result.put("userIcon", userIcon);
        result.put("numLikes", numLikes);
        result.put("timeStamp", timeStamp);
        result.put("lat", lat);
        result.put("lng", lng);

        return result;
    }
}
