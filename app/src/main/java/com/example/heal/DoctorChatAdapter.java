package com.example.heal;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorChatAdapter extends RecyclerView.Adapter<DoctorChatAdapter.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private OnReplyClickListener listener;

    public interface OnReplyClickListener {
        void onReplyClick(ChatMessage message);
    }

    public DoctorChatAdapter(List<ChatMessage> chatMessages, OnReplyClickListener listener) {
        this.chatMessages = chatMessages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.tvPatientName.setText(message.getPatientName());
        holder.tvTimestamp.setText(message.getTimestamp());
        holder.tvSymptoms.setText(message.getSymptoms());
        holder.tvStatus.setText(message.getStatus().toUpperCase());

        if ("replied".equalsIgnoreCase(message.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tab_active); // Teal color from existing drawables
            holder.btnReply.setText("View / Edit Reply");
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_blood_badge); // Red color
            holder.btnReply.setText("Reply");
        }

        holder.btnReply.setOnClickListener(v -> listener.onReplyClick(message));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvTimestamp, tvSymptoms, tvStatus;
        Button btnReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvSymptoms = itemView.findViewById(R.id.tvSymptoms);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnReply = itemView.findViewById(R.id.btnReply);
        }
    }
}
