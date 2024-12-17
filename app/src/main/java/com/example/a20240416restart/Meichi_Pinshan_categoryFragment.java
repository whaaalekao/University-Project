package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Meichi_Pinshan_categoryFragment extends Fragment implements Meichi_category_Adapter.OnItemClickListener {

    private RecyclerView rc_meichi;
    private Meichi_category_Adapter adapter;
    private List<CategoryItem> categoryItemList;
    private List<CategoryItem> filteredCategoryItemList; // 用於過濾後的列表
    private ImageView shoppingIcon;
    private SearchView searchView;

    public Meichi_Pinshan_categoryFragment() {
        // 必需的空的構造函數
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meichi__pinshan_category, container, false);

        // 初始化 RecyclerView
        rc_meichi = view.findViewById(R.id.rc_meichi);
        rc_meichi.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryItemList = new ArrayList<>();
        filteredCategoryItemList = new ArrayList<>(categoryItemList); // 初始化過濾後列表
        adapter = new Meichi_category_Adapter(filteredCategoryItemList, this); // 將過濾後的列表傳給適配器
        rc_meichi.setAdapter(adapter);

        // 初始化 SearchView
        searchView = view.findViewById(R.id.searchView);
        setupSearchView(); // 設置搜尋過濾功能

        shoppingIcon = view.findViewById(R.id.shoppin_icon);
        shoppingIcon.setOnClickListener(this::click);

        String selectedSchool = getArguments().getString("selectedSchool");
        String selectedRestaurant = getArguments().getString("selectedRestaurant");

        // 讀取類別名稱和圖片 URL
        loadCategoriesFromFirebase(selectedSchool, selectedRestaurant);

        return view;
    }

    // 搜尋過濾邏輯
    private void setupSearchView() {
        // 確保點擊整個 SearchView 都會啟用搜尋，而不只是放大鏡圖示
        searchView.setIconifiedByDefault(false);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false); // 展開搜尋欄
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText); // 過濾數據
                return true;
            }
        });
    }

    // 過濾數據
    private void filterData(String query) {
        filteredCategoryItemList.clear(); // 清空過濾列表
        if (query.isEmpty()) {
            filteredCategoryItemList.addAll(categoryItemList); // 如果搜尋欄是空的，顯示所有項目
        } else {
            for (CategoryItem item : categoryItemList) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredCategoryItemList.add(item); // 添加符合條件的項目
                }
            }
        }
        adapter.notifyDataSetChanged(); // 通知適配器數據變更
    }

    // 從 Firebase 根據類別名稱動態載入圖片
    private void loadCategoriesFromFirebase(String selectedSchool, String selectedRestaurant) {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("校區").child(selectedSchool).child(selectedRestaurant).child("食物");
        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference().child("校區").child(selectedSchool).child(selectedRestaurant).child("圖片");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryItemList.clear();
                filteredCategoryItemList.clear(); // 確保過濾後列表同步更新
                int totalItems = (int) snapshot.getChildrenCount();
                final int[] loadedItems = {0};

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String title = dataSnapshot.getKey();  // 獲取類別名稱

                    // 構建圖片的鍵，例如 "乳酪燒餅圖片"
                    String imageKey = title + "圖片";

                    imageRef.child(imageKey.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot imageSnapshot) {
                            String imageUrl = null;
                            if (imageSnapshot.exists()) {
                                imageUrl = imageSnapshot.getValue(String.class);  // 獲取圖片 URL
                            }

                            // 將圖片 URL 和類別名稱添加到列表
                            CategoryItem item = new CategoryItem(title, imageUrl != null ? imageUrl : null);
                            categoryItemList.add(item);
                            filteredCategoryItemList.add(item); // 添加到過濾列表

                            loadedItems[0]++;
                            if (loadedItems[0] == totalItems) {
                                adapter.notifyDataSetChanged(); // 更新 RecyclerView 顯示
                                runListViewAnimation();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error loading image data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading category data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(CategoryItem item, int position) {
        Meichi_Pinshan_foodFragment foodFragment = new Meichi_Pinshan_foodFragment();
        Bundle bundle = new Bundle();
        bundle.putString("selectedSchool", getArguments().getString("selectedSchool"));
        bundle.putString("selectedRestaurant", getArguments().getString("selectedRestaurant"));
        bundle.putString("selectedCategory", item.getTitle());
        bundle.putString("name", getArguments().getString("name"));
        bundle.putString("categoryImageUrl", item.getImageUrl()); // 傳遞圖片 URL 給下一個 Fragment
        foodFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, foodFragment)
                .addToBackStack(null)
                .commit();
    }

    private void runListViewAnimation() {
        rc_meichi.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                rc_meichi.getViewTreeObserver().removeOnPreDrawListener(this);  // 確保只監聽一次繪製事件

                int recyclerViewWidth = rc_meichi.getWidth(); // 獲取 RecyclerView 寬度
                if (rc_meichi.getChildCount() > 0) {
                    // 遍歷 RecyclerView 的每個子視圖，應用動畫效果
                    for (int i = 0; i < rc_meichi.getChildCount(); i++) {
                        View item = rc_meichi.getChildAt(i);  // 獲取每個 RecyclerView 的子項

                        // 設置初始位置在屏幕右側外
                        item.setTranslationX(recyclerViewWidth);
                        item.setAlpha(0f);  // 設置初始透明度為 0

                        // 延遲動畫的開始，讓每個項目有不同的延遲時間
                        item.animate()
                                .translationX(0)  // 從右側滑動到初始位置
                                .alpha(1f)  // 漸變到完全不透明
                                .setStartDelay(i * 50)  // 每個子項的動畫延遲 50 毫秒
                                .setDuration(300)  // 動畫持續時間 300 毫秒
                                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())  // 使用加速減速插值器
                                .start();  // 開始動畫
                    }
                }
                return true;
            }
        });
    }

    public void click(View view) {
        Intent intent = new Intent(getActivity(), sopping_car.class);
        startActivity(intent);
    }
}