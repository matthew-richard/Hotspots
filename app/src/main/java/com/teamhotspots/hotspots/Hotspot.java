package com.teamhotspots.hotspots;

import java.util.List;

/**
 * Created by Kathleen on 5/1/2017.
 */

public class Hotspot {
    double lat;
    double lng;
    List<String> posts;

    public Hotspot(double lat, double lng, List<String> posts) {
        this.lat = lat;
        this.lng = lng;
        this.posts = posts;
    }
}
