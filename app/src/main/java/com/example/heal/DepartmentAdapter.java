package com.example.heal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private List<String> departments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String department);
    }

    public DepartmentAdapter(List<String> departments, OnItemClickListener listener) {
        this.departments = departments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_department, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String dept = departments.get(position);
        holder.tvDeptName.setText(dept);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(dept));
    }

    @Override
    public int getItemCount() {
        return departments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeptName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeptName = itemView.findViewById(R.id.tvDeptName);
        }
    }
}
