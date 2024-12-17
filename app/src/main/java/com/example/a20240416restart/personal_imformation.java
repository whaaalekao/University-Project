package com.example.a20240416restart;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class personal_imformation extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private TextView textViewName;
    private EditText editTextHeight, editTextWeight, editTextAge;
    private RadioGroup radioGroupGender;
    private Spinner spinnerActivityLevel, spinnerGoal;
    private Button buttonChangeName, buttonSaveData;

    private String initialName, initialHeight, initialWeight, initialAge, initialGender, initialActivityLevel, initialGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_imformation);

        // 初始化 FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        ImageButton buttonBack = findViewById(R.id.button_back);
        // 設置返回按鈕點擊事件
        buttonBack.setOnClickListener(v -> finish());

        // 初始化 UI 元件
        textViewName = findViewById(R.id.name);
        editTextHeight = findViewById(R.id.height);
        editTextWeight = findViewById(R.id.weight);
        editTextAge = findViewById(R.id.age);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        buttonChangeName = findViewById(R.id.change_user_name);
        buttonSaveData = findViewById(R.id.save_button);

        // 默認將保存按鈕設為不可點擊
        buttonSaveData.setEnabled(false);

        // 獲取 Firebase 中的資料並顯示
        fetchUserData();

        // 設置修改姓名按鈕的功能
        buttonChangeName.setOnClickListener(v -> fetchUserNameAndShowDialog());

        // 設置保存按鈕，點擊後上傳資料
        buttonSaveData.setOnClickListener(v -> uploadDataToFirebase());

        // 監聽資料變更
        setupChangeListeners();
    }

    // 設置監聽器，當資料有變更時激活保存按鈕
    private void setupChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfDataChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // 監聽輸入框變更
        editTextHeight.addTextChangedListener(textWatcher);
        editTextWeight.addTextChangedListener(textWatcher);
        editTextAge.addTextChangedListener(textWatcher);

        // 監聽性別選擇變更
        radioGroupGender.setOnCheckedChangeListener((group, checkedId) -> checkIfDataChanged());

        // 監聽 Spinner 的變更
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkIfDataChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerActivityLevel.setOnItemSelectedListener(spinnerListener);
        spinnerGoal.setOnItemSelectedListener(spinnerListener);
    }

    // 檢查用戶是否更改了資料
    private void checkIfDataChanged() {
        String currentHeight = editTextHeight.getText().toString();
        String currentWeight = editTextWeight.getText().toString();
        String currentAge = editTextAge.getText().toString();
        RadioButton selectedGender = findViewById(radioGroupGender.getCheckedRadioButtonId());
        String currentGender = selectedGender != null ? selectedGender.getText().toString() : "";
        String currentActivityLevel = spinnerActivityLevel.getSelectedItem() != null ? spinnerActivityLevel.getSelectedItem().toString() : "";
        String currentGoal = spinnerGoal.getSelectedItem() != null ? spinnerGoal.getSelectedItem().toString() : "";

        boolean dataChanged = !(initialHeight != null ? initialHeight : "").equals(currentHeight)
                || !(initialWeight != null ? initialWeight : "").equals(currentWeight)
                || !(initialAge != null ? initialAge : "").equals(currentAge)
                || !(initialGender != null ? initialGender : "").equals(currentGender)
                || !(initialActivityLevel != null ? initialActivityLevel : "").equals(currentActivityLevel)
                || !(initialGoal != null ? initialGoal : "").equals(currentGoal);

        updateSaveButtonState(dataChanged);
    }

    private void updateSaveButtonState(boolean isEnabled) {
        buttonSaveData.setEnabled(isEnabled);
        buttonSaveData.setAlpha(isEnabled ? 1f : 0.5f);
    }

    // 獲取用戶的資料並顯示在相應的欄位中
    private void fetchUserData() {
        String userUid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 將所有涉及的字段都進行類型檢查
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String height = getStringValue(dataSnapshot.child("personal_info").child("height"));
                    String weight = getStringValue(dataSnapshot.child("personal_info").child("weight"));
                    String age = getStringValue(dataSnapshot.child("personal_info").child("age"));
                    String gender = dataSnapshot.child("personal_info").child("gender").getValue(String.class);
                    String activityLevel = getStringValue(dataSnapshot.child("personal_info").child("activityLevel"));
                    String goal = getStringValue(dataSnapshot.child("personal_info").child("goal"));

                    // 設定初始值
                    initialName = name != null ? name : "";
                    initialHeight = height != null ? height : "";
                    initialWeight = weight != null ? weight : "";
                    initialAge = age != null ? age : "";
                    initialGender = gender != null ? gender : "";
                    initialActivityLevel = activityLevel != null ? activityLevel : "輕度活動"; // 預設為輕度活動
                    initialGoal = goal != null ? goal : "";

                    // 顯示數據到 UI
                    textViewName.setText(initialName);
                    editTextHeight.setText(initialHeight);
                    editTextWeight.setText(initialWeight);
                    editTextAge.setText(initialAge);

                    // 設定性別
                    if ("男".equals(gender)) {
                        radioGroupGender.check(R.id.radioButtonMale);
                    } else if ("女".equals(gender)) {
                        radioGroupGender.check(R.id.radioButtonFemale);
                    }

                    // 設定 Spinner 選項
                    setSpinnerValue(spinnerActivityLevel, initialActivityLevel);
                    setSpinnerValue(spinnerGoal, initialGoal);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(personal_imformation.this, "無法讀取資料", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 輔助方法：將 Long、Double 或其他型別轉換為 String
    private String getStringValue(DataSnapshot dataSnapshot) {
        Object value = dataSnapshot.getValue();
        if (value instanceof Long) {
            return String.valueOf((Long) value); // 將 Long 類型轉換為 String
        } else if (value instanceof Double) {
            return String.valueOf((Double) value); // 將 Double 類型轉換為 String
        } else if (value instanceof String) {
            return (String) value; // 直接返回 String 類型
        }
        return "";  // 返回空字符串如果無值
    }

    private void uploadDataToFirebase() {
        String ageString = editTextAge.getText().toString().trim();
        String heightString = editTextHeight.getText().toString().trim();
        String weightString = editTextWeight.getText().toString().trim();

        // 檢查是否有空數據
        if (ageString.isEmpty() || heightString.isEmpty() || weightString.isEmpty()) {
            Toast.makeText(this, "請輸入所有數據", Toast.LENGTH_SHORT).show();
            return;
        }

        // 轉換數據
        int age = Integer.parseInt(ageString);
        float height = Float.parseFloat(heightString);
        float weight = Float.parseFloat(weightString);
        int gender = radioGroupGender.getCheckedRadioButtonId() == R.id.radioButtonMale ? 1 : 0;

        // 獲取選擇的活動強度並設定對應值
        String selectedActivityLevel = spinnerActivityLevel.getSelectedItem().toString();
        float activityLevel = 1.3f; // 預設為輕度活動
        switch (selectedActivityLevel) {
            case "輕度活動":
                activityLevel = 1.3f;
                break;
            case "中度活動":
                activityLevel = 1.55f;
                break;
            case "高度活動":
                activityLevel = 1.7f;
                break;
        }

        // 使用 String.format 將 activityLevel 格式化為一位小數
        String formattedActivityLevel = String.format("%.1f", activityLevel);

        // 根據選擇的目標設置 goal 值
        String selectedGoal = spinnerGoal.getSelectedItem().toString();
        int goalValue = 0; // 預設為維持
        switch (selectedGoal) {
            case "減重":
                goalValue = -300; // 減重時設置 -300
                break;
            case "增重":
                goalValue = 300; // 增重時設置 300
                break;
            case "維持":
                goalValue = 0; // 維持設置 0
                break;
        }

        // 建立要上傳的資料
        Map<String, Object> userData = new HashMap<>();
        userData.put("height", height);
        userData.put("weight", weight);
        userData.put("age", age);
        userData.put("gender", gender == 1 ? "男" : "女");
        userData.put("exercise intensity", formattedActivityLevel);
        userData.put("goal", goalValue); // 傳入整數的目標值

        // 上傳到 Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.child("personal_info").setValue(userData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "數據已成功上傳", Toast.LENGTH_SHORT).show();
                    buttonSaveData.setEnabled(false);
                    buttonSaveData.setAlpha(0.5f);
                } else {
                    Toast.makeText(this, "數據上傳失敗", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "用戶未登入", Toast.LENGTH_SHORT).show();
        }
    }

    // 設置 Spinner 的選項值
    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // 獲取使用者姓名並顯示修改對話框
    private void fetchUserNameAndShowDialog() {
        String userUid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String currentName = dataSnapshot.getValue(String.class);
                showChangeNameDialog(currentName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(personal_imformation.this, "無法獲取名字", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 顯示更改姓名的對話框
    private void showChangeNameDialog(String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更改名字");

        final EditText input = new EditText(this);
        input.setText(currentName);
        builder.setView(input);

        builder.setPositiveButton("確認", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
            } else {
                Toast.makeText(this, "名字不能為空", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // 更新使用者姓名到 Firebase
    private void updateUserName(String newName) {
        String userUid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        userRef.child("name").setValue(newName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "名字已更新", Toast.LENGTH_SHORT).show();
                textViewName.setText(newName);
                initialName = newName;
                buttonSaveData.setEnabled(false);
                buttonSaveData.setAlpha(0.5f);
            } else {
                Toast.makeText(this, "更新名字失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
