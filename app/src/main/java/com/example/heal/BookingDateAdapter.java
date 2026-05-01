package com.example.heal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingDateAdapter extends RecyclerView.Adapter<BookingDateAdapter.DateViewHolder> {

    private Context context;
    private List<BookingDate> dateList;
    private OnDateClickListener listener;
    private int selectedPosition = 0;

    public interface OnDateClickListener {
        void onDateClick(BookingDate bookingDate);
    }

    public BookingDateAdapter(Context context, List<BookingDate> dateList, OnDateClickListener listener) {
        this.context = context;
        this.dateList = dateList;
        this.listener = listener;
        if (!dateList.isEmpty()) {
            dateList.get(0).setSelected(true);
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        BookingDate bookingDate = dateList.get(position);
        holder.tvDayName.setText(bookingDate.getDayName());
        holder.tvDayNumber.setText(bookingDate.getDayNumber());

        if (bookingDate.isSelected()) {
            holder.llContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.tvDayName.setTextColor(Color.WHITE);
            holder.tvDayNumber.setTextColor(Color.WHITE);
        } else {
            holder.llContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.medium_gray));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            dateList.get(previousSelected).setSelected(false);
            dateList.get(selectedPosition).setSelected(true);
            
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onDateClick(bookingDate);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;
        LinearLayout llContainer;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            llContainer = itemView.findViewById(R.id.llDateContainer);
        }
    }
}
