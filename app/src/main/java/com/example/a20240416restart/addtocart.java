package com.example.a20240416restart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class addtocart extends AppCompatActivity {

    private int counter = 1;
    private TextView textCounter;
    private CheckBox addegg, addcorn, addham, addcheese;
    private int des;
    private String title, description, category, name, selectedRestaurant, selectedSchool;
    private int editPosition = -1;
    private String calories, sugar, protein, carbohydrates, fat;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String userUID;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtocart);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userUID = auth.getCurrentUser().getUid();

        Button buttonDecrement = findViewById(R.id.button_decrement);
        Button buttonIncrement = findViewById(R.id.button_increment);
        textCounter = findViewById(R.id.counter);
        addcorn = findViewById(R.id.addcorn);
        addegg = findViewById(R.id.addegg);
        addham = findViewById(R.id.addham);
        addcheese = findViewById(R.id.addrice);
        ImageView imageView = findViewById(R.id.Image);
        TextView titleTextView = findViewById(R.id.Title);

        Intent tt = getIntent();
        selectedRestaurant = tt.getStringExtra("selectedRestaurant");
        selectedSchool = tt.getStringExtra("selectedSchool");
        category = tt.getStringExtra("category");
        imageUrl = tt.getStringExtra("imageUrl");
        title = tt.getStringExtra("title");
        description = tt.getStringExtra("description");
        counter = tt.getIntExtra("quantity", 1);
        name = tt.getStringExtra("name");
        editPosition = tt.getIntExtra("position", -1);

        calories = String.valueOf(tt.getIntExtra("calories", 0));
        sugar = String.valueOf(tt.getIntExtra("sugar", 0));
        protein = String.valueOf(tt.getIntExtra("protein", 0));
        carbohydrates = String.valueOf(tt.getIntExtra("carbohydrates", 0));
        fat = String.valueOf(tt.getIntExtra("fat", 0));

        Log.d("addtocart", "Received title: " + title);
        Log.d("addtocart", "Received description: " + description);
        Log.d("addtocart", "Received quantity: " + counter);
        Log.d("addtocart", "Received calories: " + calories);
        Log.d("addtocart", "Received protein: " + protein);
        Log.d("addtocart", "Received restaurant: " + selectedRestaurant);
        Log.d("addtocart", "Received imageUrl: " + imageUrl);

        try {
            des = Integer.parseInt(description.replace("元", "").replace("$", "").trim());
        } catch (NumberFormatException e) {
            des = 0;
        }

        if (imageUrl != null) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_launcher_foreground).into(imageView);
        }

        titleTextView.setText(title + " " + description);
        updateButtonText(title, des, counter);

        if (editPosition != -1) {
            // 編輯模式，不需檢查 selectedSchool 和 category
            displayNutritionInfo();
            checkCurrentCartRestaurant();
        } else {
            // 非編輯模式才進行檢查
            if (selectedSchool == null || category == null) {
                Log.e("addtocart", "selectedSchool or category is null. Please check the data passed to this activity.");
                Toast.makeText(this, "無法加載數據，請確認選擇的內容是否正確。", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            loadDataFromFirebase(title, category, selectedSchool, selectedRestaurant);
        }

        buttonDecrement.setOnClickListener(v -> {
            if (counter > 1) {
                counter--;
                textCounter.setText(String.valueOf(counter));
                updateButtonText(title, des, counter);
            }
        });

        buttonIncrement.setOnClickListener(v -> {
            counter++;
            textCounter.setText(String.valueOf(counter));
            updateButtonText(title, des, counter);
        });

        addegg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            des += isChecked ? 10 : -10;
            updateButtonText(title, des, counter);
        });

        addcorn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            des += isChecked ? 5 : -5;
            updateButtonText(title, des, counter);
        });

        addham.setOnCheckedChangeListener((buttonView, isChecked) -> {
            des += isChecked ? 10 : -10;
            updateButtonText(title, des, counter);
        });

        addcheese.setOnCheckedChangeListener((buttonView, isChecked) -> {
            des += isChecked ? 10 : -10;
            updateButtonText(title, des, counter);
        });

        Button btAddToCart = findViewById(R.id.bt_addtocart);
        btAddToCart.setOnClickListener(v -> {
            checkAndAddToCart();
        });
    }

    private void displayNutritionInfo() {
        TextView caloriesText = findViewById(R.id.calories_text);
        TextView sugarText = findViewById(R.id.sugar_text);
        TextView proteinText = findViewById(R.id.protein_text);
        TextView carbohydratesText = findViewById(R.id.carbohydrates_text);
        TextView fatText = findViewById(R.id.text_fat);

        caloriesText.setText(calories + " kcal");
        sugarText.setText(sugar + " g");
        proteinText.setText(protein + " g");
        carbohydratesText.setText(carbohydrates + " g");
        fatText.setText(fat + " g");
    }

    private void checkCurrentCartRestaurant() {
        databaseReference.child("Users").child(userUID).child("cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean allSame = true;
                String commonRestaurantName = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String cartRestaurantName = snapshot.child("restaurantName").getValue(String.class);
                    if (cartRestaurantName == null) continue;

                    if (commonRestaurantName == null) {
                        commonRestaurantName = cartRestaurantName;
                    } else if (!cartRestaurantName.equals(commonRestaurantName)) {
                        allSame = false;
                        break;
                    }
                }

                if (!allSame) {
                    Toast.makeText(addtocart.this, "購物車中有不同餐廳的餐點。", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("addtocart", "所有餐點來自相同餐廳或購物車為空。");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addtocart.this, "無法檢查購物車。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndAddToCart() {
        databaseReference.child("Users").child(userUID).child("cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean allSame = true;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String cartRestaurantName = snapshot.child("restaurantName").getValue(String.class);
                    Log.d("addtocart", "Cart restaurantName: " + cartRestaurantName);

                    if (cartRestaurantName == null || !cartRestaurantName.equals(selectedRestaurant)) {
                        allSame = false;
                        break;
                    }
                }

                Log.d("addtocart", "Selected Restaurant: " + selectedRestaurant);
                if (!allSame) {
                    new AlertDialog.Builder(addtocart.this)
                            .setTitle("餐廳不一致")
                            .setMessage("您選擇的餐點屬於不同的餐廳。每個訂單只能包含同一餐廳的餐點。")
                            .setPositiveButton("清空購物車並添加", (dialog, which) -> {
                                clearCartData();
                                saveToFirebase();
                                Toast.makeText(addtocart.this, "已加入購物車", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    saveToFirebase();
                    Toast.makeText(addtocart.this, "已加入購物車", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addtocart.this, "無法檢查購物車。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCartData() {
        databaseReference.child("Users").child(userUID).child("cart").removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(addtocart.this, "購物車已清空", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(addtocart.this, "清空購物車失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadDataFromFirebase(String title, String category, String selectedSchool, String selectedRestaurant) {
        if (category == null || selectedSchool == null) {
            Log.e("addtocart", "Category or selectedSchool is null, cannot load data.");
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("校區").child(selectedSchool).child(selectedRestaurant).child("食物").child(category).child(title);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    calories = getValue(dataSnapshot, "熱量");
                    sugar = getValue(dataSnapshot, "糖");
                    protein = getValue(dataSnapshot, "蛋白質");
                    carbohydrates = getValue(dataSnapshot, "碳水化合物");
                    fat = getValue(dataSnapshot, "脂肪");
                    Log.d("addtocart", "Calories: " + calories);
                    Log.d("addtocart", "Sugar: " + sugar);
                    Log.d("addtocart", "Protein: " + protein);
                    Log.d("addtocart", "Carbohydrates: " + carbohydrates);
                    Log.d("addtocart", "Fat: " + fat);
                    displayNutritionInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addtocart.this, "無法加載數據。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getValue(DataSnapshot dataSnapshot, String key) {
        Object valueObj = dataSnapshot.child(key).getValue();
        return valueObj != null ? valueObj.toString() : "0";
    }

    private void updateButtonText(String title, int des, int counter) {
        Button bt4 = findViewById(R.id.bt_addtocart);
        int totalDes = des * counter;
        bt4.setText(title + "\t\t" + totalDes + "元加入購物車");
    }

    private void saveToFirebase() {
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("title", title);
        cartItem.put("description", description);
        cartItem.put("counter", counter);
        cartItem.put("calories", calories);
        cartItem.put("sugar", sugar);
        cartItem.put("protein", protein);
        cartItem.put("carbohydrates", carbohydrates);
        cartItem.put("fat", fat);
        cartItem.put("restaurantName", selectedRestaurant);
        cartItem.put("imageUrl", imageUrl);

        databaseReference.child("Users").child(userUID).child("cart").child(title).setValue(cartItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("addtocart", "Item added to cart in Firebase.");
                    } else {
                        Toast.makeText(addtocart.this, "無法將項目添加到購物車。", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}