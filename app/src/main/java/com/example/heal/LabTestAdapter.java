package com.example.heal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heal.R;
import com.example.heal.LabTest;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class LabTestAdapter extends RecyclerView.Adapter<LabTestAdapter.ViewHolder> {

    private Context context;
    private List<LabTest> labTests;
    private OnTestClickListener listener;

    public interface OnTestClickListener {
        void onTestClick(LabTest labTest);
    }

    public LabTestAdapter(Context context, List<LabTest> labTests, OnTestClickListener listener) {
        this.context = context;
        this.labTests = labTests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lab_test_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LabTest test = labTests.get(position);
        holder.tvCategoryName.setText(test.getName());
        holder.tvCategoryDesc.setText(test.getDescription());

        // Set colors based on position/category for aesthetic
        int bgColor = Color.parseColor("#0E6858"); // Default primary
        int textColor = Color.WHITE;
        
        switch (position % 4) {
            case 0:
                bgColor = Color.parseColor("#0E6858"); // Dark Green
                textColor = Color.WHITE;
                break;
            case 1:
                bgColor = Color.parseColor("#FFFFFF"); // White
                textColor = Color.parseColor("#191C1D");
                break;
            case 2:
                bgColor = Color.parseColor("#F2F4F5"); // Light Gray
                textColor = Color.parseColor("#191C1D");
                break;
            case 3:
                bgColor = Color.parseColor("#A66A53"); // Brown/Earth
                textColor = Color.WHITE;
                break;
        }

        holder.cardCategory.setCardBackgroundColor(bgColor);
        holder.tvCategoryName.setTextColor(textColor);
        holder.tvCategoryDesc.setTextColor(textColor);
        holder.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(textColor));

        holder.cardCategory.setOnClickListener(v -> listener.onTestClick(test));
        holder.btnExplore.setOnClickListener(v -> listener.onTestClick(test));
    }

    @Override
    public int getItemCount() {
        return labTests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryDesc;
        TextView btnExplore;
        ImageView ivCategoryIcon;
        MaterialCardView cardCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDesc = itemView.findViewById(R.id.tvCategoryDesc);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            cardCategory   = itemView.findViewById(R.id.cardCategory);
            btnExplore     = itemView.findViewById(R.id.btnExploreTests);
        }
    }
}
