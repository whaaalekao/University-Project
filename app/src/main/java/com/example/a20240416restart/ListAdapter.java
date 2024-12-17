package com.example.a20240416restart;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ListItem> listItems;
    private LayoutInflater inflater;
    private int lastPosition = -1;  // 用于跟踪最后一个已显示的位置

    public ListAdapter(Context context, ArrayList<ListItem> listItems) {
        this.context = context;
        this.listItems = listItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        ImageView recipeImage = convertView.findViewById(R.id.image);
        TextView recipeTitle = convertView.findViewById(R.id.titlet);
        TextView recipeDescription = convertView.findViewById(R.id.description);

        ListItem listItem = listItems.get(position);

        // 使用 Picasso 加载图像
        Picasso.get()
                .load(listItem.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)  // 占位符图片
                .into(recipeImage);

        recipeTitle.setText(listItem.getTitle());
        recipeDescription.setText(listItem.getDescription());

        if (listItem.isClosed()) {
            recipeTitle.setTextColor(Color.GRAY);
            recipeDescription.setTextColor(Color.GRAY);
        } else {
            recipeTitle.setTextColor(Color.BLACK);
            recipeDescription.setTextColor(Color.BLACK);
        }

        // 设置动画 - 仅当当前项的位置大于最后显示的位置时应用动画
        if (position > lastPosition) {
            convertView.setTranslationX(parent.getWidth());
            convertView.setAlpha(0f);

            convertView.animate()
                    .translationX(0)
                    .alpha(1f)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            lastPosition = position;
        }

        return convertView;
    }
}
