package com.example.kisha.travelandentertainmentsearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.ViewHolder> {

    private static final String TAG = "PlacesListAdapter";
    public static final String PLACES_DETAILS = "placesDetails";

    private List<PlaceItem> placeItemList;
    private Context context, parentContext;
    private RecyclerView mRecyclerView;
    private FavoritesFragment favoritesFragment;
    private ObjectMapper mapper = new ObjectMapper();
    private ProgressDialog progressDialog;
    private static final String PLACE_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=:placeid&key=AIzaSyB9syddmfdLtCW6th5dedBvvi-Ka_zmYmw";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView placeAddress;
        public TextView placeName;
        public ImageView placeIcon;
        public ImageButton favorites;
        public LinearLayout clickablePlaceItem;

        public LinearLayout getClickablePlaceItem() {
            return clickablePlaceItem;
        }

        public void setClickablePlaceItem(LinearLayout clickablePlaceItem) {
            this.clickablePlaceItem = clickablePlaceItem;
        }

        public TextView getPlaceAddress() {
            return placeAddress;
        }

        public void setPlaceAddress(TextView placeAddress) {
            this.placeAddress = placeAddress;
        }

        public TextView getPlaceName() {
            return placeName;
        }

        public void setPlaceName(TextView placeName) {
            this.placeName = placeName;
        }

        public ImageView getPlaceIcon() {
            return placeIcon;
        }

        public void setPlaceIcon(ImageView placeIcon) {
            this.placeIcon = placeIcon;
        }

        public ImageButton getFavorites() {
            return favorites;
        }

        public void setFavorites(ImageButton favorites) {
            this.favorites = favorites;
        }

        public ViewHolder(View v) {
            super(v);
            placeAddress = (TextView) v.findViewById(R.id.placeAddress);
            placeName = (TextView) v.findViewById(R.id.placeName);
            placeIcon = (ImageView) v.findViewById(R.id.placeIcon);
            favorites = (ImageButton) v.findViewById(R.id.favoritesIcon);
            clickablePlaceItem = (LinearLayout) v.findViewById(R.id.clickablePlaceItem);
        }
    }

    public PlacesListAdapter(List<PlaceItem> placeItems, FavoritesFragment favFragment, ProgressDialog progress) {
        progressDialog =progress;
        placeItemList = placeItems;
        favoritesFragment = favFragment;

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.getPlaceName().setText(placeItemList.get(position).getName());
        holder.getPlaceAddress().setText(placeItemList.get(position).getVicinity());
        Picasso.get().load(placeItemList.get(position).getIcon()).into(holder.getPlaceIcon());
        holder.getClickablePlaceItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaceDetailsAndMoveToNextActivity(holder.getAdapterPosition());
            }
        });
        holder.getFavorites().setTag(R.drawable.heart_outline_black);
        setIfFavoriteItem(position,holder.getFavorites());
        holder.getFavorites().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Favorites icon clicked");
                toggleFavorites(position ,holder.getFavorites());
            }
        });
    }

    private void setIfFavoriteItem(int position, ImageButton tempFavIcon){
        String placeId = placeItemList.get(position).getPlace_id();
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getResources().getString(R.string.preference_favorites_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(placeId.equals(entry.getKey())){
                tempFavIcon.setTag(R.drawable.heart_fill_red);
                tempFavIcon.setImageResource(R.drawable.heart_fill_red);
            }
        }
    }

    @Override
    public int getItemCount() {
        return placeItemList.size();
    }

    private void toggleFavorites(int position, ImageButton tempFavIcon){
        Log.d(TAG,position+"");
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getResources().getString(R.string.preference_favorites_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if((Integer)tempFavIcon.getTag() == R.drawable.heart_outline_black){
            tempFavIcon.setTag(R.drawable.heart_fill_red);
            tempFavIcon.setImageResource(R.drawable.heart_fill_red);
            try {
                editor.putString(placeItemList.get(position).getPlace_id(),mapper.writeValueAsString(placeItemList.get(position)));
                editor.commit();
                Log.d(TAG,"Added to favorites");
            }catch (Exception e){
                Log.d(TAG,"Error adding to favoritesList",e);
            }
            showToast(placeItemList.get(position).getName()+" was added to favorites");
        }else{
            tempFavIcon.setTag(R.drawable.heart_outline_black);
            tempFavIcon.setImageResource(R.drawable.heart_outline_black);
            showToast(placeItemList.get(position).getName()+" was removed from favorites");
            editor.remove(placeItemList.get(position).getPlace_id());
            editor.commit();
        }
        if(favoritesFragment != null){
            favoritesFragment.onResume();
        }
    }

    private void getPlaceDetailsAndMoveToNextActivity(int position){
        RequestQueue queue= Volley.newRequestQueue(context);
        String url = PLACE_DETAILS_URL.replace(":placeid", placeItemList.get(position).getPlace_id());
        Log.d(TAG,url);
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String placeResponse = jsonObject.getString("result");
                            Intent intent = new Intent(context, DetailsActivity.class);
                            Log.d(TAG,placeResponse);
                            intent.putExtra(PLACES_DETAILS, placeResponse);
                            context.startActivity(intent);
                        }catch (Exception e){
                            Log.d(TAG,"Error converting PlaceDetails response");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Error in getting placeDetails", error);
            }
        });
        queue.add(stringRequest);
    }

    private void showToast(String message){
        Toast toast=Toast.makeText(context,message,Toast.LENGTH_SHORT);
        toast.show();
    }
}
