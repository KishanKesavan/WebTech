package com.example.kisha.travelandentertainmentsearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;
    private Toolbar detailsToolbar;
    private ImageButton favoritesIcon, twitter;
    private ObjectMapper mapper = new ObjectMapper();
    private PlaceDetails placeDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        String placeInfo = getIntent().getStringExtra(PlacesListAdapter.PLACES_DETAILS);
        try{
            placeDetails = mapper.readValue(placeInfo,PlaceDetails.class);
            sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
            favoritesIcon = (ImageButton) findViewById(R.id.detailsFavorite);
            favoritesIcon.setTag(R.drawable.heart_outline_white);
            favoritesIcon.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    toggleFavorites();
                }
            });
            setIfFavorite();
            twitter = (ImageButton) findViewById(R.id.twitterShare);
            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tweet= "https://twitter.com/intent/tweet?text=Check out "+placeDetails.getName()+" Located at "+placeDetails.getFormatted_address()+". Website:  "+placeDetails.getWebsite()+" . "+"TravelAndEntertainmentSearch";
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse(tweet));
                    startActivity(viewIntent);
                }
            });
            detailsToolbar = (Toolbar) findViewById(R.id.detailsToolbar);
            detailsToolbar.setTitle(placeDetails.getName());
            setSupportActionBar(detailsToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            detailsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            viewPager = (ViewPager)findViewById(R.id.container);
            setUpViewPager(viewPager);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);

            int[] tabIcons = new int[]{R.drawable.info_outline,R.drawable.photos,R.drawable.maps,R.drawable.review};
            for(int i =0 ;i <4;++i){
                LinearLayout tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
                TextView tabContent = (TextView) tabLinearLayout.findViewById(R.id.tabContent);
                tabContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                Log.d(TAG,tabContent.toString());
                tabContent.setText(sectionsPageAdapter.getPageTitle(i));
                tabContent.setCompoundDrawablesWithIntrinsicBounds(tabIcons[i], 0, 0, 0);
                tabLayout.getTabAt(i).setCustomView(tabContent);
            }
        }catch (Exception e){
            Log.d(TAG,"Error converting JSON to placeDetails",e);
        }
    }

    private void setUpViewPager(ViewPager viewPager){
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.setPlaceDetails(placeDetails);
        PhotosFragment photosFragment = new PhotosFragment();
        photosFragment.setPlaceId(placeDetails.getPlace_id());
        ReviewsFragment reviewsFragment = new ReviewsFragment();
        reviewsFragment.setPlaceDetails(placeDetails);
        Log.d(TAG, placeDetails.getGeometry() == null ? "ISnull" : "no");
        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.setEndLocation(placeDetails.getGeometry().getLocation());
        mapsFragment.setDestination(placeDetails.getName());
        sectionsPageAdapter.addFragment(infoFragment," INFO");
        sectionsPageAdapter.addFragment(photosFragment," PHOTOS");
        sectionsPageAdapter.addFragment(mapsFragment," MAPS");
        sectionsPageAdapter.addFragment(reviewsFragment," REVIEWS");
        viewPager.setAdapter(sectionsPageAdapter);
    }

    private void setIfFavorite(){
        String placeId = placeDetails.getPlace_id();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_favorites_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(placeId.equals(entry.getKey())){
                favoritesIcon.setTag(R.drawable.heart_fill_white);
                favoritesIcon.setImageResource(R.drawable.heart_fill_white);
            }
        }
    }

    private void toggleFavorites(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_favorites_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if((Integer)favoritesIcon.getTag() == R.drawable.heart_outline_white){
            favoritesIcon.setTag(R.drawable.heart_fill_white);
            favoritesIcon.setImageResource(R.drawable.heart_fill_white);
            PlaceItem placeItem = new PlaceItem();
            placeItem.setName(placeDetails.getName());
            placeItem.setPlace_id(placeDetails.getPlace_id());
            placeItem.setVicinity(placeDetails.getFormatted_address());
            placeItem.setIcon(placeDetails.getIcon());
            try {
                editor.putString(placeDetails.getPlace_id(),mapper.writeValueAsString(placeItem));
                editor.commit();
            }catch (Exception e){
                Log.d(TAG,"Error adding to favoritesList",e);
            }
            showToast(placeDetails.getName()+" was added to favorites");
        }else{
            favoritesIcon.setTag(R.drawable.heart_outline_white);
            favoritesIcon.setImageResource(R.drawable.heart_outline_white);
            showToast(placeDetails.getName()+" was removed from favorites");
            editor.remove(placeDetails.getPlace_id());
            editor.commit();
        }
    }

    private void showToast(String message){
        Toast toast=Toast.makeText(this,message,Toast.LENGTH_SHORT);
        toast.show();
    }
}
