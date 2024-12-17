package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class sopping_car extends AppCompatActivity {

    private ArrayList<com.example.a20240416restart.ShoppingItem> cartItems;
    private com.example.a20240416restart.ShoppingListAdapter shoppingListAdapter;
    private int totalPrice = 0;
    private String name;
    private Button goPayButton;
    private String currentRestaurantName;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sopping_car);

        ListView listView = findViewById(R.id.listView);
        TextView totalTextView = findViewById(R.id.total_price);
        goPayButton = findViewById(R.id.gopaycheck);
        goPayButton.setEnabled(false);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userUID = auth.getCurrentUser().getUid();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        cartItems = new ArrayList<>();
        shoppingListAdapter = new com.example.a20240416restart.ShoppingListAdapter(this, cartItems, new com.example.a20240416restart.ShoppingListAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                com.example.a20240416restart.ShoppingItem item = cartItems.get(position);
                Intent intent = new Intent(sopping_car.this, addtocart.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("quantity", item.getQuantity());
                intent.putExtra("calories", item.getCalories());
                intent.putExtra("sugar", item.getSugar());
                intent.putExtra("protein", item.getProtein());
                intent.putExtra("carbohydrates", item.getCarbohydrates());
                intent.putExtra("fat", item.getFat());
                intent.putExtra("selectedRestaurant", item.getRestaurantName());
                intent.putExtra("imageUrl", item.getImageUrl());
                intent.putExtra("position", position);
                startActivityForResult(intent, 1);
            }

            @Override
            public void onDelete(int position) {
                cartItems.remove(position);
                shoppingListAdapter.notifyDataSetChanged();
                updateTotalPrice();
                saveCartToFirebase();
                goPayButton.setEnabled(!cartItems.isEmpty());
            }
        }, true); // 在購物車頁面啟用編輯功能
        listView.setAdapter(shoppingListAdapter);
        loadCartFromFirebase();

        totalTextView.setText(totalPrice + "元");
        shoppingListAdapter.notifyDataSetChanged();
    }

    private void loadCartFromFirebase() {
        databaseReference.child("Users").child(userUID).child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = getStringValue(snapshot.child("title"));
                    String description = getStringValue(snapshot.child("description"));
                    int counter = getIntValueFromSnapshot(snapshot.child("counter"));
                    boolean isClosed = getBooleanValueFromSnapshot(snapshot.child("isClosed"));
                    String restaurantName = getStringValue(snapshot.child("restaurantName"));
                    String imageUrl = getStringValue(snapshot.child("imageUrl"));
                    int calories = getIntValueFromSnapshot(snapshot.child("calories"));
                    int sugar = getIntValueFromSnapshot(snapshot.child("sugar"));
                    int protein = getIntValueFromSnapshot(snapshot.child("protein"));
                    int carbohydrates = getIntValueFromSnapshot(snapshot.child("carbohydrates"));
                    int fat = getIntValueFromSnapshot(snapshot.child("fat"));

                    com.example.a20240416restart.ShoppingItem item = new com.example.a20240416restart.ShoppingItem(
                            R.drawable.ic_launcher_foreground,
                            title,
                            description,
                            isClosed,
                            counter,
                            restaurantName,
                            imageUrl,
                            calories,
                            sugar,
                            protein,
                            carbohydrates,
                            fat
                    );
                    cartItems.add(item);

                    if (currentRestaurantName == null && restaurantName != null) {
                        TextView restaurantNameTextView = findViewById(R.id.restaurant_name);
                        currentRestaurantName = restaurantName;
                        restaurantNameTextView.setText(currentRestaurantName);
                    }
                }
                shoppingListAdapter.notifyDataSetChanged();
                updateTotalPrice();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(sopping_car.this, "無法加載購物車數據。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getIntValueFromSnapshot(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            Object value = snapshot.getValue();
            if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    Log.e("sopping_car", "無法解析為整數: " + value, e);
                }
            }
        }
        return 0;
    }

    private boolean getBooleanValueFromSnapshot(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            Object value = snapshot.getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }
        return false;
    }

    private String getStringValue(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            Object value = snapshot.getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

    private void updateTotalPrice() {
        TextView totalTextView = findViewById(R.id.total_price);
        totalPrice = 0;
        for (com.example.a20240416restart.ShoppingItem item : cartItems) {
            totalPrice += parsePrice(item.getDescription()) * item.getQuantity();
        }
        totalTextView.setText(totalPrice + "元");
        goPayButton.setEnabled(!cartItems.isEmpty());
    }

    private int parsePrice(String priceString) {
        String numericString = priceString.replaceAll("[^\\d]", "");
        try {
            return Integer.parseInt(numericString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void onclick_back_icon(View v) {
        finish();
    }

    public void deleteAllItems(View v) {
        clearCartData();
    }

    public void gopaycheck(View v) {
        if (!cartItems.isEmpty()) {
            saveCartToFirebase();
            Intent intent = new Intent(this, paycheck.class);
            intent.putParcelableArrayListExtra("cartItems", cartItems);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("name", name);
            intent.putExtra("restaurantName", currentRestaurantName);
            startActivity(intent);
        } else {
            Toast.makeText(this, "购物车为空，无法前往结账", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearCartData() {
        cartItems.clear();
        shoppingListAdapter.notifyDataSetChanged();
        updateTotalPrice();
        databaseReference.child("Users").child(userUID).child("cart").removeValue();
        goPayButton.setEnabled(false);
        Toast.makeText(this, "購物車已清空", Toast.LENGTH_SHORT).show();
    }

    private void saveCartToFirebase() {
        Map<String, Object> cartData = new HashMap<>();
        for (com.example.a20240416restart.ShoppingItem item : cartItems) {
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("title", item.getTitle());
            cartItem.put("description", item.getDescription());
            cartItem.put("counter", item.getQuantity());
            cartItem.put("isClosed", item.isClosed());
            cartItem.put("restaurantName", item.getRestaurantName());
            cartItem.put("imageUrl", item.getImageUrl());
            cartItem.put("calories", item.getCalories());
            cartItem.put("sugar", item.getSugar());
            cartItem.put("protein", item.getProtein());
            cartItem.put("carbohydrates", item.getCarbohydrates());
            cartItem.put("fat", item.getFat());
            cartData.put(item.getTitle(), cartItem);
        }
        databaseReference.child("Users").child(userUID).child("cart").setValue(cartData);
    }
}
