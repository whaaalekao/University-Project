package com.example.a20240416restart;

import java.util.List;

public class Order {
    private String 名字;
    private String restaurantName;
    private long timestamp;
    private double totalPrice;
    private long uploadTimestamp;
    private String userUid;
    private int differenceInMinutes;
    private String status; // 新增 status 欄位
    private List<com.example.a20240416restart.Item> items;

    // Getter 和 Setter 方法
    public String get名字() {
        return 名字;
    }

    public void set名字(String 名字) {
        this.名字 = 名字;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public int getDifferenceInMinutes() {
        return differenceInMinutes;
    }

    public void setDifferenceInMinutes(int differenceInMinutes) {
        this.differenceInMinutes = differenceInMinutes;
    }

    public String getStatus() {
        return status; // 新增 getStatus 方法
    }

    public void setStatus(String status) {
        this.status = status; // 新增 setStatus 方法
    }

    public List<com.example.a20240416restart.Item> getItems() {
        return items;
    }

    public void setItems(List<com.example.a20240416restart.Item> items) {
        this.items = items;
    }
}
