package com.example.kisha.travelandentertainmentsearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class SearchFragment extends Fragment{

    private static final String TAG = "SearchTab";
    public static final String PLACES_LIST = "placesList";
    public static final String DOMAIN_URL = "https://travel-200920.appspot.com/";
    public static final String PLACES_URL = DOMAIN_URL + "places/";
    public static final String CUSTOM_LOCATION_URL = DOMAIN_URL + "geoCode/customLocation/";

    /*private TextInputLayout keyWordWrapper, customInputLocationWrapper;*/
    private Button searchButton, clearButton;
    private TextView keywordError, customLocationError;
    private EditText keyword, distance;
    private RadioGroup location;
    private AutoCompleteTextView customLocationInput;
    private Spinner category;
    private ObjectMapper mapper = new ObjectMapper();
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_tab, container, false);

        GoogleApiClient mGoogleApiClient;

        setUpProgressDialog();
        keyword = (EditText) view.findViewById(R.id.keyword);
        keywordError = (TextView) view.findViewById(R.id.keywordError);
        distance = (EditText) view.findViewById(R.id.distance);
        category = (Spinner) view.findViewById(R.id.category);
        customLocationError = (TextView) view.findViewById(R.id.customLocationError);
        customLocationInput = (AutoCompleteTextView) view.findViewById(R.id.customLocationInput);


        CustomAutoCompleteAdapter customAutoCompleteAdapter =  new CustomAutoCompleteAdapter(getActivity().getApplicationContext());
        customLocationInput.setAdapter(customAutoCompleteAdapter);

        searchButton = (Button) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchListener(v);
            }
        });
        clearButton = (Button) view.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearForm();
            }
        });

        location = (RadioGroup) view.findViewById(R.id.location);
        location.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.customLocation){
                    customLocationInput.setEnabled(true);
                }else{
                    customLocationInput.setEnabled(false);
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.categoryTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);

        return view;
    }

    public void searchListener(View v) {
        keywordError.setVisibility(View.INVISIBLE);
        customLocationError.setVisibility(View.INVISIBLE);
        if(!isValid()){
            return;
        }
        String lat = "34.0266";
        String lon = "-118.2831";
        if(location.getCheckedRadioButtonId() == R.id.customLocation){
            RequestQueue queue= Volley.newRequestQueue(getActivity().getApplicationContext());
            String customLocation = customLocationInput.getText().toString().trim().replaceAll(" ","+");
            String url = CUSTOM_LOCATION_URL + customLocation;
            Log.d(TAG, url);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG,response);
                            try {
                                CustomLocationCoordinates customLocationCoordinates = mapper.readValue(response,CustomLocationCoordinates.class);
                                getPlacesAndMoveToNextActivity(customLocationCoordinates.getLat(),customLocationCoordinates.getLon());
                            }catch (Exception e){

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"Error in getting customLocationUrl", error);
                }
            });
             queue.add(stringRequest);
        }else{
            getPlacesAndMoveToNextActivity(lat,lon);
        }

    }


    private void getPlacesAndMoveToNextActivity(String lat, String lon){
        String radius =  String.valueOf(((distance.getText().toString().trim().equals("")) ? 10 : Integer.parseInt(distance.getText().toString())) * 1609.34);
        String key = keyword.getText().toString().trim().replaceAll(" ","+");
        String cat = category.getSelectedItem().toString();
        cat = cat.equals("Default") ? "undefined" : cat.toLowerCase();
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = PLACES_URL + lat + "/" + lon + "/" + radius + "/" + key + "/" + cat;
        //String url = "https://travel-200920.appspot.com/places/34.0266/-118.2831/16093.4/usc/undefined";
        //String url = "https://travel-200920.appspot.com/places/34.0266/-118.2831/16093.4/pizza/undefined";
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,response);
                        Intent intent = new Intent(getActivity().getApplicationContext(), PlacesActivity.class);
                        intent.putExtra(PLACES_LIST, response);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Error in getting placesList", error);
            }
        });

        queue.add(stringRequest);
    }

    private boolean isValid() {
        boolean valid = true;
        if (keyword.getText().toString().trim().equals("")) {
            keywordError.setVisibility(View.VISIBLE);
            valid = false;
        }
        if(location.getCheckedRadioButtonId() == R.id.customLocation && customLocationInput.getText().toString().trim().equals("")){
            customLocationError.setVisibility(View.VISIBLE);
            valid = false;
        }
        if(!valid){
            showToast("Please fix all fields with errors");
        }
        return valid;
    }

    private void showToast(String message){
        Toast toast=Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setUpProgressDialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching results");
    }

    private void clearForm(){
        keywordError.setVisibility(View.INVISIBLE);
        customLocationError.setVisibility(View.INVISIBLE);
        keyword.setText("");
        distance.setText("");
        customLocationInput.setText("");
        location.check(R.id.currentLocation);
        category.setSelection(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressDialog.hide();
    }
}
