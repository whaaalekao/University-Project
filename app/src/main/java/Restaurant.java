package com.example.a20240416restart;

public class Restaurant {
    private String name;
    private String description;
    private String tag; // 新增 tag 屬性
    private double rating;

    public Restaurant(String name, String description, String tag, double rating) {
        this.name = name;
        this.description = description;
        this.tag = tag;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTag() {
        return tag;
    }

    public double getRating() {
        return rating;
    }
}
