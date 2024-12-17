package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class paycheck extends AppCompatActivity {

    private static final int REQUEST_EDIT_ITEM = 1;
    private static final String CHANNEL_ID = "order_notification_channel";

    private ListView listView;
    private RecyclerView recyclerView;
    private com.example.a20240416restart.ShoppingListAdapter shoppingListAdapter;
    private com.example.a20240416restart.DayAdapter dayAdapter;
    private ArrayList<com.example.a20240416restart.ShoppingItem> cartItems;
    private TimePicker timePicker;
    private String selectedTime;
    private String selectedDate;
    private int totalPrice;
    private String userUid;
    private String userName;
    private String restaurantName;

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    // Flag to track if warning has been shown
    private boolean warningShown = false;

    // Store business hours
    private List<Map<String, String>> weekdayHoursList;
    private List<Map<String, String>> holidayHoursList;
    private boolean isHoliday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paycheck);

        listView = findViewById(R.id.lvv);
        recyclerView = findViewById(R.id.recycler_view);
        Button sendButton = findViewById(R.id.sent);
        TextView totalTextView = findViewById(R.id.total);
        TextView placeTextView = findViewById(R.id.place);

        // Firebase Initialization
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userUid = currentUser.getUid();
            fetchUserNameFromFirebase(userUid);
        } else {
            Intent intent = new Intent(paycheck.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        restaurantName = getIntent().getStringExtra("restaurantName");

        if (restaurantName == null || restaurantName.isEmpty()) {
            Toast.makeText(this, "餐廳名稱未選擇！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        placeTextView.setText("取餐地點: " + restaurantName);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        cartItems = new ArrayList<>();
        loadCartFromFirebase();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(16));

        List<com.example.a20240416restart.DayItem> dayItems = prepareDayItems();
        dayAdapter = new com.example.a20240416restart.DayAdapter(dayItems, date -> {
            selectedDate = date;
            Log.d("paycheck", "Selected date: " + selectedDate);
        });
        recyclerView.setAdapter(dayAdapter);
        dayAdapter.setSelectedPosition(0);
        selectedDate = dayItems.get(0).getDate();

        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(false);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        int initialHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int initialMinute = calendar.get(Calendar.MINUTE);

        timePicker.setHour(initialHourOfDay);
        timePicker.setMinute(initialMinute);
        selectedTime = formatTime(initialHourOfDay, initialMinute);

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCalendar.set(Calendar.MINUTE, minute);

            Calendar now = Calendar.getInstance();
            now.add(Calendar.MINUTE, 15);

            if ("今天".equals(dayAdapter.getSelectedDayLabel())) {
                if (selectedCalendar.before(now)) {
                    if (!warningShown) {
                        Toast.makeText(paycheck.this, "不能選擇比當前時間+15分鐘還早的時間，請重新選擇。", Toast.LENGTH_SHORT).show();
                        warningShown = true;
                    }
                    timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
                    timePicker.setMinute(now.get(Calendar.MINUTE));
                    selectedTime = formatTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
                } else {
                    selectedTime = formatTime(hourOfDay, minute);
                    warningShown = false;
                }
            } else {
                selectedTime = formatTime(hourOfDay, minute);
                warningShown = false;
            }
        });

        fetchBusinessHours();

        sendButton.setOnClickListener(v -> {
            if (isWithinBusinessHours()) {
                uploadDataToFirebase();
            } else {
                showOutOfBusinessHoursAlert();
            }
        });
    }

    private void showOutOfBusinessHoursAlert() {
        StringBuilder hoursMessage = new StringBuilder("營業時間為:\n");
        List<Map<String, String>> hoursList = isHoliday ? holidayHoursList : weekdayHoursList;

        for (Map<String, String> timeRange : hoursList) {
            String startTime = timeRange.get("開始");
            String endTime = timeRange.get("結束");

            // 判斷是早上或下午營業時間
            String startPeriod = Integer.parseInt(startTime.split(":")[0]) < 12 ? "早上" : "下午";
            String endPeriod = Integer.parseInt(endTime.split(":")[0]) < 12 ? "早上" : "下午";

            hoursMessage.append(startPeriod)
                    .append(" ")
                    .append(startTime)
                    .append(" - ")
                    .append(endPeriod)
                    .append(" ")
                    .append(endTime)
                    .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("營業時間提示")
                .setMessage("選擇的時間不在營業時間範圍內，請重新選擇。\n" + hoursMessage.toString().trim())
                .setPositiveButton("確定", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void fetchBusinessHours() {
        DatabaseReference hoursRef = databaseReference.child("校區").child("屏商校區").child(restaurantName).child("營業");

        hoursRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                weekdayHoursList = new ArrayList<>();
                holidayHoursList = new ArrayList<>();

                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                isHoliday = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

                for (DataSnapshot timeRange : snapshot.child("平日營業時間").getChildren()) {
                    String start = timeRange.child("開始").getValue(String.class);
                    String end = timeRange.child("結束").getValue(String.class);
                    Map<String, String> timeRangeMap = new HashMap<>();
                    timeRangeMap.put("開始", start);
                    timeRangeMap.put("結束", end);
                    weekdayHoursList.add(timeRangeMap);
                }

                for (DataSnapshot timeRange : snapshot.child("假日營業時間").getChildren()) {
                    String start = timeRange.child("開始").getValue(String.class);
                    String end = timeRange.child("結束").getValue(String.class);
                    Map<String, String> timeRangeMap = new HashMap<>();
                    timeRangeMap.put("開始", start);
                    timeRangeMap.put("結束", end);
                    holidayHoursList.add(timeRangeMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("paycheck", "無法取得營業時間", error.toException());
            }
        });
    }

    private boolean isWithinBusinessHours() {
        List<Map<String, String>> hoursList;

        try {
            Calendar selectedDateCalendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            selectedDateCalendar.setTime(dateFormat.parse(selectedDate));
            selectedDateCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            int dayOfWeek = selectedDateCalendar.get(Calendar.DAY_OF_WEEK);
            isHoliday = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

            hoursList = isHoliday ? holidayHoursList : weekdayHoursList;

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            selectedCalendar.set(Calendar.MINUTE, timePicker.getMinute());

            for (Map<String, String> timeRange : hoursList) {
                String startTime = timeRange.get("開始");
                String endTime = timeRange.get("結束");

                Calendar startCalendar = Calendar.getInstance();
                Calendar endCalendar = Calendar.getInstance();

                startCalendar.setTime(timeFormat.parse(startTime));
                endCalendar.setTime(timeFormat.parse(endTime));

                startCalendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR));
                startCalendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH));
                startCalendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH));

                endCalendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR));
                endCalendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH));
                endCalendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH));

                if (!selectedCalendar.before(startCalendar) && !selectedCalendar.after(endCalendar)) {
                    return true;
                }
            }
        } catch (ParseException e) {
            Log.e("paycheck", "時間格式解析錯誤", e);
        }
        return false;
    }

    // 其餘代碼保持不變，例如 `fetchUserNameFromFirebase`、`checkBalanceAndSubmitOrder` 等方法



    private String formatTime(int hourOfDay, int minute) {
        String period = (hourOfDay >= 12) ? "PM" : "AM";
        int hour = (hourOfDay > 12) ? hourOfDay - 12 : hourOfDay;
        if (hour == 0) hour = 12;
        return String.format("%02d:%02d %s", hour, minute, period);
    }

    private void fetchUserNameFromFirebase(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("name");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.getValue(String.class);
                Log.d("paycheck", "Fetched userName from Firebase: " + userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("paycheck", "Failed to fetch userName from Firebase", databaseError.toException());
            }
        });
    }

    // 其餘的程式碼維持不變，包括方法 `checkBalanceAndSubmitOrder()`、`loadCartFromFirebase()`、`prepareDayItems()` 等
    // 注意這些方法的邏輯並未更動，只是省略不再顯示。




    private void checkBalanceAndSubmitOrder() {
        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid).child("wallet");
        walletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer balance = snapshot.getValue(Integer.class);
                if (balance != null && balance >= totalPrice) {
                    // 餘額足夠，送出訂單
                    uploadDataToFirebase();
                } else {
                    // 餘額不足，顯示提示訊息
                    Toast.makeText(paycheck.this, "餘額不足，請前往儲值", Toast.LENGTH_SHORT).show();
                    // 在此可以增加一個按鈕，導向儲值頁面
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("paycheck", "Failed to check balance", error.toException());
            }
        });
    }
    private void loadCartFromFirebase() {
        DatabaseReference cartReference = databaseReference.child("Users").child(userUid).child("cart");
        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItems.clear();
                totalPrice = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    int quantity = snapshot.child("counter").getValue(Integer.class);
                    boolean isClosed = snapshot.child("isClosed").getValue(Boolean.class);
                    String itemRestaurantName = snapshot.child("restaurantName").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    int calories = snapshot.child("calories").getValue(Integer.class) != null ? snapshot.child("calories").getValue(Integer.class) : 0;
                    int sugar = snapshot.child("sugar").getValue(Integer.class) != null ? snapshot.child("sugar").getValue(Integer.class) : 0;
                    int protein = snapshot.child("protein").getValue(Integer.class) != null ? snapshot.child("protein").getValue(Integer.class) : 0;
                    int carbohydrates = snapshot.child("carbohydrates").getValue(Integer.class) != null ? snapshot.child("carbohydrates").getValue(Integer.class) : 0;
                    int fat = snapshot.child("fat").getValue(Integer.class) != null ? snapshot.child("fat").getValue(Integer.class) : 0;

                    int price = 0;
                    if (description != null) {
                        try {
                            price = Integer.parseInt(description.replaceAll("[^\\d]", ""));
                        } catch (NumberFormatException e) {
                            Log.e("paycheck", "Error parsing price from description: " + description, e);
                        }
                    }

                    com.example.a20240416restart.ShoppingItem item = new com.example.a20240416restart.ShoppingItem(
                            R.drawable.ic_launcher_foreground,
                            title,
                            description,
                            isClosed,
                            quantity,
                            itemRestaurantName,
                            imageUrl,
                            calories,
                            sugar,
                            protein,
                            carbohydrates,
                            fat
                    );
                    cartItems.add(item);

                    totalPrice += price * quantity;
                }

                // 使用禁用編輯選項的 adapter
                shoppingListAdapter = new com.example.a20240416restart.ShoppingListAdapter(paycheck.this, cartItems, new com.example.a20240416restart.ShoppingListAdapter.OnItemActionListener() {
                    @Override
                    public void onEdit(int position) {
                        // 不執行編輯操作，結帳頁面禁用編輯功能
                    }

                    @Override
                    public void onDelete(int position) {
                        cartItems.remove(position);
                        shoppingListAdapter.notifyDataSetChanged();
                        totalPrice = calculateTotalPrice();
                        updateTotalPriceDisplay();
                    }
                }, false); // 傳入 false 以禁用編輯功能

                listView.setAdapter(shoppingListAdapter);
                updateTotalPriceDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(paycheck.this, "無法加載購物車數據。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<com.example.a20240416restart.DayItem> prepareDayItems() {
        List<com.example.a20240416restart.DayItem> dayItems = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        dayItems.add(new com.example.a20240416restart.DayItem("今天", dateFormat.format(calendar.getTime())));
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dayItems.add(new com.example.a20240416restart.DayItem("明天", dateFormat.format(calendar.getTime())));
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dayItems.add(new com.example.a20240416restart.DayItem("後天", dateFormat.format(calendar.getTime())));
        return dayItems;
    }

    private int calculateTotalPrice() {
        int total = 0;
        for (com.example.a20240416restart.ShoppingItem item : cartItems) {
            int price = Integer.parseInt(item.getDescription().replaceAll("[^\\d]", ""));
            total += price * item.getQuantity();
        }
        return total;
    }

    private void uploadDataToFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Orders");
        String orderId = databaseReference.push().getKey();

        Log.d("paycheck", "Selected date: " + selectedDate);
        Log.d("paycheck", "Selected time: " + selectedTime);

        long timestamp = getTimestampFromDateTime();
        long uploadTimestamp = System.currentTimeMillis();

        long differenceInMillis = timestamp - uploadTimestamp;
        long differenceInMinutes = differenceInMillis / (1000 * 60);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userUid", userUid);
        orderData.put("timestamp", timestamp);
        orderData.put("uploadTimestamp", uploadTimestamp);
        orderData.put("differenceInMinutes", differenceInMinutes);
        orderData.put("totalPrice", totalPrice);
        orderData.put("名字", userName);
        orderData.put("restaurantName", restaurantName);
        orderData.put("接單狀況", "未接單");

        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (com.example.a20240416restart.ShoppingItem item : cartItems) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("title", item.getTitle());
            itemData.put("description", item.getDescription().replaceAll("[^\\d]", ""));
            itemData.put("quantity", item.getQuantity());
            itemData.put("calories", item.getCalories());
            itemData.put("sugar", item.getSugar());
            itemData.put("protein", item.getProtein());
            itemData.put("carbohydrates", item.getCarbohydrates());
            itemData.put("fat", item.getFat());
            itemData.put("restaurantName", restaurantName);
            itemsData.add(itemData);
        }
        orderData.put("items", itemsData);

        if (orderId != null) {
            databaseReference.child(orderId).setValue(orderData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("paycheck", "Order uploaded successfully: " + orderData);

                        clearCartDataInFirebase();
                        monitorOrderStatus(orderId);
                        Intent intent = new Intent(paycheck.this, MainActivity2.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Log.e("paycheck", "Failed to upload order", e));
        }
    }

    private void monitorOrderStatus(String orderId) {
        DatabaseReference orderStatusRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId).child("接單狀況");

        orderStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String orderStatus = dataSnapshot.getValue(String.class);
                if (orderStatus != null && orderStatus.equals("接受訂單")) {
                    // 店家已接單，扣除金額
                    deductBalance();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("paycheck", "Failed to monitor order status", databaseError.toException());
            }
        });
    }
    private void deductBalance() {
        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid).child("money");
        walletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer balance = snapshot.getValue(Integer.class);

                // 加入除錯訊息，檢查 balance 是否正確取得
                Log.d("deductBalance", "Current balance: " + balance);

                if (balance != null) {
                    int newBalance = balance - totalPrice;

                    // 檢查新計算的餘額是否正確
                    Log.d("deductBalance", "New balance after deduction: " + newBalance);

                    walletRef.setValue(newBalance).addOnSuccessListener(aVoid ->
                            Toast.makeText(paycheck.this, "扣款成功", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(paycheck.this, "扣款失敗", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Log.e("deductBalance", "Balance is null, unable to proceed with deduction");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("paycheck", "Failed to deduct balance", error.toException());
            }
        });
    }





    public void onclick_back_icon(View v) {
        finish();
    }

    private long getTimestampFromDateTime() {
        try {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(dateFormat.parse(selectedDate));

            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));

            String[] timeParts = selectedTime.split(" ");
            String[] hourMinute = timeParts[0].split(":");
            int hour = Integer.parseInt(hourMinute[0]);
            int minute = Integer.parseInt(hourMinute[1]);

            if (timeParts[1].equalsIgnoreCase("PM") && hour < 12) {
                hour += 12;
            }
            if (timeParts[1].equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTimeInMillis();
        } catch (Exception e) {
            Log.e("paycheck", "解析日期時間時出錯: ", e);
            return 0;
        }
    }

    private void clearCartDataInFirebase() {
        databaseReference.child("Users").child(userUid).child("cart").removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(paycheck.this, "訂單已提交，購物車已清空", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(paycheck.this, "清空購物車失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotalPriceDisplay() {
        TextView totalTextView = findViewById(R.id.total);
        totalTextView.setText("小計: " + totalPrice + "元");
    }

}