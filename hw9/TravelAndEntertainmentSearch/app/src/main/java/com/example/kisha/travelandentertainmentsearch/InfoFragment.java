package com.example.kisha.travelandentertainmentsearch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

public class InfoFragment extends Fragment {

    TextView address,phone,priceLevel,website,googlePage;
    RatingBar ratingInput;
    PlaceDetails placeDetails;

    public PlaceDetails getPlaceDetails() {
        return placeDetails;
    }

    public void setPlaceDetails(PlaceDetails placeDetails) {
        this.placeDetails = placeDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_tab, container, false);
        address = (TextView) view.findViewById(R.id.addressInput);
        phone = (TextView) view.findViewById(R.id.phoneInput);
        priceLevel = (TextView) view.findViewById(R.id.priceInput);
        website = (TextView) view.findViewById(R.id.websiteInput);
        googlePage = (TextView) view.findViewById(R.id.googlePageInput);
        ratingInput = (RatingBar) view.findViewById(R.id.ratingInput);

        address.setText(placeDetails.getFormatted_address());
        phone.setText(placeDetails.getFormatted_phone_number());
        priceLevel.setText(new String(new char[placeDetails.getPrice_level()]).replace("\0", "$"));
        website.setText(placeDetails.getWebsite());
        googlePage.setText(placeDetails.getUrl());

        ratingInput.setRating((Float)placeDetails.getRating());
        return view;
    }
}
