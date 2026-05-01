package com.example.heal;

public class BookingDate {
    private String date; // e.g. "2026-05-02"
    private String dayName; // e.g. "Mon"
    private String dayNumber; // e.g. "02"
    private boolean isSelected;

    public BookingDate(String date, String dayName, String dayNumber) {
        this.date = date;
        this.dayName = dayName;
        this.dayNumber = dayNumber;
    }

    public String getDate() { return date; }
    public String getDayName() { return dayName; }
    public String getDayNumber() { return dayNumber; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
