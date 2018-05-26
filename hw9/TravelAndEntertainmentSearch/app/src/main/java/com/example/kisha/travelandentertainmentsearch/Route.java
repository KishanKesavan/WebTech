package com.example.kisha.travelandentertainmentsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Route {
    private OverViewPolyLine overview_polyline;

    public OverViewPolyLine getOverview_polyline() {
        return overview_polyline;
    }

    public void setOverview_polyline(OverViewPolyLine overview_polyline) {
        this.overview_polyline = overview_polyline;
    }
}
