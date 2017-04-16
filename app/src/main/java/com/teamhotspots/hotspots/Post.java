package com.teamhotspots.hotspots;

/**
 * Created by Kathleen on 4/7/2017.
 */

public class Post {
    private String username;
    private String msg;
    private Integer drawable;    // TODO: Figure out a better way to store this
    private int numLikes;

    public Post(String username, String msg, Integer drawable) {
        this.username = username;
        this.msg = msg;
        this.drawable = drawable;
        this.numLikes = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getDrawable() {
        return drawable;
    }

    public void setDrawable(Integer drawable) {
        this.drawable = drawable;
    }

    public int getNumLikes() {
        return this.numLikes;
    }

    public void upvote() {
        this.numLikes++;
    }

    public void undoVote() { this.numLikes--; }

    public boolean isPicturePost() { return drawable != null; }
}
