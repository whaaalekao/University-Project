package com.example.a20240416restart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private ListView rc_school;  // ListView 用於顯示學校列表
    private ArrayList<String> ar;  // 學校列表數據
    private SchoolAdapter schoolAdapter;  // 自定義適配器
    private ArrayList<String> ls;  // 用於初始化的學校名稱列表
    private TextView moneyTextView; // 用於顯示餘額的 TextView
    private FirebaseAuth mAuth;

    public HomeFragment() {
        // 必需的空的公開構造函數
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 確保佈局名稱是 fragment_home 並且存在於 res/layout 資料夾中
        View view = inflater.inflate(R.layout.fragment_home2, container, false);

        // 設置 ListView 和 TextView
        rc_school = view.findViewById(R.id.rc_school);  // 使用 `view.findViewById` 來初始化 ListView
        moneyTextView = view.findViewById(R.id.money);  // 初始化顯示餘額的 TextView

        // 初始化 FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 檢查是否有用戶登入，並載入餘額
        if (currentUser != null) {
            String userUid = currentUser.getUid();
            loadMoneyFromFirebase(userUid);
        } else {
            Toast.makeText(getContext(), "請先登入以查看餘額", Toast.LENGTH_SHORT).show();
        }

        // 初始化學校列表並設置適配器
        ls = new ArrayList<>(Arrays.asList("屏商校區", "民生校區", "屏師校區"));  // 初始化學校名稱列表
        ar = new ArrayList<>(ls);  // 直接使用原來的名稱，不進行映射

        schoolAdapter = new SchoolAdapter(getActivity(), ar);  // 創建自定義適配器
        rc_school.setAdapter(schoolAdapter);  // 設置適配器

        // 設置 ListView 點擊事件以導航到不同的餐廳頁面
        rc_school.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSchool = ls.get(position);  // 根據原本位置獲取選中的學校

                // 創建新的 Fragment 並傳遞選中的校區
                ChooseRestaurantFragment chooseRestaurantFragment = new ChooseRestaurantFragment();
                Bundle args = new Bundle();
                args.putString("selectedSchool", selectedSchool);
                chooseRestaurantFragment.setArguments(args);

                // 使用 FragmentTransaction 替換當前的 Fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, chooseRestaurantFragment);
                transaction.addToBackStack(null);  // 將事務添加到返回堆棧
                transaction.commit();
            }
        });

        return view;  // 返回視圖
    }

    // 從 Firebase 中載入指定使用者的 money 數據
    private void loadMoneyFromFirebase(String userUid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);
        userRef.child("money").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer money = dataSnapshot.getValue(Integer.class);
                    if (money != null) {
                        // 更新 UI，使用格式化方式顯示金額
                        moneyTextView.setText(String.format(Locale.getDefault(), "%,d 元", money));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理錯誤
            }
        });
    }
}
