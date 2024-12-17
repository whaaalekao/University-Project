package com.example.a20240416restart;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<com.example.a20240416restart.DayItem> dayItems;
    private OnDateSelectedListener onDateSelectedListener;
    private int selectedPosition = -1; // 預設為未選中

    // 定義接口，用於傳遞點擊的日期
    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    public DayAdapter(List<com.example.a20240416restart.DayItem> dayItems, OnDateSelectedListener listener) {
        this.dayItems = dayItems;
        this.onDateSelectedListener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        com.example.a20240416restart.DayItem dayItem = dayItems.get(position);
        holder.tvDayLabel.setText(dayItem.getDayLabel());
        holder.tvDate.setText(dayItem.getDate());

        // 設置背景顏色，選中項為灰色，未選中項為白色
        holder.itemView.setBackgroundColor(position == selectedPosition ? Color.GRAY : Color.WHITE);

        // 設置點擊事件監聽器
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected); // 重設上次選中項目
            notifyItemChanged(selectedPosition); // 更新當前選中項目

            if (onDateSelectedListener != null) {
                onDateSelectedListener.onDateSelected(dayItem.getDate());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayItems.size();
    }

    // 新增方法設置初始選中項目
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // 通知UI更新選中狀態
    }

    // 新增方法來獲取目前選中的日期標籤
    public String getSelectedDayLabel() {
        if (selectedPosition != -1 && selectedPosition < dayItems.size()) {
            return dayItems.get(selectedPosition).getDayLabel(); // 假設 DayItem 有 getDayLabel() 方法
        }
        return null;
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayLabel, tvDate;

        DayViewHolder(View itemView) {
            super(itemView);
            tvDayLabel = itemView.findViewById(R.id.tv_day_label);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
