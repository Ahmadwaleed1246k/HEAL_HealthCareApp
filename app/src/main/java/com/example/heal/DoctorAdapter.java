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

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private Context context;
    private List<Doctor> doctorList;
    private boolean isHorizontal;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(Context context, List<Doctor> doctorList, boolean isHorizontal, OnDoctorClickListener listener) {
        this.context = context;
        this.doctorList = doctorList;
        this.isHorizontal = isHorizontal;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isHorizontal ? R.layout.item_doctor_horizontal : R.layout.item_doctor_vertical;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.tvName.setText(doctor.getName());
        holder.tvSpecialty.setText(doctor.getSpecialization());
        holder.tvRating.setText(String.valueOf(doctor.getRating()));

        if (isHorizontal) {
            holder.tvReviews.setText("(" + doctor.getTotal_reviews() + ")");
        } else {
            holder.tvExperience.setText(doctor.getExperience_years() + " Years");
            holder.tvPatients.setText(doctor.getTotal_reviews() * 12 + "+"); // Simple estimation
        }

        String imageUrl = doctor.getProfile_picture();
        if (imageUrl != null && imageUrl.contains("drive.google.com")) {
            imageUrl = convertDriveLink(imageUrl);
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .into(holder.ivProfile);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDoctorClick(doctor);
        });
        
        if (holder.btnAction != null) {
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onDoctorClick(doctor);
            });
        }
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    private String convertDriveLink(String originalLink) {
        try {
            if (originalLink.contains("/file/d/")) {
                String fileId = originalLink.split("/file/d/")[1].split("/")[0];
                return "https://drive.google.com/uc?export=view&id=" + fileId;
            } else if (originalLink.contains("id=")) {
                String fileId = originalLink.split("id=")[1].split("&")[0];
                return "https://drive.google.com/uc?export=view&id=" + fileId;
            }
        } catch (Exception e) {
            return originalLink;
        }
        return originalLink;
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvSpecialty, tvRating, tvReviews, tvExperience, tvPatients;
        View btnAction;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivDoctorImage);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvRating = itemView.findViewById(R.id.tvDoctorRating);
            tvReviews = itemView.findViewById(R.id.tvDoctorReviews);
            tvExperience = itemView.findViewById(R.id.tvDoctorExperience);
            tvPatients = itemView.findViewById(R.id.tvDoctorPatients);
            btnAction = itemView.findViewById(R.id.btnBookNow);
            if (btnAction == null) btnAction = itemView.findViewById(R.id.btnBookConsultation);
        }
    }
}
