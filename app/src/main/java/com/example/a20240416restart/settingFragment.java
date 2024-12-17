package com.example.a20240416restart;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

/**
 * 用於設定畫面，包含登出功能及跳轉到個人資料和健康設定頁面
 */
public class settingFragment extends Fragment {

    private FirebaseAuth mAuth;  // Firebase Authentication 實例

    public settingFragment() {
        // 必須的空構造函數
    }

    // 設置 fragment 的參數
    public static settingFragment newInstance(String param1, String param2) {
        settingFragment fragment = new settingFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化 FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 為這個 fragment 加載布局
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // 查找登出按鈕並設置點擊事件
        Button buttonLogout = view.findViewById(R.id.btn_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();  // 執行登出操作
            }
        });

        // 查找個人資料按鈕並設置點擊事件
        Button buttonToPersonalInfo = view.findViewById(R.id.to_personal_imformation);
        buttonToPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳轉到 personal_imformation 頁面
                Intent intent = new Intent(getActivity(), personal_imformation.class);
                startActivity(intent);
            }
        });

        // 查找健康專區按鈕並設置點擊事件
        Button buttonToHealthSetting = view.findViewById(R.id.btn_health);
        buttonToHealthSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳轉到 Health_setting 頁面
                Intent intent = new Intent(getActivity(), Health_setting.class);
                startActivity(intent);
            }
        });

        return view;
    }

    // 登出功能實現
    private void logout() {
        mAuth.signOut();  // 退出當前的 Firebase 用戶
        Intent intent = new Intent(getActivity(), MainActivity.class);  // 返回到 MainActivity
        startActivity(intent);  // 啟動 MainActivity
        if (getActivity() != null) {
            getActivity().finish();  // 結束當前的活動
        }
    }
}
