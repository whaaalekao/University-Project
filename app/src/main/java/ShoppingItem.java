package com.example.a20240416restart;

import android.os.Parcel;
import android.os.Parcelable;

public class ShoppingItem implements Parcelable {
    private int imageId;
    private String title;
    private String description;
    private boolean isClosed;
    private int quantity;
    private String restaurantName;
    private int calories;
    private int sugar;
    private int protein;
    private int carbohydrates;
    private int fat;
    private String imageUrl;  // 新增圖片URL字段

    // 完整的構造函數
    public ShoppingItem(int imageId, String title, String description, boolean isClosed, int quantity,
                        String restaurantName, String imageUrl, int calories, int sugar, int protein,
                        int carbohydrates, int fat) {
        this.imageId = imageId;
        this.title = title;
        this.description = description;
        this.isClosed = isClosed;
        this.quantity = quantity;
        this.restaurantName = restaurantName;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.sugar = sugar;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
    }

    // 簡化版構造函數，不包含營養成分
    public ShoppingItem(int imageId, String title, String description, boolean isClosed, int quantity,
                        String restaurantName, String imageUrl) {
        this(imageId, title, description, isClosed, quantity, restaurantName, imageUrl, 0, 0, 0, 0, 0); // 使用默認值
    }

    // Parcelable implementation
    protected ShoppingItem(Parcel in) {
        imageId = in.readInt();
        title = in.readString();
        description = in.readString();
        isClosed = in.readByte() != 0;
        quantity = in.readInt();
        restaurantName = in.readString();
        calories = in.readInt();
        sugar = in.readInt();
        protein = in.readInt();
        carbohydrates = in.readInt();
        fat = in.readInt();
        imageUrl = in.readString();  // 读取圖片URL
    }

    public static final Creator<ShoppingItem> CREATOR = new Creator<ShoppingItem>() {
        @Override
        public ShoppingItem createFromParcel(Parcel in) {
            return new ShoppingItem(in);
        }

        @Override
        public ShoppingItem[] newArray(int size) {
            return new ShoppingItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeByte((byte) (isClosed ? 1 : 0));
        dest.writeInt(quantity);
        dest.writeString(restaurantName);
        dest.writeInt(calories);
        dest.writeInt(sugar);
        dest.writeInt(protein);
        dest.writeInt(carbohydrates);
        dest.writeInt(fat);
        dest.writeString(imageUrl);  // 保存圖片URL
    }

    // Getter and setter methods
    public int getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public int getCalories() {
        return calories;
    }

    public int getSugar() {
        return sugar;
    }

    public int getProtein() {
        return protein;
    }

    public int getCarbohydrates() {
        return carbohydrates;
    }

    public int getFat() {
        return fat;
    }

    public String getImageUrl() {
        return imageUrl;  // 新增圖片URL的getter方法
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setSugar(int sugar) {
        this.sugar = sugar;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public void setCarbohydrates(int carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public void setImageUrl(String imageUrl) {  // 新增圖片URL的setter方法
        this.imageUrl = imageUrl;
    }
} 