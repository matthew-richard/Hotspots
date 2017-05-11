package com.teamhotspots.hotspots;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Kathleen on 5/1/2017.
 */

public class Hotspot {
    public double lat;
    public double lng;
    public HashMap<String, Boolean> posts;

    public Hotspot() {}
    public Hotspot(double lat, double lng, HashMap<String, Boolean> posts) {
        this.lat = lat;
        this.lng = lng;
        this.posts = posts;
    }

    public void addPost(String key) {
        posts.put(key, true);
    }
}
