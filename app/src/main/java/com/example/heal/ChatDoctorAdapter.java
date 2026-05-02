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

public class ChatDoctorAdapter extends RecyclerView.Adapter<ChatDoctorAdapter.ViewHolder> {

    private List<Doctor> doctors;
    private OnChatClickListener listener;
    private Context context;

    public interface OnChatClickListener {
        void onChatClick(Doctor doctor);
    }

    public ChatDoctorAdapter(Context context, List<Doctor> doctors, OnChatClickListener listener) {
        this.context = context;
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.tvDoctorName.setText(doctor.getName());
        holder.tvSpecialization.setText(doctor.getSpecialization());

        if (doctor.getProfile_picture() != null && !doctor.getProfile_picture().isEmpty()) {
            Glide.with(context).load(doctor.getProfile_picture()).placeholder(R.drawable.ic_person).into(holder.ivDoctorProfile);
        }

        holder.btnChat.setOnClickListener(v -> listener.onChatClick(doctor));
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvSpecialization;
        ImageView ivDoctorProfile;
        View btnChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            ivDoctorProfile = itemView.findViewById(R.id.ivDoctorProfile);
            btnChat = itemView.findViewById(R.id.btnChatWithDoctor);
        }
    }
}
