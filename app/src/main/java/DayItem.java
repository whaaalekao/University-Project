package com.example.a20240416restart;

public class DayItem {
    private String dayLabel;
    private String date;
    private boolean isSelected; // 添加一个字段表示是否被选中

    public DayItem(String dayLabel, String date) {
        this.dayLabel = dayLabel;
        this.date = date;
        this.isSelected = false; // 初始化时默认为未选中
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
