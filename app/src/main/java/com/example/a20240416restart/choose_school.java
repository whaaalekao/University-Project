package com.example.a20240416restart;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;

public class choose_school extends AppCompatActivity {
    ListView rc_school;
    ArrayList<String> ar;
    SchoolAdapter schoolAdapter;
    ArrayList<String> ls;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_school);
        Intent it = getIntent();
        String name = it.getStringExtra("name");
        rc_school = findViewById(R.id.rc_school);
        TextView tv = findViewById(R.id.tv);

        // 初始化 FirebaseAuth 实例
        mAuth = FirebaseAuth.getInstance();

        // 設置逐字動畫
        String text = "你好!今天想預定哪一間校區的餐廳呢?";
        long delay = 125; // 每個字符顯示的延遲時間，以毫秒為單位
        TextAnimator textAnimator = new TextAnimator(tv, text, delay);
        textAnimator.animateText();
        // 設置 ListView 的淡入動畫
        AlphaAnimation qb = new AlphaAnimation(0.0f, 1.0f);
        qb.setDuration(1900);
        rc_school.startAnimation(qb);

        ls = new ArrayList<>(Arrays.asList("屏商校區", "民生校區", "屏師校區"));
        ar = new ArrayList<>(ls);

        // 設置 ListView 的自定義適配器
        schoolAdapter = new SchoolAdapter(this, ar);
        rc_school.setAdapter(schoolAdapter);

        // 為 ListView 設置點擊事件
        rc_school.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSchool = ar.get(position);
                // 處理選中的校區
                Intent intent = new Intent(choose_school.this, choose_restaurant.class);
                intent.putExtra("selectedSchool", selectedSchool);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });
        Button buttonLogout = findViewById(R.id.logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    // 登出方法
    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(choose_school.this, MainActivity.class);
        startActivity(intent);
        finish();  // 结束当前的 MainActivity
    }
    public void his(View v){
        Intent hi = new Intent(this, history_order.class);
        startActivity(hi);
    }
}
