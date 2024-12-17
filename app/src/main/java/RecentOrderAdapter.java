package com.example.a20240416restart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ORDER = 0;
    private static final int VIEW_TYPE_EMPTY = 1;

    private List<Order> orderList;
    private Context context;

    public RecentOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public int getItemViewType(int position) {
        if (orderList.isEmpty()) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_ORDER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_no_orders, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
            return new OrderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ORDER) {
            Order order = orderList.get(position);
            OrderViewHolder orderHolder = (OrderViewHolder) holder;
            orderHolder.tvOrderStatus.setText("接單狀況: " + order.getStatus());
            orderHolder.tvRestaurantName.setText("取餐地點: " + order.getRestaurantName());
            orderHolder.tvTimeRemaining.setText("取餐剩餘時間: " + getTimeRemaining(order.getTimestamp()));
            orderHolder.tvOrderItems.setText("點餐項目: " + getOrderItems(order.getItems()));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.isEmpty() ? 1 : orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderStatus, tvRestaurantName, tvTimeRemaining, tvOrderItems;

        public OrderViewHolder(View itemView) {
            super(itemView);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            tvTimeRemaining = itemView.findViewById(R.id.tv_time_remaining);
            tvOrderItems = itemView.findViewById(R.id.tv_order_items);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmptyMessage;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            tvEmptyMessage = itemView.findViewById(R.id.tv_empty_message);
        }
    }

    private String getTimeRemaining(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = timestamp - currentTime;

        if (timeDiff <= 0) {
            return "已過期";
        }

        long days = timeDiff / (1000 * 60 * 60 * 24);
        long hours = (timeDiff / (1000 * 60 * 60)) % 24;
        long minutes = (timeDiff / (1000 * 60)) % 60;

        StringBuilder timeRemaining = new StringBuilder();
        if (days > 0) {
            timeRemaining.append(days).append(" 天 ");
        }
        if (hours > 0 || days > 0) {
            timeRemaining.append(hours).append(" 小時 ");
        }
        timeRemaining.append(minutes).append(" 分鐘");

        return timeRemaining.toString();
    }

    private String getOrderItems(List<com.example.a20240416restart.Item> items) {
        StringBuilder itemsList = new StringBuilder();
        for (com.example.a20240416restart.Item item : items) {
            itemsList.append(item.getTitle()).append(" x").append(item.getQuantity()).append(", ");
        }
        if (itemsList.length() > 0) {
            itemsList.setLength(itemsList.length() - 2);
        }
        return itemsList.toString();
    }
}
