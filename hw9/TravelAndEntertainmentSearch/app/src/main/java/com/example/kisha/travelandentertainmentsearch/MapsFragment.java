package com.example.kisha.travelandentertainmentsearch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback{

    private static final String TAG = "MapsFragment";
    private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=:origin&destination=:destination&mode=:mode&key=AIzaSyAn1UZM5hQYzuMUMusHvHbfpbN2o2ZVBZo";
    private Location endLocation;
    private String destination;
    private Spinner travelMode;
    private AutoCompleteTextView fromLocation;
    private SupportMapFragment mapFragment;
    private ObjectMapper mapper = new ObjectMapper();
    private GoogleMap map;
    private Marker originMarker;
    private Polyline currentPolyline;

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_tab, container, false);
        travelMode = (Spinner) view.findViewById(R.id.travelMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.travelModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        travelMode.setAdapter(adapter);
        fromLocation = (AutoCompleteTextView) view.findViewById(R.id.mapFromLocation);
        CustomAutoCompleteAdapter customAutoCompleteAdapter = new CustomAutoCompleteAdapter(getActivity().getApplicationContext());
        fromLocation.setAdapter(customAutoCompleteAdapter);

        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fromLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                renderDirections();
            }
        });

        travelMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(fromLocation.getText().toString().trim().equals("")){
                    return;
                }
                renderDirections();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng endLocationCoordinates = new LatLng(endLocation.getLat(),endLocation.getLng());
        map.addMarker(new MarkerOptions().position(endLocationCoordinates).title(destination));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocationCoordinates,13));
    }

    private void renderDirections(){
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = DIRECTIONS_URL.replace(":origin",fromLocation.getText().toString().replace(" ","+")).replace(":destination",endLocation.getLat()+","+endLocation.getLng()).replace(":mode",travelMode.getSelectedItem().toString().toLowerCase());
        Log.d(TAG,url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            String routesString = jsonObject.getString("routes");
                            List<Route> routes = mapper.readValue(routesString, new TypeReference<List<Route>>(){});
                            for(Route route : routes){
                                OverViewPolyLine overViewPolyLine = route.getOverview_polyline();
                                if(overViewPolyLine != null){
                                    if(currentPolyline!=null){
                                        currentPolyline.remove();
                                    }
                                    if(originMarker != null){
                                        originMarker.remove();
                                    }
                                    String points = overViewPolyLine.getPoints();
                                    PolylineOptions options = new PolylineOptions().width(25).color(Color.BLUE).geodesic(true).addAll(decodePoly(points));
                                    currentPolyline = map.addPolyline(options);
                                    LatLngBounds path = new LatLngBounds(currentPolyline.getPoints().get(0),currentPolyline.getPoints().get(currentPolyline.getPoints().size()-1));
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(path.getCenter(),13));
                                    originMarker = map.addMarker(new MarkerOptions().position(currentPolyline.getPoints().get(0)));
                                }
                            }
                        }catch (Exception e){
                            Log.d(TAG,"Error converting Map direction result");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Error in getting placesList", error);
            }
        });

        queue.add(stringRequest);
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
