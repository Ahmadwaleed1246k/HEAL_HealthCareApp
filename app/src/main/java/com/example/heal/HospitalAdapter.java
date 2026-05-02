package com.example.heal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private Context context;
    private List<Hospital> hospitalList;
    private OnHospitalClickListener listener;

    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
        void onBookRoomClick(Hospital hospital);
    }

    public HospitalAdapter(Context context, List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.context = context;
        this.hospitalList = hospitalList;
        this.listener = listener;
    }

    public HospitalAdapter(List<Hospital> hospitalList) {
        this.hospitalList = hospitalList;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.tvName.setText(hospital.getName());
        holder.tvAddress.setText(hospital.getAddress());
        holder.tvRating.setText(String.valueOf(hospital.getRating()));
        holder.tvDescription.setText(hospital.getDescription());

        Glide.with(context)
                .load(hospital.getImageUrl())
                .placeholder(R.drawable.ic_hospital)
                .into(holder.ivHospitalImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHospitalClick(hospital);
        });
        holder.btnBookRoom.setOnClickListener(v -> {
            if (listener != null) listener.onBookRoomClick(hospital);
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

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHospitalImage;
        TextView tvName, tvAddress, tvRating, tvDescription;
        Button btnBookRoom;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHospitalImage = itemView.findViewById(R.id.ivHospitalImage);
            tvName = itemView.findViewById(R.id.tvHospitalName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnBookRoom = itemView.findViewById(R.id.btnBookRoom);
        }
    }
}
