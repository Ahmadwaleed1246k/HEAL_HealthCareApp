package com.example.heal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserLabBookingsAdapter extends RecyclerView.Adapter<UserLabBookingsAdapter.ViewHolder> {

    private final Context context;
    private final List<TestBooking> bookings;

    public UserLabBookingsAdapter(Context context, List<TestBooking> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lab_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestBooking booking = bookings.get(position);

        holder.tvTestName.setText(booking.getTest_name() != null ? booking.getTest_name() : "—");

        // Status badge with color
        String status = booking.getStatus();
        if (status != null) {
            holder.tvStatus.setText(capitalize(status));
            if ("confirmed".equalsIgnoreCase(status)) {
                holder.tvStatus.setTextColor(0xFF2E7D32);
                holder.tvStatus.setBackgroundResource(R.drawable.search_bar_bg);
                holder.tvStatus.getBackground().setTint(0xFFE8F5E9);
            } else if ("pending".equalsIgnoreCase(status)) {
                holder.tvStatus.setTextColor(0xFFF57F17);
                holder.tvStatus.getBackground().setTint(0xFFFFF8E1);
            } else {
                holder.tvStatus.setTextColor(0xFF757575);
                holder.tvStatus.getBackground().setTint(0xFFF5F5F5);
            }
        }

        holder.tvPreferredDate.setText(
                booking.getPreferred_date() != null ? booking.getPreferred_date() : "—");

        holder.tvTimeSlot.setText(
                booking.getTime_slot() != null ? booking.getTime_slot() : "—");

        // Appointment type: make it readable
        String type = booking.getAppointment_type();
        if ("home_collection".equalsIgnoreCase(type)) {
            holder.tvType.setText("🏠 Home Collection");
        } else if ("clinic_visit".equalsIgnoreCase(type)) {
            holder.tvType.setText("🏥 Clinic Visit");
        } else {
            holder.tvType.setText(type != null ? capitalize(type) : "—");
        }

        // Amount
        double amount = booking.getTotal_amount();
        holder.tvAmount.setText(amount > 0 ? "$" + (int) amount : "—");

        // Preparation
        String prep = booking.getPreparation_instructions();
        holder.tvPreparation.setText(prep != null && !prep.isEmpty() ? prep : "No special preparation required.");

        // View Report Button logic
        if (booking.isAi_result_ready()) {
            holder.btnViewReport.setVisibility(View.VISIBLE);
            holder.btnViewReport.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, ResultDetailActivity.class);
                intent.putExtra(ResultDetailActivity.EXTRA_BOOKING_ID, booking.getBooking_id());
                intent.putExtra(ResultDetailActivity.EXTRA_TEST_NAME, booking.getTest_name());
                intent.putExtra(ResultDetailActivity.EXTRA_BOOKING_DATE, booking.getBooking_date());
                intent.putExtra(ResultDetailActivity.EXTRA_AI_RESULT, booking.getAi_result());
                context.startActivity(intent);
            });
        } else {
            holder.btnViewReport.setVisibility(View.GONE);
        }

        // Dismiss Button logic
        holder.btnDismiss.setOnClickListener(v -> {
            String bookingId = booking.getBooking_id();
            if (bookingId != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("test_bookings")
                        .child(bookingId)
                        .removeValue()
                        .addOnSuccessListener(unused -> {
                            int currentPos = holder.getAdapterPosition();
                            if (currentPos != RecyclerView.NO_POSITION) {
                                bookings.remove(currentPos);
                                notifyItemRemoved(currentPos);
                                android.widget.Toast.makeText(context, "Record dismissed successfully", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTestName, tvStatus, tvPreferredDate, tvTimeSlot, tvType, tvAmount, tvPreparation;
        TextView btnDismiss, btnViewReport;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTestName = itemView.findViewById(R.id.tvBookingTestName);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvPreferredDate = itemView.findViewById(R.id.tvBookingPreferredDate);
            tvTimeSlot = itemView.findViewById(R.id.tvBookingTimeSlot);
            tvType = itemView.findViewById(R.id.tvBookingType);
            tvAmount = itemView.findViewById(R.id.tvBookingAmount);
            tvPreparation = itemView.findViewById(R.id.tvBookingPreparation);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
            btnViewReport = itemView.findViewById(R.id.btnViewReport);
        }
    }
}
