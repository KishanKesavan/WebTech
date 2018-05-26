package com.example.kisha.travelandentertainmentsearch;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class PlacesActivity extends AppCompatActivity {

    private static final String TAG = "PlacesActivity";
    private static final String PAGE_TOKEN_URL = SearchFragment.PLACES_URL + "pageToken/";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button previous, next;
    private int pageNumber;
    private TextView noSearchResults;
    private List<PlaceItem> placeItemList;
    private List<PlaceItem> currentPlaceItems;
    private Toolbar searchResultsToolbar;
    private LinearLayout buttonParent;
    private List<String> nextPageToken = new ArrayList<>();
    private ProgressDialog progressDialog, adapterDialog;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpProgressDialog();
        setContentView(R.layout.activity_places);
        pageNumber = 0;
        previous = (Button) findViewById(R.id.previousButton);
        next = (Button) findViewById(R.id.nextButton);
        searchResultsToolbar = (Toolbar) findViewById(R.id.searchResultsBar);
        setSupportActionBar(searchResultsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        searchResultsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noSearchResults = (TextView) findViewById(R.id.noSearchResults);
        buttonParent = (LinearLayout) findViewById(R.id.buttonParent);

        String placesInfo = getIntent().getStringExtra(SearchFragment.PLACES_LIST);
        try {
            Log.d(TAG,placesInfo);
            NearbyPlacesResult nearbyPlacesResult = mapper.readValue(placesInfo, NearbyPlacesResult.class);
            placeItemList = new ArrayList<>(nearbyPlacesResult.getPlaces());
            currentPlaceItems = new ArrayList<>(nearbyPlacesResult.getPlaces());
            nextPageToken.add(nearbyPlacesResult.getNext_page_token());
            Log.d(TAG, " NExt : " + nextPageToken);
            recyclerView = (RecyclerView) findViewById(R.id.placesList);

            recyclerView.setHasFixedSize(true);

            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToNextPage();
                }
            });

            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPreviousPage();
                }
            });

            setDetails();
            modifyPaginationButtons();
        }catch (Exception e){
            Log.d(TAG,"Error converting JSON into placesList",e);
        }
    }

    private void setDetails(){
        if(currentPlaceItems.size() == 0){
            noSearchResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            buttonParent.setVisibility(View.GONE);
        }else {
            noSearchResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            buttonParent.setVisibility(View.VISIBLE);
        }
        adapter = new PlacesListAdapter(new ArrayList<>(currentPlaceItems),null,adapterDialog);
        recyclerView.setAdapter(adapter);
    }

    private void modifyPaginationButtons(){
        progressDialog.hide();
        next.setEnabled(false);
        previous.setEnabled(false);
        if(pageNumber != 0){
            previous.setEnabled(true);
        }
        if(nextPageToken.get(pageNumber) != null){
            next.setEnabled(true);
        }
    }

    private void goToNextPage(){
        if(placeItemList.size() > 20*(pageNumber+1)){
            currentPlaceItems = new ArrayList<>(placeItemList.subList(20*(pageNumber+1),20*(pageNumber+2)));
            pageNumber++;
            setDetails();
            modifyPaginationButtons();
        }else{
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = PAGE_TOKEN_URL + nextPageToken.get(pageNumber) ;
            Log.d(TAG, url);
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                           try{
                               Log.d(TAG,response);
                               NearbyPlacesResult nearbyPlacesResult = mapper.readValue(response,NearbyPlacesResult.class);
                               nextPageToken.add(nearbyPlacesResult.getNext_page_token());
                               placeItemList.addAll(nearbyPlacesResult.getPlaces());
                               currentPlaceItems = new ArrayList<>(nearbyPlacesResult.getPlaces());
                               pageNumber++;
                               setDetails();
                               modifyPaginationButtons();
                           }catch (Exception e){}
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"Error in getting next page", error);
                }
            });
            queue.add(stringRequest);
        }
    }

    private void goToPreviousPage(){
        currentPlaceItems = new ArrayList<>(placeItemList.subList((pageNumber-1)*20,pageNumber*20));
        pageNumber--;
        setDetails();
        modifyPaginationButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterDialog.hide();
        setDetails();
    }

    private void setUpProgressDialog(){
        progressDialog = new ProgressDialog(PlacesActivity.this);
        adapterDialog = new ProgressDialog(PlacesActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        adapterDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        adapterDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching next page");
        adapterDialog.setMessage("Fetching details");
    }


}
