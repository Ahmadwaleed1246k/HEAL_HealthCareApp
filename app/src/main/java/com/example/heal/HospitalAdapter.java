package com.example.heal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Locale;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private List<Hospital> hospitalList;
    
    // We'll use a few placeholders from Unsplash to make the mock UI look like the screenshot
    private String[] mockImages = {
        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800&q=80",
        "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=800&q=80",
        "https://images.unsplash.com/photo-1538108149393-cebb47ac80da?w=800&q=80",
        "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=800&q=80"
    };

    public HospitalAdapter(List<Hospital> hospitalList) {
        this.hospitalList = hospitalList;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        
        holder.tvName.setText(hospital.getName());
        holder.tvAddress.setText(hospital.getAddress() != null && !hospital.getAddress().isEmpty() ? hospital.getAddress() : "Location Details Unavailable");
        holder.tvDistance.setText(String.format(Locale.US, "%.1f\nmiles", hospital.getDistanceMiles()));
        holder.tvWaitTime.setText("~" + hospital.getWaitTimeMin() + " min");
        holder.tvRating.setText("★ " + String.format(Locale.US, "%.1f", hospital.getRating()));

        // Handle specialty tags
        String[] specialties = hospital.getSpecialties();
        holder.tvTag1.setVisibility(View.GONE);
        holder.tvTag2.setVisibility(View.GONE);
        holder.tvTag3.setVisibility(View.GONE);
        
        if (specialties != null && specialties.length > 0) {
            holder.tvTag1.setText(specialties[0]);
            holder.tvTag1.setVisibility(View.VISIBLE);
            if (specialties.length > 1) {
                holder.tvTag2.setText(specialties[1]);
                holder.tvTag2.setVisibility(View.VISIBLE);
            }
            if (specialties.length > 2) {
                holder.tvTag3.setText(specialties[2]);
                holder.tvTag3.setVisibility(View.VISIBLE);
            }
        }
        
        // Load random mock image using Glide
        String imageUrl = mockImages[position % mockImages.length];
        Glide.with(holder.itemView.getContext())
            .load(imageUrl)
            .apply(new RequestOptions().transform(new CenterCrop()))
            .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return hospitalList != null ? hospitalList.size() : 0;
    }
    
    public void updateData(List<Hospital> newHospitals) {
        this.hospitalList = newHospitals;
        notifyDataSetChanged();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDistance, tvAddress;
        TextView tvTag1, tvTag2, tvTag3;
        TextView tvWaitTime, tvRating;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivHospitalImage);
            tvName = itemView.findViewById(R.id.tvHospitalName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTag1 = itemView.findViewById(R.id.tvTag1);
            tvTag2 = itemView.findViewById(R.id.tvTag2);
            tvTag3 = itemView.findViewById(R.id.tvTag3);
            tvWaitTime = itemView.findViewById(R.id.tvWaitTime);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
