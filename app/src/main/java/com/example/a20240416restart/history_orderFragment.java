package com.example.a20240416restart;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import androidx.appcompat.widget.SearchView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class history_orderFragment extends Fragment {

    private ExpandableListView expandableListView;
    private SimpleExpandableListAdapter expandableListAdapter;
    private List<Map<String, String>> listGroupTitles;
    private List<List<Map<String, String>>> listChildItems;
    private FirebaseAuth mAuth;
    private TextView recentOrderTextView;
    private RecyclerView recyclerRecentOrders;
    private com.example.a20240416restart.RecentOrderAdapter recentOrderAdapter;
    private List<Order> recentOrderList = new ArrayList<>();
    private static final String CHANNEL_ID = "OrderStatusChannel";
    private boolean isExpanded = false;

    public history_orderFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_order, container, false);

        createNotificationChannel();

        expandableListView = view.findViewById(R.id.expandableListView);
        recyclerRecentOrders = view.findViewById(R.id.recycler_recent_orders);
        recyclerRecentOrders.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        recentOrderAdapter = new com.example.a20240416restart.RecentOrderAdapter(getContext(), recentOrderList);
        recyclerRecentOrders.setAdapter(recentOrderAdapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userUid = currentUser.getUid();
            loadRecentOrder(userUid);
            loadDataFromFirebase(userUid);
        }

        listGroupTitles = new ArrayList<>();
        listChildItems = new ArrayList<>();
        expandableListAdapter = new SimpleExpandableListAdapter(
                getContext(),
                listGroupTitles,
                android.R.layout.simple_expandable_list_item_1,
                new String[]{"OrderInfo"},
                new int[]{android.R.id.text1},
                listChildItems,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{"ItemInfo"},
                new int[]{android.R.id.text1}
        );

        expandableListView.setAdapter(expandableListAdapter);

        Button toggleExpandButton = view.findViewById(R.id.btn_toggle_expand);
        toggleExpandButton.setOnClickListener(v -> {
            if (isExpanded) {
                collapseAllGroups();
            } else {
                expandAllGroups();
            }
            isExpanded = !isExpanded;
            toggleExpandButton.setText(isExpanded ? "收起所有" : "展開所有");
        });

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterOrders(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterOrders(newText);
                return true;
            }
        });

        return view;
    }

    private void expandAllGroups() {
        for (int i = 0; i < listGroupTitles.size(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    private void collapseAllGroups() {
        for (int i = 0; i < listGroupTitles.size(); i++) {
            expandableListView.collapseGroup(i);
        }
    }

    private void loadRecentOrder(String userUid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
        databaseReference.orderByChild("userUid").equalTo(userUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        recentOrderList.clear();
                        long currentTime = System.currentTimeMillis(); // 取得當前時間

                        for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                            String status = orderSnapshot.child("接單狀況").getValue(String.class);
                            Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);

                            // 只顯示尚未過期且狀態為未完成的訂單
                            if (status != null && timestamp != null && timestamp > currentTime && !status.equals("完成訂單") && !status.equals("拒絕訂單") && !status.equals("已過期")) {
                                Order order = new Order();
                                order.setRestaurantName(orderSnapshot.child("restaurantName").getValue(String.class));
                                order.setItems(getItemList(orderSnapshot.child("items")));
                                order.setTimestamp(timestamp);
                                order.setStatus(status);
                                recentOrderList.add(order);
                            }
                        }
                        recentOrderAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }


    private List<com.example.a20240416restart.Item> getItemList(DataSnapshot itemsSnapshot) {
        List<com.example.a20240416restart.Item> items = new ArrayList<>();
        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
            com.example.a20240416restart.Item item = new com.example.a20240416restart.Item();
            item.setTitle(itemSnapshot.child("title").getValue(String.class));
            item.setQuantity(itemSnapshot.child("quantity").getValue(Integer.class));
            items.add(item);
        }
        return items;
    }

    private void loadDataFromFirebase(String userUid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
        databaseReference.orderByChild("userUid").equalTo(userUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listGroupTitles.clear();
                        listChildItems.clear();

                        for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                            String status = orderSnapshot.child("接單狀況").getValue(String.class);

                            // 顯示完成、拒絕和已過期的訂單
                            if (status.equals("完成訂單") || status.equals("拒絕訂單") || status.equals("已過期")) {
                                String orderInfo = "｜訂購人: " + orderSnapshot.child("名字").getValue(String.class);
                                Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);

                                if (timestamp != null) {
                                    String formattedDate = formatTimestampToDate(timestamp);
                                    orderInfo += " | 取餐時間: " + formattedDate;
                                }

                                Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);
                                if (totalPrice != null) {
                                    orderInfo += " | 總價: " + totalPrice + "元";
                                }

                                orderInfo += " | 狀態: " + status; // 加入訂單狀態

                                Map<String, String> group = new HashMap<>();
                                group.put("OrderInfo", orderInfo);

                                List<Map<String, String>> childItems = new ArrayList<>();
                                for (DataSnapshot itemSnapshot : orderSnapshot.child("items").getChildren()) {
                                    String itemTitle = itemSnapshot.child("title").getValue(String.class);
                                    String itemDesc = itemSnapshot.child("description").getValue(String.class) + "元";
                                    int quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                                    String note = itemSnapshot.child("note").getValue(String.class);

                                    String itemInfo = "品項: " + itemTitle + " | 價格: " + itemDesc + " | 數量: " + quantity + (note != null ? " | 備註: " + note : "");
                                    Map<String, String> child = new HashMap<>();
                                    child.put("ItemInfo", itemInfo);
                                    childItems.add(child);
                                }

                                listGroupTitles.add(group);
                                listChildItems.add(childItems);
                            }
                        }

                        expandableListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private String formatTimestampToDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void filterOrders(String query) {
        List<Map<String, String>> filteredGroupTitles = new ArrayList<>();
        List<List<Map<String, String>>> filteredChildItems = new ArrayList<>();

        for (int i = 0; i < listGroupTitles.size(); i++) {
            Map<String, String> group = listGroupTitles.get(i);
            String orderInfo = group.get("OrderInfo");

            List<Map<String, String>> matchingChildItems = new ArrayList<>();
            List<Map<String, String>> childItems = listChildItems.get(i);

            for (Map<String, String> child : childItems) {
                String itemInfo = child.get("ItemInfo");

                if ((orderInfo != null && orderInfo.toLowerCase().contains(query.toLowerCase())) ||
                        (itemInfo != null && itemInfo.toLowerCase().contains(query.toLowerCase()))) {
                    matchingChildItems.add(child);
                }
            }

            if (!matchingChildItems.isEmpty()) {
                filteredGroupTitles.add(group);
                filteredChildItems.add(matchingChildItems);
            }
        }

        expandableListAdapter = new SimpleExpandableListAdapter(
                getContext(),
                filteredGroupTitles,
                android.R.layout.simple_expandable_list_item_1,
                new String[]{"OrderInfo"},
                new int[]{android.R.id.text1},
                filteredChildItems,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{"ItemInfo"},
                new int[]{android.R.id.text1}
        );

        expandableListView.setAdapter(expandableListAdapter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Status Channel";
            String description = "Channel for order status notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String status) {
        String message = "您的訂單" + (status.equals("已過期") ? "已過期" : "拒絕訂單");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("訂單通知")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
