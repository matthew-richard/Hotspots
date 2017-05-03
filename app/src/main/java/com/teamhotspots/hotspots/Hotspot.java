package com.teamhotspots.hotspots;

import java.util.List;

/**
 * Created by Kathleen on 5/1/2017.
 */

public class Hotspot {
    public double lat;
    public double lng;
    public List<String> posts;

    public Hotspot() {}
    public Hotspot(double lat, double lng, List<String> posts) {
        this.lat = lat;
        this.lng = lng;
        this.posts = posts;
    }
}
