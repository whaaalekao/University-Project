package com.example.a20240416restart;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class Meichi_category_Adapter extends RecyclerView.Adapter<Meichi_category_Adapter.ViewHolder> {

    private final List<CategoryItem> categoryItemList;
    private final OnItemClickListener onItemClickListener;

    public Meichi_category_Adapter(List<CategoryItem> categoryItemList, OnItemClickListener onItemClickListener) {
        this.categoryItemList = categoryItemList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.foodcategory_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem item = categoryItemList.get(position);
        holder.categoryTitle.setText(item.getTitle());

        // 檢查圖片 URL 是否為 null 或空
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)  // 占位符圖像
                    .error(R.drawable.linshanrestauran)  // 錯誤時顯示的圖像
                    .into(holder.categoryImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("Meichi_category_Adapter", "Image loaded successfully for item: " + item.getTitle());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Meichi_category_Adapter", "Error loading image for item: " + item.getTitle(), e);
                        }
                    });
        } else {
            // 如果 URL 為 null 或空，則設置占位符圖像
            holder.categoryImage.setImageResource(R.drawable.ic_launcher_foreground);
            Log.w("Meichi_category_Adapter", "Image URL is null or empty for item: " + item.getTitle());
        }

        // 設置點擊監聽器
        holder.itemView.setOnClickListener(v -> {
            Log.d("Meichi_category_Adapter", "Item clicked: " + item.getTitle() + " at position: " + position);
            onItemClickListener.onItemClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return categoryItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        ImageView categoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categorytitle);
            categoryImage = itemView.findViewById(R.id.categoryimage);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(CategoryItem item, int position);
    }
}
