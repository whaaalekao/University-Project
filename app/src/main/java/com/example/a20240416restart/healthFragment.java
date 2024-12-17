package com.example.a20240416restart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class healthFragment extends Fragment {

    // 生命週期：此Fragment的視圖
    private TextView textViewCalories, textViewCarbohydrates, textViewFat, textViewProtein;
    private TextView textViewCaloriesPercentage,textViewCarbPercentage,textViewFatPercentage,textViewPortPercentage;
    private TextView textViewCaloriesPr,textViewCarbohydratesPr,textViewFatPr,textViewProteinPr;
    private TextView textViewSelectedDate, textViewAge, textViewGender, textViewHeight, textViewWeight, textViewBmr;
    private TextView kal_textview, cal_textview, fat_textview, pro_textview;
    private Button btnSelectDate; // 用來選擇日期的按鈕
    private int totalCalories = 0, totalCarbohydrates = 0, totalFat = 0, totalProtein = 0; // 累計的營養數據
    private boolean isPersonalInfoFilled = false; // 判斷是否有填寫個人資料
    private ProgressBar progressBarCalories, progressBarCarbohydrates, progressBarFat, progressBarProtein;


    // 空的構造函數
    public healthFragment() {
        // Required empty public constructor
    }

    // Fragment的視圖被創建時調用
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 將XML文件轉換成View對象
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        // 初始化所有需要的視圖
        textViewCaloriesPr = view.findViewById(R.id.textViewCaloriesPr);
        textViewCarbohydratesPr = view.findViewById(R.id.textViewCarbohydratesPr);
        textViewFatPr = view.findViewById(R.id.textViewFatPr);
        textViewProteinPr = view.findViewById(R.id.textViewProteinPr);

        textViewCalories = view.findViewById(R.id.textViewCalories);
        textViewCarbohydrates = view.findViewById(R.id.textViewCarbohydrates);
        textViewFat = view.findViewById(R.id.textViewFat);
        textViewProtein = view.findViewById(R.id.textViewProtein);
        textViewSelectedDate = view.findViewById(R.id.textViewSelectedDate);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        textViewAge = view.findViewById(R.id.textViewAge);
        textViewGender = view.findViewById(R.id.textViewGender);
        textViewHeight = view.findViewById(R.id.textViewHeight);
        textViewWeight = view.findViewById(R.id.textViewWeight);
        textViewBmr = view.findViewById(R.id.textViewBmr);
        kal_textview = view.findViewById(R.id.kal_textview);
        cal_textview = view.findViewById(R.id.cal_textview);
        fat_textview = view.findViewById(R.id.fat_textview);
        pro_textview = view.findViewById(R.id.pro_textview);
        progressBarCalories = view.findViewById(R.id.progressBarCalories);
        progressBarCarbohydrates = view.findViewById(R.id.progressBarCarbohydrates);
        progressBarFat = view.findViewById(R.id.progressBarFat);
        progressBarProtein = view.findViewById(R.id.progressBarProtein);
        textViewCaloriesPercentage = view.findViewById(R.id.textViewCaloriesPercentage);
        textViewCarbPercentage = view.findViewById(R.id.textViewCarbPercentage);
        textViewFatPercentage = view.findViewById(R.id.textViewFatPercentage);
        textViewPortPercentage = view.findViewById(R.id.textViewPortPercentage);

        // 初始化每日需求的 TextView，這裡設置了預設值
        kal_textview.setText("熱量：2000大卡");
        cal_textview.setText("碳水化合物：300 克");
        fat_textview.setText("脂肪：70克");
        pro_textview.setText("蛋白質：50克");

        // 顯示當天的日期，並將其顯示在TextView中
        String todayDate = getTodayDate();
        textViewSelectedDate.setText("日期: " + todayDate);
        loadAndAccumulateNutritionInfo(todayDate); // 預設加載當天的營養信息

        // 設置日期選擇按鈕的點擊事件
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // 獲取當前用戶，並檢查是否已登入
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // 獲取UID
            fetchPersonalInfoFromFirebase(uid); // 獲取個人資料
            fetchPersonalInfoAndCalculateBmr(uid); // 獲取並計算BMR和TDEE
        } else {
            // 未登入情況下顯示Toast提示
            Toast.makeText(getContext(), "用戶未登入", Toast.LENGTH_SHORT).show();
        }

        return view; // 返回創建的視圖
    }

    // 從Firebase中根據UID獲取個人資料
    private void fetchPersonalInfoFromFirebase(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("personal_info");

        // 添加一次性事件監聽器來獲取數據
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isPersonalInfoFilled = true;

                    // 從Firebase中提取個人資料並顯示在TextView中
                    Integer age = getIntFromSnapshot(dataSnapshot.child("age"));
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    Integer height = getIntFromSnapshot(dataSnapshot.child("height"));
                    Integer weight = getIntFromSnapshot(dataSnapshot.child("weight"));

                    // 設置提取到的個人資料
                    textViewAge.setText("年齡: " + (age != null ? age : "未提供"));
                    textViewGender.setText("性別: " + (gender != null ? gender : "未提供"));
                    textViewHeight.setText("身高: " + (height != null ? height + " cm" : "未提供"));
                    textViewWeight.setText("體重: " + (weight != null ? weight + " kg" : "未提供"));
                } else {
                    // 如果個人資料不存在，顯示提示框
                    isPersonalInfoFilled = false;
                    showNoPersonalInfoAlert();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "數據讀取失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 根據UID獲取個人資料並計算BMR和TDEE
    private void fetchPersonalInfoAndCalculateBmr(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("personal_info");

        userRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer age = getIntFromSnapshot(dataSnapshot.child("age"));
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    Integer height = getIntFromSnapshot(dataSnapshot.child("height"));
                    Integer weight = getIntFromSnapshot(dataSnapshot.child("weight"));
                    Integer goal = getIntFromSnapshot(dataSnapshot.child("goal"));
                    Double exercise_intensity = getDoubleFromSnapshot(dataSnapshot.child("exercise intensity"));

                    // 確認資料完整
                    if (age != null && gender != null && height != null && weight != null && goal != null && exercise_intensity != null) {
                        int genderValue = gender.equals("男") ? 1 : 0; // 男性為1，女性為0
                        double bmr = (9.99 * weight) + (6.25 * height) - (4.92 * age) + (166 * genderValue) - 161; // BMR公式
                        double TDEE = bmr * exercise_intensity + goal; // TDEE公式

                        // 顯示計算結果到UI
                        textViewAge.setText("年齡: " + age);
                        textViewGender.setText("性別: " + gender);
                        textViewHeight.setText("身高: " + height + " cm");
                        textViewWeight.setText("體重: " + weight + " kg");
                        kal_textview.setText("熱量：" + Math.round(TDEE) + "大卡");
                        fat_textview.setText("脂肪：" + Math.round(weight) + "克");
                        pro_textview.setText("蛋白質：" + Math.round(weight * 1.5) + "克");
                        cal_textview.setText("碳水化合物：" + Math.round((TDEE - (weight * 9 + weight * 2 * 4)) / 4) + " 克");
                        textViewBmr.setText("每日總攝取量: " + Math.round(TDEE) + "大卡");
                    } else {
                        Toast.makeText(getContext(), "無法獲取完整的個人資料", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "無法找到個人資料", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "數據讀取失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 顯示當個人資料缺失時的提示框
    private void showNoPersonalInfoAlert() {
        if (!isPersonalInfoFilled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("個人資料未填寫");
            builder.setMessage("您尚未填寫個人資料，請前往填寫。");
            builder.setPositiveButton("填寫資料", (dialog, which) -> {
                Intent intent = new Intent(getContext(), personal_imformation.class);
                startActivity(intent);
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        }
    }

    // 獲取當天日期的字符串格式
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // 顯示日期選擇器
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            String selectedDate = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            textViewSelectedDate.setText("日期: " + selectedDate);
            loadAndAccumulateNutritionInfo(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    // 加載並累加特定日期的營養信息
    private void loadAndAccumulateNutritionInfo(String selectedDate) {
        totalCalories = 0;
        totalCarbohydrates = 0;
        totalFat = 0;
        totalProtein = 0;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();

            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean hasOrders = false;

                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);
                        String userUid = orderSnapshot.child("userUid").getValue(String.class);
                        String orderStatus = orderSnapshot.child("接單狀況").getValue(String.class);
                        if (timestamp == null || userUid == null || orderStatus == null) continue;

                        String orderDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));

                        if (orderDate.equals(selectedDate) && userUid.equals(currentUserUid) && "完成訂單".equals(orderStatus)) {
                            hasOrders = true;
                            for (DataSnapshot itemSnapshot : orderSnapshot.child("items").getChildren()) {
                                Integer quantity = getIntFromSnapshot(itemSnapshot.child("quantity"));
                                Integer calories = getIntFromSnapshot(itemSnapshot.child("calories"));
                                Integer carbohydrates = getIntFromSnapshot(itemSnapshot.child("carbohydrates"));
                                Integer fat = getIntFromSnapshot(itemSnapshot.child("fat"));
                                Integer protein = getIntFromSnapshot(itemSnapshot.child("protein"));

                                if (calories != null && quantity != null) totalCalories += calories * quantity;
                                if (carbohydrates != null && quantity != null) totalCarbohydrates += carbohydrates * quantity;
                                if (fat != null && quantity != null) totalFat += fat * quantity;
                                if (protein != null && quantity != null) totalProtein += protein * quantity;
                            }
                        }
                    }

                    if (!hasOrders) {
                        showNoOrdersAlert();
                    } else {
                        updateNutritionDisplay();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "數據加載失敗: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 顯示當無訂單時的提示框
    private void showNoOrdersAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("警告");
        builder.setMessage("未購物");
        builder.setPositiveButton("確定", null);
        builder.show();
    }

    // 更新營養數據的顯示，並且更新圖表顏色
    private void updateNutritionDisplay() {
        if (!isAdded()) return;

        // 檢查並設置每個TextView的顏色
        checkAndSetTextColor();

        // 添加Log來檢查各個營養數據的進度條設置
        int driCalories = extractNumber(kal_textview.getText().toString());
        int driCarbohydrates = extractNumber(cal_textview.getText().toString());
        int driFat = extractNumber(fat_textview.getText().toString());
        int driProtein = extractNumber(pro_textview.getText().toString());

        Log.d("NutritionDisplay", "DRI Values - Calories: " + driCalories + ", Carbohydrates: " + driCarbohydrates + ", Fat: " + driFat + ", Protein: " + driProtein);
        Log.d("NutritionDisplay", "Total Values - Calories: " + totalCalories + ", Carbohydrates: " + totalCarbohydrates + ", Fat: " + totalFat + ", Protein: " + totalProtein);

        // 計算每個營養成分的百分比並設置進度條
        int caloriesPercentage = driCalories > 0 ? Math.min((totalCalories * 100) / driCalories, 100) : 0;
        int carbohydratesPercentage = driCarbohydrates > 0 ? Math.min((totalCarbohydrates * 100) / driCarbohydrates, 100) : 0;
        int fatPercentage = driFat > 0 ? Math.min((totalFat * 100) / driFat, 100) : 0;
        int proteinPercentage = driProtein > 0 ? Math.min((totalProtein * 100) / driProtein, 100) : 0;

        Log.d("NutritionDisplay", "Calculated Percentages - Calories: " + caloriesPercentage + "%, Carbohydrates: " + carbohydratesPercentage + "%, Fat: " + fatPercentage + "%, Protein: " + proteinPercentage + "%");

        // 設置進度條的進度和顏色
        setProgressBar(progressBarCalories, caloriesPercentage, totalCalories > driCalories);
        setProgressBar(progressBarCarbohydrates, carbohydratesPercentage, totalCarbohydrates > driCarbohydrates);
        setProgressBar(progressBarFat, fatPercentage, totalFat > driFat);
        setProgressBar(progressBarProtein, proteinPercentage, totalProtein > driProtein);

        // 更新百分比顯示的 TextView
        textViewCaloriesPercentage.setText(caloriesPercentage + "%");
        textViewCarbPercentage.setText(carbohydratesPercentage + "%");
        textViewFatPercentage.setText(fatPercentage + "%");
        textViewPortPercentage.setText(proteinPercentage + "%");
    }

    private void setProgressBar(ProgressBar progressBar, int progress, boolean isOverStandard) {
        progressBar.setMax(100); // 設置最大值為100
        progressBar.setProgress(progress); // 設置當前進度

        if (isOverStandard) {
            // 當進度超過標準值時，整條變紅色
            progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            // 當未達到標準值時，已達到的部分為綠色，未達到的部分為灰色
            progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#4FA772"), android.graphics.PorterDuff.Mode.SRC_IN);

            // 設置未達到部分為灰色
            progressBar.getProgressDrawable().mutate().setTintMode(android.graphics.PorterDuff.Mode.MULTIPLY);
            progressBar.getProgressDrawable().mutate().setTint(Color.LTGRAY);
        }
    }

    // 檢查每個營養數據是否超過標準值並設置顏色
    private void checkAndSetTextColor() {
        int driCalories = extractNumber(kal_textview.getText().toString());
        int driCarbohydrates = extractNumber(cal_textview.getText().toString());
        int driFat = extractNumber(fat_textview.getText().toString());
        int driProtein = extractNumber(pro_textview.getText().toString());

        Log.d("CheckTextColor", "Dri Values - Calories: " + driCalories + ", Carbohydrates: " + driCarbohydrates + ", Fat: " + driFat + ", Protein: " + driProtein);
        Log.d("CheckTextColor", "Total Values - Calories: " + totalCalories + ", Carbohydrates: " + totalCarbohydrates + ", Fat: " + totalFat + ", Protein: " + totalProtein);

        setTextColor(textViewCalories, "熱量: " + totalCalories + " kcal", totalCalories, driCalories);
        setTextColor(textViewCarbohydrates, "碳水化合物: " + totalCarbohydrates + " g", totalCarbohydrates, driCarbohydrates);
        setTextColor(textViewFat, "脂肪: " + totalFat + " g", totalFat, driFat);
        setTextColor(textViewProtein, "蛋白質: " + totalProtein + " g", totalProtein, driProtein);
        setTextColor(textViewCaloriesPr, "熱量: " + totalCalories + " kcal", totalCalories, driCalories);
        setTextColor(textViewCarbohydratesPr, "碳水化合物: " + totalCarbohydrates + " g", totalCarbohydrates, driCarbohydrates);
        setTextColor(textViewFatPr, "脂肪: " + totalFat + " g", totalFat, driFat);
        setTextColor(textViewProteinPr, "蛋白質: " + totalProtein + " g", totalProtein, driProtein);
    }

    // 設置TextView顏色的方法
    private void setTextColor(TextView textView, String text, int value, int standardValue) {
        SpannableString spannableString = new SpannableString(text);
        int color = (value > standardValue) ? Color.RED : Color.parseColor("#4FA772");

        Log.d("SetTextColor", "Setting color for " + textView.getId() + ": value = " + value + ", standard = " + standardValue + ", color = " + (color == Color.RED ? "RED" : "GREEN"));

        String valueString = String.valueOf(value);
        int start = text.indexOf(valueString);
        if (start != -1) {
            int end = start + valueString.length();
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
    }

    // 從字符串中提取數字
    private int extractNumber(String text) {
        String number = text.replaceAll("[^0-9]", "");
        try {
            int result = number.isEmpty() ? 0 : Integer.parseInt(number);
            Log.d("ExtractNumber", "Parsed number: " + result + " from text: " + text);
            return result;
        } catch (NumberFormatException e) {
            Log.e("ExtractNumberError", "無法解析數字: " + text, e);
            return 0;
        }
    }

    // 安全地從DataSnapshot中獲取Integer
    private Integer getIntFromSnapshot(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    // 安全地從DataSnapshot中獲取Double
    private Double getDoubleFromSnapshot(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Double) {
            return (Double) value;
        }
        return null;
    }
}