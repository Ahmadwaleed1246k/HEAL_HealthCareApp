package com.example.heal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChatPaymentBottomSheet extends BottomSheetDialogFragment {

    public interface OnPaymentSuccess {
        void onPaid(String cardNumber);
    }

    private final String doctorName;
    private final double fee;
    private final OnPaymentSuccess callback;

    public ChatPaymentBottomSheet(String doctorName, double fee, OnPaymentSuccess callback) {
        this.doctorName = doctorName;
        this.fee = fee;
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_chat_payment, container, false);

        TextView tvDoctorName = view.findViewById(R.id.tvPayDoctorName);
        TextView tvFee        = view.findViewById(R.id.tvPayFee);
        EditText etCard       = view.findViewById(R.id.etCardNumber);
        TextView tvError      = view.findViewById(R.id.tvCardError);
        TextView btnPay       = view.findViewById(R.id.btnPayChat);
        TextView btnCancel    = view.findViewById(R.id.btnCancelChatPayment);

        tvDoctorName.setText(doctorName);
        tvFee.setText("$" + (int) fee);

        etCard.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnPay.setOnClickListener(v -> {
            String card = etCard.getText().toString().trim();
            if (card.length() != 16) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Card number must be exactly 16 digits.");
                return;
            }
            if (!card.matches("\\d{16}")) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Only digits allowed.");
                return;
            }
            
            btnPay.setText("Processing...");
            btnPay.setEnabled(false);
            
            // Simulate payment processing
            view.postDelayed(() -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Payment successful!", Toast.LENGTH_SHORT).show();
                    dismiss();
                    if (callback != null) callback.onPaid(card);
                }
            }, 1500);
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return view;
    }
}
