package com.example.heal;

import java.util.List;

public class LabTest {
    private String test_id;
    private String name;
    private String category;
    private String description;
    private double price;
    private String turnaround_time;
    private boolean fasting_required;
    private int fasting_hours;
    private String preparation_instructions;
    private List<String> markers;
    private boolean popular;
    private boolean available_for_home_collection;
    private boolean available_for_clinic_visit;

    public LabTest() {
    }

    public String getTest_id() { return test_id; }
    public void setTest_id(String test_id) { this.test_id = test_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getTurnaround_time() { return turnaround_time; }
    public void setTurnaround_time(String turnaround_time) { this.turnaround_time = turnaround_time; }

    public boolean isFasting_required() { return fasting_required; }
    public void setFasting_required(boolean fasting_required) { this.fasting_required = fasting_required; }

    public int getFasting_hours() { return fasting_hours; }
    public void setFasting_hours(int fasting_hours) { this.fasting_hours = fasting_hours; }

    public String getPreparation_instructions() { return preparation_instructions; }
    public void setPreparation_instructions(String preparation_instructions) { this.preparation_instructions = preparation_instructions; }

    public List<String> getMarkers() { return markers; }
    public void setMarkers(List<String> markers) { this.markers = markers; }

    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }

    public boolean isAvailable_for_home_collection() { return available_for_home_collection; }
    public void setAvailable_for_home_collection(boolean available_for_home_collection) { this.available_for_home_collection = available_for_home_collection; }

    public boolean isAvailable_for_clinic_visit() { return available_for_clinic_visit; }
    public void setAvailable_for_clinic_visit(boolean available_for_clinic_visit) { this.available_for_clinic_visit = available_for_clinic_visit; }
}
