package com.example.a20240416restart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShoppingListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<com.example.a20240416restart.ShoppingItem> items;
    private OnItemActionListener onItemActionListener;
    private boolean enableEdit; // 用於控制是否顯示編輯功能

    public ShoppingListAdapter(Context context, ArrayList<com.example.a20240416restart.ShoppingItem> items, OnItemActionListener onItemActionListener, boolean enableEdit) {
        this.context = context;
        this.items = items;
        this.onItemActionListener = onItemActionListener;
        this.enableEdit = enableEdit; // 初始化時設置是否啟用編輯功能
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.shopping_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image);
        TextView titleTextView = convertView.findViewById(R.id.titlet);
        TextView descriptionTextView = convertView.findViewById(R.id.description);
        TextView quantityTextView = convertView.findViewById(R.id.quantity);
        ImageButton moreOptionsButton = convertView.findViewById(R.id.more_options_button);

        final com.example.a20240416restart.ShoppingItem item = items.get(position);

        // 使用 Picasso 或其他圖片加載庫加載圖片
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).placeholder(R.drawable.ic_launcher_foreground).into(imageView);
        } else {
            imageView.setImageResource(item.getImageId()); // 使用本地資源ID的圖片
        }

        titleTextView.setText(item.getTitle());
        descriptionTextView.setText(item.getDescription());
        quantityTextView.setText("数量: " + item.getQuantity());

        // 設置更多選項按鈕
        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.item_menu);

                // 根據 enableEdit 設置是否顯示編輯選項
                if (!enableEdit) {
                    popupMenu.getMenu().findItem(R.id.action_edit).setVisible(false); // 隱藏編輯選項
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_edit) {
                            onItemActionListener.onEdit(position);
                            return true;
                        } else if (menuItem.getItemId() == R.id.action_delete) {
                            onItemActionListener.onDelete(position);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        return convertView;
    }

    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }
}
