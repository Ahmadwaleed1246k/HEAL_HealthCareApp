package com.example.heal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyResultsAdapter extends RecyclerView.Adapter<MyResultsAdapter.ViewHolder> {

    public interface OnViewResultClick {
        void onClick(TestBooking booking);
    }

    private final Context context;
    private final List<TestBooking> bookings;
    private final OnViewResultClick listener;

    public MyResultsAdapter(Context context, List<TestBooking> bookings, OnViewResultClick listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_my_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        TestBooking b = bookings.get(position);

        h.tvTestName.setText(b.getTest_name() != null ? b.getTest_name() : "—");
        h.tvBookingDate.setText("Booked: " + (b.getBooking_date() != null ? b.getBooking_date() : "—"));
        h.tvAmount.setText("$" + (b.getTotal_amount() > 0 ? (int) b.getTotal_amount() : "0"));

        boolean paid = "paid".equalsIgnoreCase(b.getPayment_status());
        boolean aiReady = b.isAi_result_ready();

        // Payment badge
        h.tvPaymentStatus.setText(paid ? "✓ Paid" : "Pending Payment");
        h.tvPaymentStatus.setTextColor(paid ? Color.parseColor("#2E7D32") : Color.parseColor("#E65100"));

        // CTA button appearance
        if (paid && aiReady) {
            h.btnViewResult.setText("✦  View AI Result");
            h.btnViewResult.setBackgroundColor(Color.parseColor("#0E6858"));
            h.btnViewResult.setTextColor(Color.WHITE);
        } else if (!aiReady) {
            h.btnViewResult.setText("⏳  AI Analysis Pending...");
            h.btnViewResult.setBackgroundColor(Color.parseColor("#BDBDBD"));
            h.btnViewResult.setTextColor(Color.WHITE);
        } else {
            h.btnViewResult.setText("🔒  Pay & View AI Result");
            h.btnViewResult.setBackgroundColor(Color.parseColor("#0E6858"));
            h.btnViewResult.setTextColor(Color.WHITE);
        }

        h.btnViewResult.setOnClickListener(v -> {
            if (!aiReady) {
                // Nothing to show yet
                android.widget.Toast.makeText(context,
                        "AI analysis is still being prepared. Please wait.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onClick(b);
        });

        h.btnDismiss.setOnClickListener(v -> {
            String bookingId = b.getBooking_id();
            if (bookingId != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("test_bookings")
                        .child(bookingId)
                        .removeValue()
                        .addOnSuccessListener(unused -> {
                            int currentPos = h.getAdapterPosition();
                            if (currentPos != RecyclerView.NO_POSITION) {
                                bookings.remove(currentPos);
                                notifyItemRemoved(currentPos);
                                android.widget.Toast.makeText(context, "Result dismissed", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTestName, tvBookingDate, tvAmount, tvPaymentStatus, btnViewResult, btnDismiss;

        ViewHolder(@NonNull View item) {
            super(item);
            tvTestName      = item.findViewById(R.id.tvResultTestName);
            tvBookingDate   = item.findViewById(R.id.tvResultBookingDate);
            tvAmount        = item.findViewById(R.id.tvResultAmount);
            tvPaymentStatus = item.findViewById(R.id.tvPaymentStatus);
            btnViewResult   = item.findViewById(R.id.btnViewResult);
            btnDismiss      = item.findViewById(R.id.btnDismiss);
        }
    }
}
