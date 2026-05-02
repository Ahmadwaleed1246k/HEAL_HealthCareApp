package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorChatFragment extends Fragment {

    private RecyclerView rvChatRequests;
    private DoctorChatAdapter adapter;
    private List<ChatMessage> chatList;
    private TextView tvNoRequests;
    private DatabaseReference mDatabase;
    private String doctorId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_chat, container, false);

        rvChatRequests = view.findViewById(R.id.rvChatRequests);
        tvNoRequests = view.findViewById(R.id.tvNoRequests);
        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvChatRequests.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatList = new ArrayList<>();
        adapter = new DoctorChatAdapter(chatList, message -> {
            Intent intent = new Intent(getActivity(), DoctorReplyActivity.class);
            intent.putExtra("chatMessage", message);
            startActivity(intent);
        });
        rvChatRequests.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats");
        fetchChatRequests();

        return view;
    }

    private void fetchChatRequests() {
        mDatabase.orderByChild("doctorId").equalTo(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ChatMessage message = postSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        chatList.add(0, message); // Latest first
                    }
                }
                
                if (chatList.isEmpty()) {
                    tvNoRequests.setVisibility(View.VISIBLE);
                    rvChatRequests.setVisibility(View.GONE);
                } else {
                    tvNoRequests.setVisibility(View.GONE);
                    rvChatRequests.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
