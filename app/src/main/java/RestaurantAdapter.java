package com.example.a20240416restart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;

public class RestaurantAdapter extends ArrayAdapter<com.example.a20240416restart.Restaurant> {

    public RestaurantAdapter(@NonNull Context context, ArrayList<com.example.a20240416restart.Restaurant> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.restaurant_item, parent, false);
        }

        // 獲取當前餐廳項目
        com.example.a20240416restart.Restaurant currentRestaurant = getItem(position);

        // 確保 currentRestaurant 不為 null
        if (currentRestaurant != null) {
            // 獲取布局中的視圖
            MaterialTextView restaurantName = convertView.findViewById(R.id.restaurant_name);
            MaterialTextView restaurantDescription = convertView.findViewById(R.id.restaurant_description);
            ImageView restaurantImage = convertView.findViewById(R.id.restaurant_image);

            // 設置餐廳名稱和描述
            restaurantName.setText(currentRestaurant.getName());
            restaurantDescription.setText(currentRestaurant.getDescription());

            // 根據餐廳名稱設定圖片
            if ("美琪晨餐館".equals(currentRestaurant.getName())) {
                restaurantImage.setImageResource(R.drawable.p_restaurant_11zon);  // 美琪晨餐館的圖片
            } else if ("紅鈕扣".equals(currentRestaurant.getName())) {
                restaurantImage.setImageResource(R.drawable.restaurant05);
                // 替換為紅鈕扣的圖片資源
            }
            else if ("MINI小晨堡".equals(currentRestaurant.getName())) {
                restaurantImage.setImageResource(R.drawable.mini);
                // 替換為紅鈕扣的圖片資源
            }
            else if ("阿布早午餐".equals(currentRestaurant.getName())) {
                restaurantImage.setImageResource(R.drawable.abu);
                // 替換為紅鈕扣的圖片資源
            }
            else if ("戀茶屋".equals(currentRestaurant.getName())) {
                restaurantImage.setImageResource(R.drawable.chau);
                // 替換為紅鈕扣的圖片資源
            }else {
                restaurantImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        return convertView;
    }
}
