package com.example.kisha.travelandentertainmentsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class PhotosFragment extends Fragment {

    private static final String TAG = "PhotosFragment";
    private String placeId;
    private GeoDataClient mGeoDataClient;
    private LinearLayout imageContainer;
    private LayoutInflater layoutInflater;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_tab, container, false);
        mGeoDataClient = Places.getGeoDataClient(getActivity().getApplicationContext(),null);
        imageContainer = (LinearLayout) view.findViewById(R.id.imagesContainer);
                layoutInflater = (LayoutInflater)getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getPhotos();
        return view;
    }

    private void getPhotos() {
        Log.d(TAG,"Photos");
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                List<PlacePhotoMetadata> placePhotoMetadataList = new ArrayList<>();
                int i = 0;
                while(true){
                    try{
                        PlacePhotoMetadata temp = photoMetadataBuffer.get(i++);
                        placePhotoMetadataList.add(temp);
                    }catch (Exception e){
                        break;
                    }
                }
                for(i=0;i<placePhotoMetadataList.size();++i){
                    PlacePhotoMetadata photoMetadata = placePhotoMetadataList.get(i);
                    // Get the attribution text.
                    CharSequence attribution = photoMetadata.getAttributions();
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    //photoMetadata = photoMetadataBuffer.get(0);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            View temp = layoutInflater.inflate(R.layout.custom_photo,null);
                            ImageView tempPhoto = (ImageView) temp.findViewById(R.id.photoItem);
                            tempPhoto.setImageBitmap(bitmap);
                            imageContainer.addView(temp);
                        }
                    });
                }
            }
        });
    }
}
