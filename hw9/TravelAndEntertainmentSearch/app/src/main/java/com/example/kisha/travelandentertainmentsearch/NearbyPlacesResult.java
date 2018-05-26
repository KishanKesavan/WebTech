package com.example.kisha.travelandentertainmentsearch;

import java.util.List;

public class NearbyPlacesResult {
    private List<PlaceItem> places;
    private String next_page_token;

    public List<PlaceItem> getPlaces() {
        return places;
    }

    public void setPlaces(List<PlaceItem> placeItems) {
        this.places = placeItems;
    }

    public String getNext_page_token() {
        return next_page_token;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }
}
