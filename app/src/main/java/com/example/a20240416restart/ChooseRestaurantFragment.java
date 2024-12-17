package com.example.a20240416restart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseRestaurantFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView lv;
    private com.example.a20240416restart.RestaurantAdapter adapter;
    private List<com.example.a20240416restart.Restaurant> restaurantList;

    public ChooseRestaurantFragment() {
        // 必需的空的構造函數
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_restaurant, container, false);

        lv = view.findViewById(R.id.ll);
        lv.setOnItemClickListener(this);

        restaurantList = new ArrayList<>();

        String selectedSchool = getArguments() != null ? getArguments().getString("selectedSchool") : "";

        DatabaseReference schoolRef = FirebaseDatabase.getInstance().getReference().child("校區").child(selectedSchool);
        schoolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                restaurantList.clear();

                for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                    String restaurantName = restaurantSnapshot.getKey();
                    String description = restaurantSnapshot.child("餐廳描述").getValue(String.class);
                    String tag = restaurantSnapshot.child("tag").getValue(String.class);
                    Double rating = restaurantSnapshot.child("rating").getValue(Double.class);

                    // 預設值處理
                    if (description == null) description = "沒有描述信息";
                    if (tag == null) tag = "無標籤";
                    if (rating == null) rating = 0.0;

                    // 建立包含所有屬性的 Restaurant 物件
                    com.example.a20240416restart.Restaurant restaurant = new com.example.a20240416restart.Restaurant(restaurantName, description, tag, rating);
                    restaurantList.add(restaurant);
                }

                adapter = new com.example.a20240416restart.RestaurantAdapter(getActivity(), new ArrayList<>(restaurantList));
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChooseRestaurantFragment", "Database error: " + databaseError.getMessage());
            }
        });

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        com.example.a20240416restart.Restaurant selectedRestaurant = restaurantList.get(position);
        String selectedSchool = getArguments() != null ? getArguments().getString("selectedSchool") : "";

        Log.d("ChooseRestaurantFragment", "Selected School: " + selectedSchool);
        Log.d("ChooseRestaurantFragment", "Selected Restaurant: " + selectedRestaurant.getName());
        String name = getArguments() != null ? getArguments().getString("name") : "";

        Meichi_Pinshan_categoryFragment categoryFragment = new Meichi_Pinshan_categoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("selectedSchool", selectedSchool);
        bundle.putString("selectedRestaurant", selectedRestaurant.getName());
        bundle.putString("name", name);
        categoryFragment.setArguments(bundle);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, categoryFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
