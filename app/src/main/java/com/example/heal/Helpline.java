package com.example.heal;

public class Helpline {
    private String name;
    private String number;
    private String description;

    public Helpline(String name, String number, String description) {
        this.name = name;
        this.number = number;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }
}
