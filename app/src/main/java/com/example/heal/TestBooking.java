package com.example.heal;

public class TestBooking {
    private String booking_id;
    private String user_id;
    private String user_name;
    private String test_id;
    private String test_name;
    private String booking_date;
    private String preferred_date;
    private String time_slot;
    private String appointment_type;
    private String status;
    private double total_amount;
    private String payment_status;
    private String created_at;
    private String preparation_instructions;
    // AI fields
    private String ai_result;
    private boolean ai_result_ready;
    private String user_answers;
    private String account_number;

    public TestBooking() {}

    public String getBooking_id() { return booking_id; }
    public void setBooking_id(String booking_id) { this.booking_id = booking_id; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }

    public String getTest_id() { return test_id; }
    public void setTest_id(String test_id) { this.test_id = test_id; }

    public String getTest_name() { return test_name; }
    public void setTest_name(String test_name) { this.test_name = test_name; }

    public String getBooking_date() { return booking_date; }
    public void setBooking_date(String booking_date) { this.booking_date = booking_date; }

    public String getPreferred_date() { return preferred_date; }
    public void setPreferred_date(String preferred_date) { this.preferred_date = preferred_date; }

    public String getTime_slot() { return time_slot; }
    public void setTime_slot(String time_slot) { this.time_slot = time_slot; }

    public String getAppointment_type() { return appointment_type; }
    public void setAppointment_type(String appointment_type) { this.appointment_type = appointment_type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal_amount() { return total_amount; }
    public void setTotal_amount(double total_amount) { this.total_amount = total_amount; }

    public String getPayment_status() { return payment_status; }
    public void setPayment_status(String payment_status) { this.payment_status = payment_status; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getPreparation_instructions() { return preparation_instructions; }
    public void setPreparation_instructions(String preparation_instructions) { this.preparation_instructions = preparation_instructions; }

    public String getAi_result() { return ai_result; }
    public void setAi_result(String ai_result) { this.ai_result = ai_result; }

    public boolean isAi_result_ready() { return ai_result_ready; }
    public void setAi_result_ready(boolean ai_result_ready) { this.ai_result_ready = ai_result_ready; }

    public String getUser_answers() { return user_answers; }
    public void setUser_answers(String user_answers) { this.user_answers = user_answers; }

    public String getAccount_number() { return account_number; }
    public void setAccount_number(String account_number) { this.account_number = account_number; }
}
