package com.example.kisha.travelandentertainmentsearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReviewsListAdapter extends RecyclerView.Adapter<ReviewsListAdapter.ViewHolder> {
    private static final String TAG = "ReviewsListAdapter";
    private List<Review> reviewList;

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

    public ReviewsListAdapter(List<Review> reviews) {
        reviewList = reviews;
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
                .inflate(R.layout.review_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.getReviewerName().setText(reviewList.get(position).getAuthor_name());
        holder.getReviewContent().setText(reviewList.get(position).getText());
        holder.getReviewRatings().setRating(reviewList.get(position).getRating());
        Picasso.get().load(reviewList.get(position).getProfile_photo_url()).into(holder.getReviewImage());
        holder.getReviewDate().setText(getDate(reviewList.get(position).getTime()));
        holder.getReviewItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(reviewList.get(position).getAuthor_url()));
                context.startActivity(viewIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    private String getDate(Long epochTime){
        Date time = new Date(epochTime*1000);
        Calendar calender = Calendar.getInstance();
        calender.setTime(time);
        return (calender.get(Calendar.YEAR))+"-"+(calender.get(Calendar.MONTH)+1)+"-"+calender.get(Calendar.DATE)+" "+calender.get(Calendar.HOUR)+":"+calender.get(Calendar.MINUTE)+":"+calender.get(Calendar.SECOND);
    }
}
