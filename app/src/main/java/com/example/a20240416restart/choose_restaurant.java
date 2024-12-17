package com.example.a20240416restart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class choose_restaurant extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView lv;
    private ArrayAdapter<String> adapter;
    private List<String> restaurantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_restaurant);

        lv = findViewById(R.id.ll);
        lv.setOnItemClickListener(this);

        // 初始化餐廳列表
        restaurantList = new ArrayList<>();

        // 獲取選中的校區
        String selectedSchool = getIntent().getStringExtra("selectedSchool");



        // 動態從 Firebase 加載餐廳數據
        DatabaseReference schoolRef = FirebaseDatabase.getInstance().getReference().child("校區").child(selectedSchool);
        schoolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                    String restaurantName = restaurantSnapshot.getKey();
                    restaurantList.add(restaurantName);
                }
                // 設置適配器
                adapter = new ArrayAdapter<>(choose_restaurant.this, android.R.layout.simple_list_item_1, restaurantList);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理錯誤
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedRestaurant = restaurantList.get(position);
        String selectedSchool = getIntent().getStringExtra("selectedSchool");

        Log.d("choose_restaurant", "Selected School: " + selectedSchool);
        Log.d("choose_restaurant", "Selected Restaurant: " + selectedRestaurant);
        String name = getIntent().getStringExtra("name");

        Intent intent = new Intent(this, Meichi_Pinshan_category.class);
        intent.putExtra("selectedSchool", selectedSchool);
        intent.putExtra("selectedRestaurant", selectedRestaurant);
        intent.putExtra("name",name);
        startActivity(intent);
    }
}
