package com.example.heal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;

public class HospitalLocationAdapter extends RecyclerView.Adapter<HospitalLocationAdapter.LocationViewHolder> {

    private Context context;
    private List<Hospital> hospitalList;
    private OnHospitalClickListener listener;

    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }

    public HospitalLocationAdapter(Context context, List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.context = context;
        this.hospitalList = hospitalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hospital_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.tvName.setText(hospital.getName());
        holder.tvAddress.setText(hospital.getAddress());
        holder.tvRating.setText(String.format(Locale.US, "%.1f ★", hospital.getRating()));
        holder.tvDistance.setText(String.format(Locale.US, "%.1f miles", hospital.getDistanceMiles()));
        holder.tvWaitTime.setText(String.format(Locale.US, "• %d min wait", hospital.getWaitTime()));

        Glide.with(context)
                .load(hospital.getImageUrl())
                .placeholder(R.drawable.ic_hospital)
                .into(holder.ivHospitalImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHospitalClick(hospital);
        });
    }

    public void updateData(List<Hospital> newList) {
        this.hospitalList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHospitalImage;
        TextView tvName, tvAddress, tvRating, tvDistance, tvWaitTime;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHospitalImage = itemView.findViewById(R.id.ivHospitalImage);
            tvName = itemView.findViewById(R.id.tvHospitalName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvWaitTime = itemView.findViewById(R.id.tvWaitTime);
        }
    }
}
