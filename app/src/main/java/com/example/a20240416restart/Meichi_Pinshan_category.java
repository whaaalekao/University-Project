package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Meichi_Pinshan_category extends AppCompatActivity implements Meichi_category_Adapter.OnItemClickListener {

    private static final String TAG = "Meichi_Pinshan_category";
    private RecyclerView rc_maeichi;
    private Meichi_category_Adapter adapter;
    private List<CategoryItem> categoryItemList;
    private String userUid;  // 用於區分不同使用者

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meichi_pinshan_category);

        rc_maeichi = findViewById(R.id.rc_meichi);
        rc_maeichi.setLayoutManager(new LinearLayoutManager(this));

        categoryItemList = new ArrayList<>();
        adapter = new Meichi_category_Adapter(categoryItemList, this);
        rc_maeichi.setAdapter(adapter);

        String selectedSchool = getIntent().getStringExtra("selectedSchool");
        String selectedRestaurant = getIntent().getStringExtra("selectedRestaurant");
        userUid = getIntent().getStringExtra("uid");  // 獲取使用者 UID

        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("校區").child(selectedSchool).child(selectedRestaurant);
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryItemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String title = dataSnapshot.getKey();
                    String imageUrl = dataSnapshot.child("圖片").child("種類").getValue(String.class);

                    categoryItemList.add(new CategoryItem(title, imageUrl));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Meichi_Pinshan_category.this, "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(CategoryItem item, int position) {
        Intent intent = new Intent(this, Meichi_Pinshan_food.class);
        String selectedSchool = getIntent().getStringExtra("selectedSchool");
        String selectedRestaurant = getIntent().getStringExtra("selectedRestaurant");
        String name = getIntent().getStringExtra("name");
        intent.putExtra("selectedSchool", selectedSchool);
        intent.putExtra("selectedRestaurant", selectedRestaurant);
        intent.putExtra("selectedCategory", item.getTitle());
        intent.putExtra("name", name);
        intent.putExtra("uid", userUid);  // 傳遞 UID 給下一個活動
        startActivity(intent);
    }

    public void click(View view) {
        Intent intent = new Intent(this, sopping_car.class);
        intent.putExtra("uid", userUid);  // 傳遞 UID 到 sopping_car 活動
        startActivity(intent);
    }
}