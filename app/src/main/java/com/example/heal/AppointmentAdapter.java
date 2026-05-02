package com.example.heal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private Context context;
    private List<Appointment> appointmentList;
    private OnAppointmentActionListener listener;
    private boolean isDoctor;

    public interface OnAppointmentActionListener {
        void onAccept(Appointment appointment);
        void onReject(Appointment appointment);
        void onReschedule(Appointment appointment);
        void onCancel(Appointment appointment);
        void onPrescribe(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> appointmentList, boolean isDoctor, OnAppointmentActionListener listener) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.isDoctor = isDoctor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        
        if (isDoctor) {
            holder.tvPatientName.setText(appointment.getPatientName());
            holder.ivIcon.setImageResource(R.drawable.ic_schedule);
        } else {
            if (appointment.getType().equals("room")) {
                holder.tvPatientName.setText(appointment.getHospitalName());
                holder.ivIcon.setImageResource(R.drawable.ic_hospital);
            } else {
                holder.tvPatientName.setText(appointment.getDoctorName());
                holder.ivIcon.setImageResource(R.drawable.ic_schedule);
            }
        }
        holder.tvDate.setText(appointment.getDate());
        holder.tvTime.setText(appointment.getTime());
        holder.tvStatus.setText(appointment.getStatus().toUpperCase());

        // Status coloring
        switch (appointment.getStatus().toLowerCase()) {
            case "pending":
                holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorPrimary));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0F2F1")));
                break;
            case "accepted":
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                break;
            case "rejected":
            case "cancelled":
            case "cancelled_by_doctor":
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
                if (appointment.getStatus().equalsIgnoreCase("cancelled_by_doctor")) {
                    holder.tvStatus.setText("CANCELLED BY DOCTOR");
                }
                break;
            case "rescheduled":
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF3E0")));
                break;
        }

        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText("Notes: " + appointment.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        if (isDoctor) {
            holder.btnCancel.setVisibility(View.GONE);
            if (appointment.getStatus().equalsIgnoreCase("pending") || appointment.getStatus().equalsIgnoreCase("rescheduled")) {
                holder.llActions.setVisibility(View.VISIBLE);
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReschedule.setVisibility(View.VISIBLE);
                holder.btnPrescribe.setVisibility(View.GONE);
            } else if (appointment.getStatus().equalsIgnoreCase("accepted")) {
                holder.llActions.setVisibility(View.VISIBLE);
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                holder.btnReschedule.setVisibility(View.GONE);
                holder.btnPrescribe.setVisibility(View.VISIBLE);
            } else {
                holder.llActions.setVisibility(View.GONE);
            }
        } else {
            // Patient view
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnReschedule.setVisibility(View.GONE);
            holder.btnPrescribe.setVisibility(View.GONE);
            holder.llActions.setVisibility(View.VISIBLE);
            String status = appointment.getStatus().toLowerCase();
            if (status.equals("accepted") || status.equals("pending") || status.equals("rescheduled") || status.equals("confirmed")) {
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setText("Cancel");
                holder.btnCancel.setTextColor(android.graphics.Color.RED);
                holder.btnCancel.setOnClickListener(v -> listener.onCancel(appointment));
            } else if (status.equals("cancelled_by_doctor")) {
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setText("Dismiss");
                holder.btnCancel.setTextColor(android.graphics.Color.RED);
                holder.btnCancel.setOnClickListener(v -> listener.onCancel(appointment));
            } else {
                holder.btnCancel.setVisibility(View.GONE);
            }
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(appointment));
        holder.btnReject.setOnClickListener(v -> listener.onReject(appointment));
        holder.btnReschedule.setOnClickListener(v -> listener.onReschedule(appointment));
        holder.btnPrescribe.setOnClickListener(v -> listener.onPrescribe(appointment));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDate, tvTime, tvStatus, tvNotes;
        Button btnAccept, btnReject, btnReschedule, btnCancel, btnPrescribe;
        LinearLayout llActions;
        ImageView ivIcon;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnReschedule = itemView.findViewById(R.id.btnReschedule);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnPrescribe = itemView.findViewById(R.id.btnPrescribe);
            llActions = itemView.findViewById(R.id.llActions);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
