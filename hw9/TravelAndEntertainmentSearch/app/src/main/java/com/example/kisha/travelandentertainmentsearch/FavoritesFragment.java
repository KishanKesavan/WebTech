package com.example.kisha.travelandentertainmentsearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesTab";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<PlaceItem> favoritesList;
    private ProgressDialog progressDialog;
    private TextView noFavorites;
    private ObjectMapper mapper = new ObjectMapper();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorites_tab, container, false);
        setUpProgressDialog();
        noFavorites = (TextView) view.findViewById(R.id.noFavorites);
        recyclerView = (RecyclerView) view.findViewById(R.id.favoritesList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        setFavorites();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"OnResume");
        progressDialog.hide();
        setFavorites();
    }

    private void setFavorites(){
        favoritesList = new ArrayList<>();
        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                getString(R.string.preference_favorites_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                favoritesList.add(mapper.readValue(entry.getValue().toString(),PlaceItem.class));
            }catch (Exception e){
            }
        }
        if(favoritesList.size() == 0){
            noFavorites.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            noFavorites.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter = new PlacesListAdapter(new ArrayList<>(favoritesList),this,progressDialog);
        recyclerView.setAdapter(adapter);
    }

    private void setUpProgressDialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching details");
    }
}
