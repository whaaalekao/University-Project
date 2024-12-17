package com.example.a20240416restart;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.a20240416restart.databinding.ActivityMain2Binding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity2 extends AppCompatActivity {

    private DatabaseReference ordersRef;
    private String currentUserId;
    private static final String CHANNEL_ID = "order_notification_channel";
    private static final String PREFS_NAME = "RejectedOrdersPrefs";
    private static final String REJECTED_ORDERS_KEY = "notifiedRejectedOrders";
    private Set<String> notifiedRejectedOrders;
    ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 從 SharedPreferences 讀取已通知的拒絕訂單
        notifiedRejectedOrders = getNotifiedRejectedOrders();

        // 設置初始 Fragment
        replaceFragment(new HomeFragment());

        // 底部導航監聽器設置
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.history_order) {
                replaceFragment(new history_orderFragment());
            } else if (itemId == R.id.health) {
                replaceFragment(new healthFragment());
            } else if (itemId == R.id.setting) {
                replaceFragment(new settingFragment());
            }

            return true;
        });

        // 建立通知頻道
        createNotificationChannel();

        // 取得從 MainActivity 傳遞過來的當前使用者 UID
        currentUserId = getIntent().getStringExtra("uid");

        if (currentUserId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            ordersRef = database.getReference("Orders");

            // 使用 addChildEventListener 只監聽單一訂單的新增和更改事件
            ordersRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @NonNull String previousChildName) {
                    handleOrderData(snapshot);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @NonNull String previousChildName) {
                    handleOrderData(snapshot);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @NonNull String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "DatabaseError: " + error.getMessage());
                }
            });
        }
    }

    // 處理訂單資料變化
    private void handleOrderData(DataSnapshot orderSnapshot) {
        String orderId = orderSnapshot.getKey();
        Order order = orderSnapshot.getValue(Order.class);

        if (order != null && currentUserId.equals(order.getUserUid())) {
            // 處理取餐提醒
            long currentTime = System.currentTimeMillis();
            long remainingTime = order.getTimestamp() - currentTime;

            if (remainingTime <= 0) {
                String userName = order.get名字() != null ? order.get名字() : "未知訂購人";
                String restaurantName = order.getRestaurantName() != null ? order.getRestaurantName() : "未知地點";

                sendPickupNotification(userName, restaurantName); // 發送取餐通知
            }

            // 處理訂單完成邏輯
            if ("完成訂單".equals(order.get接單狀況())) {
                if (orderId != null && orderId.length() >= 6) {
                    String orderLast6 = orderId.substring(orderId.length() - 6); // 提取訂單後6碼
                    sendOrderCompletedNotification(order, orderLast6); // 發送完成通知
                } else {
                    Log.e("handleOrderData", "訂單編號不足6碼，無法提取後6碼");
                }
            }

            // 處理訂單拒絕邏輯
            if ("拒絕訂單".equals(order.get接單狀況()) && !notifiedRejectedOrders.contains(orderId)) {
                String rejectReason = orderSnapshot.child("拒絕原因").getValue(String.class);

                if (rejectReason == null) {
                    Log.e("handleOrderData", "拒絕原因為 null，請檢查 Firebase 資料結構是否正確");
                } else {
                    sendRejectedNotification(order, rejectReason); // 發送通知
                    notifiedRejectedOrders.add(orderId); // 更新已通知的訂單集合
                    saveNotifiedRejectedOrders(); // 儲存通知狀態
                }
            }
        }
    }


    // 發送取餐提醒通知
    private void sendPickupNotification(String userName, String restaurantName) {
        String message = "訂購人: " + userName + "\n" +
                "取餐地點: " + restaurantName + "\n" +
                "提醒: 您的訂單已準備好，請前往取餐。";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("取餐提醒")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((userName + restaurantName).hashCode(), builder.build());
    }

    // 創建通知頻道
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Channel for order status notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void sendOrderCompletedNotification(Order order, String orderLast6) {
        String orderTime = formatTimestamp(order.getTimestamp());
        String userName = order.get名字() != null ? order.get名字() : "未知訂購人";
        String restaurantName = order.getRestaurantName() != null ? order.getRestaurantName() : "未知餐廳";

        String message = "訂餐時間: " + orderTime + "\n" +
                "訂購人: " + userName + "\n" +
                "餐廳: " + restaurantName + "\n" +
                "訂單後6碼: " + orderLast6 + "\n" +
                "您的訂單已完成，感謝使用！";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("訂單完成通知")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(order.hashCode(), builder.build());
    }

    // 發送訂單拒絕的通知
    private void sendRejectedNotification(Order order, String rejectReason) {
        String orderTime = formatTimestamp(order.getTimestamp());
        String userName = order.get名字() != null ? order.get名字() : "未知訂購人";
        String restaurantName = order.getRestaurantName() != null ? order.getRestaurantName() : "未知餐廳";

        String reasonMessage = (rejectReason != null && !rejectReason.isEmpty()) ? rejectReason : "無法提供原因";

        String message = "訂餐時間: " + orderTime + "\n" +
                "訂購人: " + userName + "\n" +
                "餐廳: " + restaurantName + "\n" +
                "原因: " + reasonMessage;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("您的訂單已被拒絕")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(order.hashCode(), builder.build());
    }

    // 將時間戳轉換為格式化日期時間字串
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // 切換 Fragment 顯示
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // 使用 SharedPreferences 來儲存已通知的拒絕訂單 ID
    private void saveNotifiedRejectedOrders() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(REJECTED_ORDERS_KEY, notifiedRejectedOrders);
        editor.apply();
    }

    // 從 SharedPreferences 讀取已通知的拒絕訂單 ID
    private Set<String> getNotifiedRejectedOrders() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return new HashSet<>(preferences.getStringSet(REJECTED_ORDERS_KEY, new HashSet<>()));
    }

    // Order 類別
    public static class Order {
        private String 名字;
        private String 接單狀況;
        private String userUid;
        private String restaurantName;
        private long timestamp;

        public Order() {}

        public String get名字() {
            return 名字;
        }

        public void set名字(String 名字) {
            this.名字 = 名字;
        }

        public String get接單狀況() {
            return 接單狀況;
        }

        public void set接單狀況(String 接單狀況) {
            this.接單狀況 = 接單狀況;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
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
    }
}