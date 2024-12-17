package com.example.a20240416restart;

public class ListItem {
    private String imageUrl;
    private String title;
    private String description;
    private boolean isClosed;

    public ListItem(String imageUrl, String title, String description, boolean isClosed) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.isClosed = isClosed;
    }

    public String getImageUrl() {
        return imageUrl;
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
}
