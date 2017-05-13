package com.teamhotspots.hotspots;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

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

    @Exclude
    public DatabaseReference ref;

    public Post() {}
    public Post(String username, String msg, String imageUrl, String userIcon,
                String timeStamp, double lat, double lng) {
        this.username = username;
        this.msg = msg;
        this.imageUrl = imageUrl;
        this.userIcon = userIcon;
        this.timeStamp = timeStamp;
        this.lat = lat;
        this.lng = lng;
        this.numLikes = 0;
    }

    public String getUsername() {
        return username;
    }

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

    @Override
    @Exclude
    public boolean equals(Object obj) {
        return obj.getClass() == Post.class && ((Post) obj).ref.equals(this.ref);
    }
}
