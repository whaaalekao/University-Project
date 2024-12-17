package com.example.a20240416restart;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    // 声明 UI 组件和 Firebase 身份验证实例
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin, buttonForgotPassword;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    CheckBox rememberMeCheckBox;
    SharedPreferences sharedPreferences;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局文件
        setContentView(R.layout.activity_main);

        // 初始化 UI 组件
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.name);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        buttonForgotPassword = findViewById(R.id.btn_forgot_password);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.RegisterNow);
        rememberMeCheckBox = findViewById(R.id.checkbox_remember_me);

        // 获取 SharedPreferences 实例，用于存储和读取用户凭据
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        loadLoginDetails();

        // 手動獲取 FCM 令牌
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // 成功獲取到令牌
                            String token = task.getResult();
                            Log.d(TAG, "獲取到的 FCM Token: " + token);
                            // 您可以將此令牌保存到伺服器或其他地方
                        } else {
                            // 獲取令牌失敗
                            Log.e(TAG, "無法獲取 FCM Token", task.getException());
                        }
                    }
                });

        textView.setOnClickListener(view -> {
            // 跳转到注册活动
            Intent intent = new Intent(getApplicationContext(), sign_up.class);
            startActivity(intent);
        });

        buttonLogin.setOnClickListener(v -> {
            // 显示进度条
            progressBar.setVisibility(View.VISIBLE);
            // 获取用户输入的电子邮件和密码
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // 检查电子邮件是否为空
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(MainActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // 检查密码是否为空
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // 使用 Firebase 身份验证进行登录
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        // 隐藏进度条
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 获取用户的 UID
                                String uid = user.getUid();

                                // 从 Firebase 中检索用户的名字
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("name");
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String name = dataSnapshot.getValue(String.class);
                                        if (name != null) {
                                            // 保存用户名到 SharedPreferences
                                            saveUserName(name);
                                            // 跳转到 MainActivity2 活动，并传递用户名
                                            Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                                            intent.putExtra("uid", uid);
                                            intent.putExtra("name", name);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(MainActivity.this, "未能检索到用户名", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(MainActivity.this, "数据库错误：" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                if (rememberMeCheckBox.isChecked()) {
                                    saveLoginDetails(email, password);
                                } else {
                                    clearLoginDetails();
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "密码或电子邮件错误", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Authentication failed: " + task.getException().getMessage());
                        }
                    });
        });

        buttonForgotPassword.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(MainActivity.this, "Enter Email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to send password reset email: " + task.getException().getMessage());
                        }
                    });
        });
    }

    private void saveLoginDetails(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    private void saveUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.apply();
    }

    private void clearLoginDetails() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("email");
        editor.remove("password");
        editor.apply();
    }

    private void loadLoginDetails() {
        String email = sharedPreferences.getString("email", "");
        String password = sharedPreferences.getString("password", "");
        if (!email.isEmpty() && !password.isEmpty()) {
            editTextEmail.setText(email);
            editTextPassword.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }
}
