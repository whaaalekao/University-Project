package com.example.a20240416restart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class Meichi_Pinshan_food extends AppCompatActivity {
    private static final String TAG = "Meichi_Pinshan_food";
    ListView lv_meichi_food;
    ListAdapter listAdapter;
    ArrayList<ListItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meichi_pinshan_food);

        lv_meichi_food = findViewById(R.id.lv_meichi_food);

        Intent it = getIntent();
        String selectedSchool = it.getStringExtra("selectedSchool");
        String selectedRestaurant = it.getStringExtra("selectedRestaurant");
        String selectedCategory = it.getStringExtra("selectedCategory");
        String name = getIntent().getStringExtra("name");

        list = new ArrayList<>();
        listAdapter = new ListAdapter(this, list);
        lv_meichi_food.setAdapter(listAdapter);

        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference()
                .child("校區").child(selectedSchool).child(selectedRestaurant).child(selectedCategory);
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String title = dataSnapshot.getKey();
                    String imageUrl = dataSnapshot.child("照片").getValue(String.class);
                    Object value = dataSnapshot.child("價格").getValue();
                    boolean isClosed = dataSnapshot.child("Closed").getValue(Boolean.class) != null && dataSnapshot.child("Closed").getValue(Boolean.class);

                    String description;
                    if (value instanceof Long) {
                        description = String.valueOf(value);
                    } else if (value instanceof Integer) {
                        description = String.valueOf(value);
                    } else if (value instanceof String) {
                        description = (String) value;
                    } else {
                        description = "Unknown type";
                    }

                    list.add(new ListItem(imageUrl, title, description, isClosed));
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read menu items.", error.toException());
            }
        });

        lv_meichi_food.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem selectedItem = list.get(position);

                if (selectedItem.isClosed()) {
                    new AlertDialog.Builder(Meichi_Pinshan_food.this)
                            .setTitle("項目已關閉")
                            .setMessage("店家已關閉此品項")
                            .setPositiveButton("確定", null)
                            .show();
                    return;
                }

                Intent intent = new Intent(Meichi_Pinshan_food.this, addtocart.class);
                intent.putExtra("category", selectedCategory);
                intent.putExtra("imageUrl", selectedItem.getImageUrl());
                intent.putExtra("title", selectedItem.getTitle());
                intent.putExtra("description", selectedItem.getDescription());
                intent.putExtra("selectedSchool",selectedSchool);
                intent.putExtra("selectedRestaurant",selectedRestaurant);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });
    }
}
