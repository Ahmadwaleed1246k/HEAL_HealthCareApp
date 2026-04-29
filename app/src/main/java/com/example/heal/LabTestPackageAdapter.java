package com.example.heal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heal.R;
import com.example.heal.LabTest;

import java.util.List;

public class LabTestPackageAdapter extends RecyclerView.Adapter<LabTestPackageAdapter.ViewHolder> {

    private Context context;
    private List<LabTest> packages;
    private OnPackageClickListener listener;

    public interface OnPackageClickListener {
        void onAddToCartClick(LabTest labTest);
    }

    public LabTestPackageAdapter(Context context, List<LabTest> packages, OnPackageClickListener listener) {
        this.context = context;
        this.packages = packages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lab_test_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LabTest pkg = packages.get(position);
        holder.tvPackageName.setText(pkg.getName());
        holder.tvPackagePrice.setText("$" + String.format("%.2f", pkg.getPrice()));
        holder.tvPackageDesc.setText(pkg.getDescription());
        holder.tvTat.setText("⏱ " + pkg.getTurnaround_time() + " TAT");
        
        if (pkg.isFasting_required()) {
            holder.tvFasting.setText("🚫 " + pkg.getFasting_hours() + "h Fasting");
            holder.tvFasting.setVisibility(View.VISIBLE);
        } else {
            holder.tvFasting.setText("No Fasting");
        }

        holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCartClick(pkg));
        holder.btnDetails.setOnClickListener(v -> listener.onAddToCartClick(pkg)); // Use same bottom sheet
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPackageName, tvPackagePrice, tvPackageDesc, tvTat, tvFasting, btnDetails, btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvPackagePrice = itemView.findViewById(R.id.tvPackagePrice);
            tvPackageDesc = itemView.findViewById(R.id.tvPackageDesc);
            tvTat = itemView.findViewById(R.id.tvTat);
            tvFasting = itemView.findViewById(R.id.tvFasting);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
