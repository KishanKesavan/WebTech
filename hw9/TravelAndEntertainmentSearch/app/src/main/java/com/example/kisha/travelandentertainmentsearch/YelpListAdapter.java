package com.example.kisha.travelandentertainmentsearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class YelpListAdapter extends RecyclerView.Adapter<YelpListAdapter.ViewHolder> {
    private static final String TAG = "ReviewsListAdapter";
    private List<YelpReview> yelpReviewList;

    private Context context;
    private RecyclerView mRecyclerView;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView reviewImage;
        public RatingBar reviewRatings;
        public TextView reviewerName,reviewDate, reviewContent;
        public LinearLayout reviewItem;

        public LinearLayout getReviewItem() {
            return reviewItem;
        }

        public void setReviewItem(LinearLayout reviewItem) {
            this.reviewItem = reviewItem;
        }

        public ImageView getReviewImage() {
            return reviewImage;
        }

        public void setReviewImage(ImageView reviewImage) {
            this.reviewImage = reviewImage;
        }

        public RatingBar getReviewRatings() {
            return reviewRatings;
        }

        public void setReviewRatings(RatingBar reviewRatings) {
            this.reviewRatings = reviewRatings;
        }

        public TextView getReviewerName() {
            return reviewerName;
        }

        public void setReviewerName(TextView reviewerName) {
            this.reviewerName = reviewerName;
        }

        public TextView getReviewDate() {
            return reviewDate;
        }

        public void setReviewDate(TextView reviewDate) {
            this.reviewDate = reviewDate;
        }

        public TextView getReviewContent() {
            return reviewContent;
        }

        public void setReviewContent(TextView reviewContent) {
            this.reviewContent = reviewContent;
        }

        public ViewHolder(View v) {
            super(v);
            reviewItem = (LinearLayout) v.findViewById(R.id.reviewItem);
            reviewRatings = (RatingBar) v.findViewById(R.id.reviewRating);
            reviewImage = (ImageView) v.findViewById(R.id.reviewImage);
            reviewerName = (TextView) v.findViewById(R.id.reviewerName);
            reviewDate = (TextView) v.findViewById(R.id.reviewDate);
            reviewContent = (TextView) v.findViewById(R.id.reviewContent);
        }
    }

    public YelpListAdapter(List<YelpReview> reviews) {
        yelpReviewList = reviews;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }


    @NonNull
    @Override
    public YelpListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);
        context = parent.getContext();
        return new YelpListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final YelpListAdapter.ViewHolder holder, final int position) {
        holder.getReviewerName().setText(yelpReviewList.get(position).getUser().getName());
        holder.getReviewContent().setText(yelpReviewList.get(position).getText());
        holder.getReviewRatings().setRating(yelpReviewList.get(position).getRating());
        Picasso.get().load(yelpReviewList.get(position).getUser().getImage_url()).into(holder.getReviewImage());
        holder.getReviewDate().setText(yelpReviewList.get(position).getTime_created());
        holder.getReviewItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(yelpReviewList.get(position).getUrl()));
                context.startActivity(viewIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return yelpReviewList.size();
    }
}

