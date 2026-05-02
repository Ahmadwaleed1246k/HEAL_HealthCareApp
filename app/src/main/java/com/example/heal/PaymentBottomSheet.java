package com.example.heal;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentBottomSheet extends BottomSheetDialogFragment {

    public interface OnPaymentSuccess {
        void onPaid(String bookingId);
    }

    private final TestBooking booking;
    private final OnPaymentSuccess callback;

    public PaymentBottomSheet(TestBooking booking, OnPaymentSuccess callback) {
        this.booking = booking;
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_payment, container, false);

        TextView tvTestName = view.findViewById(R.id.tvPayTestName);
        TextView tvAmount   = view.findViewById(R.id.tvPayAmount);
        EditText etAccount  = view.findViewById(R.id.etAccountNumber);
        TextView tvError    = view.findViewById(R.id.tvAccountError);
        TextView btnPay     = view.findViewById(R.id.btnPay);
        TextView btnCancel  = view.findViewById(R.id.btnCancelPayment);

        tvTestName.setText(booking.getTest_name());
        tvAmount.setText("$" + (int) booking.getTotal_amount());

        etAccount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnPay.setOnClickListener(v -> {
            String acct = etAccount.getText().toString().trim();
            if (acct.length() != 16) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Account number must be exactly 16 digits.");
                return;
            }
            if (!acct.matches("\\d{16}")) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Only digits allowed.");
                return;
            }
            processPayment(acct, btnPay);
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return view;
    }

    private void processPayment(String accountNumber, TextView btnPay) {
        btnPay.setText("Processing...");
        btnPay.setEnabled(false);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("test_bookings")
                .child(booking.getBooking_id());

        ref.child("payment_status").setValue("paid");
        ref.child("account_number").setValue(accountNumber)
            .addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), "Payment successful!", Toast.LENGTH_SHORT).show();
                dismiss();
                if (callback != null) callback.onPaid(booking.getBooking_id());
            })
            .addOnFailureListener(e -> {
                btnPay.setText("Pay & View Results");
                btnPay.setEnabled(true);
                Toast.makeText(getContext(), "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
