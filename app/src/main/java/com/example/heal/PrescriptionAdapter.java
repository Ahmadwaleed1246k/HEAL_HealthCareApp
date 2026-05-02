package com.example.heal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {

    private Context context;
    private List<Prescription> prescriptionList;
    private boolean isHistoryMode;
    private OnPrescriptionActionListener listener;

    public interface OnPrescriptionActionListener {
        void onDismiss(Prescription prescription);
        void onItemClick(Prescription prescription);
    }

    public PrescriptionAdapter(Context context, List<Prescription> prescriptionList, boolean isHistoryMode, OnPrescriptionActionListener listener) {
        this.context = context;
        this.prescriptionList = prescriptionList;
        this.isHistoryMode = isHistoryMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        Prescription prescription = prescriptionList.get(position);
        holder.tvMedicineName.setText(prescription.getMedicineName());
        holder.tvDosageFreq.setText(prescription.getDosage() + " - " + prescription.getFrequency());
        holder.tvDoctorName.setText(prescription.getDoctorName());
        holder.tvInstructions.setText(prescription.getInstructions());
        holder.tvDate.setText(prescription.getDate());
        
        if (isHistoryMode) {
            holder.btnDismiss.setVisibility(View.GONE);
        } else if (listener != null) {
            holder.btnDismiss.setVisibility(View.VISIBLE);
            holder.btnDismiss.setTextColor(android.graphics.Color.parseColor("#F57C00"));
            holder.btnDismiss.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF3E0")));
            holder.btnDismiss.setOnClickListener(v -> listener.onDismiss(prescription));
        } else {
            holder.btnDismiss.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(prescription);
        });
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    public static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvDosageFreq, tvDoctorName, tvInstructions, tvDate;
        androidx.appcompat.widget.AppCompatButton btnDismiss;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosageFreq = itemView.findViewById(R.id.tvDosageFreq);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvInstructions = itemView.findViewById(R.id.tvPrescriptionInstructions);
            tvDate = itemView.findViewById(R.id.tvPrescriptionDate);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
        }
    }
}
