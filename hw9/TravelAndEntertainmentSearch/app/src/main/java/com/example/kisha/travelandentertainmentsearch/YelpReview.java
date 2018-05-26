package com.example.kisha.travelandentertainmentsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YelpReview {
    private String url;
    private float rating;
    private String time_created;
    private String text;
    private YelpUser user;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getTime_created() {
        return time_created;
    }

    public void setTime_created(String time_created) {
        this.time_created = time_created;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public YelpUser getUser() {
        return user;
    }

    public void setUser(YelpUser user) {
        this.user = user;
    }
}
