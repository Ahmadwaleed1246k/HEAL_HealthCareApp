package com.example.heal;

import java.io.Serializable;
import java.util.List;

public class Doctor implements Serializable {
    private String doctor_id;
    private String name;
    private String specialization;
    private String profile_picture;
    private double rating;
    private int total_reviews;
    private int experience_years;
    private String about;
    private List<String> sub_specialties;
    private java.util.Map<String, Object> timings;

    // Required empty constructor for Firebase
    public Doctor() {}

    public java.util.Map<String, Object> getTimings() { return timings; }
    public void setTimings(java.util.Map<String, Object> timings) { this.timings = timings; }

    public Doctor(String name, String specialization, double rating, String profile_picture) {
        this.name = name;
        this.specialization = specialization;
        this.rating = rating;
        this.profile_picture = profile_picture;
    }

    // Getters and Setters
    public String getDoctor_id() { return doctor_id; }
    public void setDoctor_id(String doctor_id) { this.doctor_id = doctor_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getTotal_reviews() { return total_reviews; }
    public void setTotal_reviews(int total_reviews) { this.total_reviews = total_reviews; }

    public int getExperience_years() { return experience_years; }
    public void setExperience_years(int experience_years) { this.experience_years = experience_years; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public List<String> getSub_specialties() { return sub_specialties; }
    public void setSub_specialties(List<String> sub_specialties) { this.sub_specialties = sub_specialties; }
}
