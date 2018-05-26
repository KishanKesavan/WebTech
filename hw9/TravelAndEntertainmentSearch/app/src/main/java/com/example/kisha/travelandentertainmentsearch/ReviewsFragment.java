package com.example.kisha.travelandentertainmentsearch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private static final String TAG = "ReviewsTab";
    private static final String YELP_REVIEWS_URL = SearchFragment.DOMAIN_URL + "reviews/:name/:address/:city/:state/:country";
    private ObjectMapper mapper = new ObjectMapper();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView noReviews;
    private PlaceDetails placeDetails;
    private Spinner sortType, reviewType;
    private List<Review> googleReviews;
    private List<Review> defaultGoogleReviews;
    private List<YelpReview> yelpReviews;
    private List<YelpReview> defaultYelpReviews;
    private boolean showingGoogleReviews;

    public PlaceDetails getPlaceDetails() {
        return placeDetails;
    }

    public void setPlaceDetails(PlaceDetails placeDetails) {
        this.placeDetails = placeDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reviews_tab, container, false);

        googleReviews = new ArrayList<>(placeDetails.getReviews());
        defaultGoogleReviews= new ArrayList<>(placeDetails.getReviews());

        initializeYelpReviews();

        sortType = (Spinner) view.findViewById(R.id.sortType);
        reviewType = (Spinner) view.findViewById(R.id.reviewType);
        recyclerView = (RecyclerView) view.findViewById(R.id.reviewsList);
        recyclerView.setHasFixedSize(true);

        noReviews = (TextView) view.findViewById(R.id.noReviews);
        noReviews.setVisibility(View.INVISIBLE);

        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        setGoogleReviews();

        reviewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                   setGoogleReviews();
                }else{
                    setYelpReviews();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    private void addListeners(){
        sortType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    googleReviews = new ArrayList<>(defaultGoogleReviews);
                    yelpReviews = new ArrayList<>(defaultYelpReviews);
                    if(showingGoogleReviews){
                        setGoogleReviews();
                    }else{
                        setYelpReviews();
                    }
                }else if(position == 1){
                    sortGoogleReviews("Rating",false);
                    sortYelpReviews("Rating",false);
                    adapter.notifyDataSetChanged();
                }else if(position == 2){
                    sortGoogleReviews("Rating",true);
                    sortYelpReviews("Rating",true);
                    adapter.notifyDataSetChanged();
                }else if(position == 3){
                    sortGoogleReviews("Time",false);
                    sortYelpReviews("Time",false);
                    adapter.notifyDataSetChanged();
                }else if(position == 4){
                    sortGoogleReviews("Time",true);
                    sortYelpReviews("Time",true);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void sortGoogleReviews(final String sortType, final boolean ascending){
        Collections.sort(googleReviews, new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                if(sortType.equals("Rating")){
                    if(ascending){
                        return Float.compare(o1.getRating(),o2.getRating());
                    }else{
                        return Float.compare(o2.getRating(),o1.getRating());
                    }
                }
                if(sortType.equals("Time")){
                    if(ascending){
                        return Long.compare(o1.getTime(),o2.getTime());
                    }else{
                        return Long.compare(o2.getTime(),o1.getTime());
                    }
                }
                return 0;
            }
        });
    }

    private void sortYelpReviews(final String sortType, final boolean ascending){
        Collections.sort(yelpReviews, new Comparator<YelpReview>() {
            @Override
            public int compare(YelpReview o1, YelpReview o2) {
                if(sortType.equals("Rating")){
                    if(ascending){
                        return Float.compare(o1.getRating(),o2.getRating());
                    }else{
                        return Float.compare(o2.getRating(),o1.getRating());
                    }
                }
                if(sortType.equals("Time")){
                    if(ascending){
                        return o1.getTime_created().compareTo(o2.getTime_created());
                    }else{
                        return o2.getTime_created().compareTo(o1.getTime_created());
                    }
                }
                return 0;
            }
        });
    }

    private void setGoogleReviews(){
        showingGoogleReviews = true;
        if(googleReviews.size() == 0){
            noReviews.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            noReviews.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
            adapter=new ReviewsListAdapter(googleReviews);
            recyclerView.setAdapter(adapter);
    }

    private void setYelpReviews(){
        showingGoogleReviews = false;
        if(yelpReviews.size() == 0){
            noReviews.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            noReviews.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
            adapter = new YelpListAdapter(yelpReviews);
            recyclerView.setAdapter(adapter);
    }

    private void initializeYelpReviews(){
        String country="",state="",city="";
        for(int i =0; i<placeDetails.getAddress_components().size(); ++i){
            AddressComponent component = placeDetails.getAddress_components().get(i);
            if(component.getTypes().contains("country")){
                country = component.getShort_name();
            }
            if(component.getTypes().contains("administrative_area_level_1")){
                state = component.getShort_name();
            }
            if(component.getTypes().contains("administrative_area_level_2")){
                city = component.getShort_name();
            }
        }
        String url = YELP_REVIEWS_URL.replace(":name",placeDetails.getName()).replace(":address",placeDetails.getFormatted_address().split(",")[0]).replace(":city",city).replace(":state",state).replace(":country",country).replace(" ","%20");
        Log.d(TAG,url);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.replace(" ","%20"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,response);
                        try {
                            yelpReviews = mapper.readValue(response, new TypeReference<List<YelpReview>>(){});
                            defaultYelpReviews = new ArrayList<>(yelpReviews);
                            addListeners();
                        }catch (Exception e){
                            Log.d(TAG,"Error in converting Yelp Result",e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Error in yelp reviews", error);
            }
        });

        queue.add(stringRequest);
    }
}
