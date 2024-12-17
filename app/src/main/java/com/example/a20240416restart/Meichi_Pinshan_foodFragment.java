package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class Meichi_Pinshan_foodFragment extends Fragment {
    private static final String TAG = "Meichi_Pinshan_food";
    private ListView lv_meichi_food;
    private ListAdapter listAdapter;
    private ArrayList<ListItem> list;
    private String selectedSchool, selectedRestaurant, selectedCategory, name;
    private ImageView categoryImageView; // 用來顯示類別圖片的 ImageView

    public Meichi_Pinshan_foodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedSchool = getArguments().getString("selectedSchool");
            selectedRestaurant = getArguments().getString("selectedRestaurant");
            selectedCategory = getArguments().getString("selectedCategory");
            name = getArguments().getString("name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 正確引用佈局文件
        View view = inflater.inflate(R.layout.fragment_meichi__pinshan_food_, container, false);

        lv_meichi_food = view.findViewById(R.id.lv_meichi_food);  // 初始化 ListView
        categoryImageView = view.findViewById(R.id.meichi_pinshan_food_img); // 初始化類別圖片的 ImageView

        // 接收傳遞過來的圖片 URL 並顯示
        String categoryImageUrl = getArguments().getString("categoryImageUrl");
        if (categoryImageUrl != null && !categoryImageUrl.isEmpty()) {
            Picasso.get().load(categoryImageUrl).into(categoryImageView); // 使用 Picasso 加載圖片
        }

        // 初始化 List 和 Adapter
        list = new ArrayList<>();
        listAdapter = new ListAdapter(getActivity(), list);
        lv_meichi_food.setAdapter(listAdapter);

        // 連接到 Firebase Database 並獲取數據
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference()
                .child("校區").child(selectedSchool).child(selectedRestaurant).child("食物").child(selectedCategory);
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();  // 清空舊數據
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String title = dataSnapshot.getKey();  // 獲取菜品名稱
                    String imageUrl = dataSnapshot.child("照片").getValue(String.class);  // 獲取照片 URL
                    Object value = dataSnapshot.child("價格").getValue();  // 獲取價格
                    boolean isClosed = dataSnapshot.child("Closed").getValue(Boolean.class) != null
                            && dataSnapshot.child("Closed").getValue(Boolean.class);  // 獲取是否關閉狀態

                    // 將價格轉換為字符串並加上 "元"
                    String description;
                    if (value instanceof Long) {
                        description = value + "元";
                    } else if (value instanceof Integer) {
                        description = value + "元";
                    } else if (value instanceof String) {
                        description = value + "元";
                    } else {
                        description = "Unknown type";
                    }

                    // 添加到列表中
                    list.add(new ListItem(imageUrl, title, description, isClosed));
                }
                listAdapter.notifyDataSetChanged();  // 通知 Adapter 數據已更改

                // 在數據加載完成後啟動動畫
                lv_meichi_food.post(new Runnable() {
                    @Override
                    public void run() {
                        runListViewAnimation();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read menu items.", error.toException());  // 錯誤處理
            }
        });

        // 設置 ListView 項目點擊事件
        lv_meichi_food.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem selectedItem = list.get(position);  // 獲取選中的項目

                // 如果菜品已關閉，顯示對話框
                if (selectedItem.isClosed()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("項目已關閉")
                            .setMessage("店家已關閉此品項")
                            .setPositiveButton("確定", null)
                            .show();
                    return;
                }

                // 啟動 addtocart 活動並傳遞數據
                Intent intent = new Intent(getActivity(), addtocart.class);
                intent.putExtra("category", selectedCategory);
                intent.putExtra("imageUrl", selectedItem.getImageUrl());
                intent.putExtra("title", selectedItem.getTitle());
                intent.putExtra("description", selectedItem.getDescription());
                intent.putExtra("selectedSchool", selectedSchool);
                intent.putExtra("selectedRestaurant", selectedRestaurant);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });

        return view;
    }

    // 獨立的 runListViewAnimation 方法
    private void runListViewAnimation() {
        lv_meichi_food.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                lv_meichi_food.getViewTreeObserver().removeOnPreDrawListener(this);  // 確保只監聽一次繪製事件

                // 遍歷 ListView 的每個子視圖，應用動畫效果
                for (int i = 0; i < lv_meichi_food.getChildCount(); i++) {
                    View item = lv_meichi_food.getChildAt(i);  // 獲取每個 ListView 的子項

                    // 只對初始透明度為 0 的子項進行動畫
                    if (item.getAlpha() == 0f) {
                        item.setTranslationX(lv_meichi_food.getWidth()); // 設置初始位置在屏幕右側外
                        item.setAlpha(0f);  // 設置初始透明度為 0

                        // 延遲動畫的開始，讓每個項目有不同的延遲時間
                        item.animate()
                                .translationX(0)  // 從右側滑動到初始位置
                                .alpha(1f)  // 漸變到完全不透明
                                .setStartDelay(i * 100)  // 每個子項的動畫延遲 100 毫秒
                                .setDuration(500)  // 動畫持續時間（500 毫秒）
                                .setInterpolator(new android.view.animation.DecelerateInterpolator())  // 使用減速插值器，讓動畫進入時更平滑
                                .start();  // 開始動畫
                    }
                }
                return true;
            }
        });
    }
}