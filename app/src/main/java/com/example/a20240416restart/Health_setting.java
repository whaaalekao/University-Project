package com.example.a20240416restart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.Random;

public class Health_setting extends AppCompatActivity {

    private ImageView healthTipImage;
    private TextView healthTipContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_setting);

        // 設定每日健康小貼士
        String[] healthTips = {
                "早睡早起保持充足睡眠。",
                "多喝水有助於身體代謝。",
                "每天運動至少30分鐘。",
                "避免長時間久坐，每隔1小時站起來活動。",
                "均衡飲食，攝取各種營養素。"
        };

        // 設定小貼士的圖片資源
        int[] healthTipImages = {
                R.drawable.healthcare,
                R.drawable.healthfood,
                R.drawable.sleep,
                R.drawable.water,
                R.drawable.sports
        };

        // 初始化 UI 元素
        healthTipImage = findViewById(R.id.health_tip_image);
        healthTipContent = findViewById(R.id.health_tip_content);
        Button btnRefreshTip = findViewById(R.id.btn_refresh_tip);

        // 顯示隨機小貼士
        displayRandomTip(healthTips, healthTipImages);

        // 按鈕點擊事件：刷新小貼士和圖片
        btnRefreshTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayRandomTip(healthTips, healthTipImages);
            }
        });

        // 設置卡片點擊事件
        setupCardClickListeners();
    }

    private void displayRandomTip(String[] tips, int[] images) {
        Random random = new Random();
        int randomIndex = random.nextInt(tips.length);

        // 設置隨機的小貼士內容和圖片
        healthTipContent.setText(tips[randomIndex]);
        healthTipImage.setImageResource(images[randomIndex]);
    }

    private void setupCardClickListeners() {
        // Peeta Card
        CardView peetaCard = findViewById(R.id.peeta_card);
        peetaCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYouTubeChannel("https://www.youtube.com/@peetagege");
            }
        });

        // Ricky's Time Card
        CardView rickyCard = findViewById(R.id.ricky_card);
        rickyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYouTubeChannel("https://www.youtube.com/@RickysTime");
            }
        });

        // 營養師杯蓋 Card
        CardView nutruelifeCard = findViewById(R.id.nutruelife_card);
        nutruelifeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYouTubeChannel("https://www.youtube.com/@nutruelife");
            }
        });

        // shuaisoserious Card
        CardView shuaCard = findViewById(R.id.shuaisoserious_card);
        shuaCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYouTubeChannel("https://www.youtube.com/@shuaisoserious");
            }
        });

        // 健康2.0 Card
        CardView healthTwoCard = findViewById(R.id.heaithtwo_card);
        healthTwoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYouTubeChannel("https://www.youtube.com/@tvbshealth20");
            }
        });
    }

    // 開啟指定的 YouTube 頻道
    private void openYouTubeChannel(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.youtube"); // 確保打開 YouTube 應用
        try {
            startActivity(intent);
        } catch (Exception e) {
            // 若無 YouTube 應用則打開瀏覽器
            intent.setPackage(null);
            startActivity(intent);
        }
    }
}
