package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.SimpleExpandableListAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class history_order extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private SimpleExpandableListAdapter expandableListAdapter;
    private List<Map<String, String>> listGroupTitles;
    private List<List<Map<String, String>>> listChildItems;
    private List<Map<String, String>> originalListGroupTitles;
    private List<List<Map<String, String>>> originalListChildItems;
    private FirebaseAuth mAuth;
    private LinearLayout mainLayout;
    private boolean allGroupsExpanded = false; // 用于跟踪所有组的展开状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);  // 确保与布局文件名称一致

        mAuth = FirebaseAuth.getInstance();

        expandableListView = findViewById(R.id.expandableListView);
        SearchView searchView = findViewById(R.id.searchView);
        mainLayout = findViewById(R.id.main_layout);

        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            searchView.clearFocus();

            searchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchView.setIconified(false);
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterData(newText);
                    return true;
                }
            });
        }

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboardAndClearFocus(v);
                return false;
            }
        });

        expandableListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboardAndClearFocus(v);
                return false;
            }
        });

        listGroupTitles = new ArrayList<>();
        listChildItems = new ArrayList<>();
        originalListGroupTitles = new ArrayList<>();
        originalListChildItems = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String userUid = currentUser.getUid();
        loadDataFromFirebase(userUid);

        expandableListAdapter = new SimpleExpandableListAdapter(
                this,
                listGroupTitles,
                android.R.layout.simple_expandable_list_item_1,
                new String[]{"OrderInfo"},
                new int[]{android.R.id.text1},
                listChildItems,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{"ItemInfo"},
                new int[]{android.R.id.text1}
        );

        expandableListView.setAdapter(expandableListAdapter);

        Button buttonLogout = findViewById(R.id.btn_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Button buttonToggleExpand = findViewById(R.id.btn_toggle_expand);
        buttonToggleExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpandAllGroups();
            }
        });
    }

    private void hideKeyboardAndClearFocus(View view) {
        SearchView searchView = findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.clearFocus();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void toggleExpandAllGroups() {
        if (allGroupsExpanded) {
            // 如果所有组都已展开，则收起所有组
            for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                expandableListView.collapseGroup(i);
            }
            allGroupsExpanded = false;
        } else {
            // 如果所有组都未展开，则展开所有组
            for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                expandableListView.expandGroup(i);
            }
            allGroupsExpanded = true;
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth != null && mAuth.getCurrentUser() != null) {

        }
    }

    private void loadDataFromFirebase(String userUid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
        databaseReference.orderByChild("userUid").equalTo(userUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listGroupTitles.clear();
                        listChildItems.clear();
                        originalListGroupTitles.clear();
                        originalListChildItems.clear();

                        for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                            String orderId = orderSnapshot.getKey();
                            String orderInfo = "｜訂購人: " + orderSnapshot.child("名字").getValue(String.class);

                            // 将时间戳转换为日期格式
                            Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);
                            if (timestamp != null) {
                                String formattedDate = formatTimestampToDate(timestamp);
                                orderInfo += " | 取餐時間: " + formattedDate;
                            }

                            Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);
                            if (totalPrice != null) {
                                orderInfo += " | 總價: " + totalPrice + "元";
                            }

                            Map<String, String> group = new HashMap<>();
                            group.put("OrderInfo", orderInfo);
                            listGroupTitles.add(group);
                            originalListGroupTitles.add(group);

                            List<Map<String, String>> childItems = new ArrayList<>();
                            List<Map<String, String>> originalChildItems = new ArrayList<>();
                            for (DataSnapshot itemSnapshot : orderSnapshot.child("items").getChildren()) {
                                String itemTitle = itemSnapshot.child("title").getValue(String.class);
                                String itemDesc = itemSnapshot.child("description").getValue(String.class) + "元";
                                int quantity = itemSnapshot.child("quantity").getValue(Integer.class);

                                String note = itemSnapshot.child("note").getValue(String.class);
                                StringBuilder addingBuilder = new StringBuilder();
                                DataSnapshot addingSnapshot = itemSnapshot.child("adding");
                                if (addingSnapshot.exists()) {
                                    for (DataSnapshot add : addingSnapshot.getChildren()) {
                                        String addName = add.getKey();
                                        Integer addQuantity = add.getValue(Integer.class);
                                        addingBuilder.append(addName).append(": ").append(addQuantity).append("元 ");
                                    }
                                }

                                String itemInfo = "品項: " + itemTitle
                                        + " | 價格: " + itemDesc
                                        + " | 數量: " + quantity
                                        + (note != null ? " | 備註: " + note : "")
                                        + (addingBuilder.length() > 0 ? " | 加料: " + addingBuilder.toString().trim() : "");

                                Map<String, String> child = new HashMap<>();
                                child.put("ItemInfo", itemInfo);
                                childItems.add(child);
                                originalChildItems.add(child);
                            }

                            listChildItems.add(childItems);
                            originalListChildItems.add(originalChildItems);
                        }

                        expandableListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private String formatTimestampToDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void filterData(String query) {
        query = query.toLowerCase();
        listGroupTitles.clear();
        listChildItems.clear();

        if (query.isEmpty()) {
            listGroupTitles.addAll(originalListGroupTitles);
            listChildItems.addAll(originalListChildItems);
        } else {
            for (int i = 0; i < originalListGroupTitles.size(); i++) {
                Map<String, String> group = originalListGroupTitles.get(i);
                List<Map<String, String>> childItems = originalListChildItems.get(i);

                if (group.get("OrderInfo").toLowerCase().contains(query)) {
                    listGroupTitles.add(group);
                    listChildItems.add(childItems);
                }
            }
        }

        expandableListAdapter.notifyDataSetChanged();
    }
}
