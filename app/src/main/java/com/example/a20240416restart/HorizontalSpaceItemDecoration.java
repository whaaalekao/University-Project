package com.example.a20240416restart;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int spaceWidth;

    public HorizontalSpaceItemDecoration(int spaceWidth) {
        this.spaceWidth = spaceWidth;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = spaceWidth;   // 設置每個項目左側的空隙
        outRect.right = spaceWidth;  // 設置每個項目右側的空隙
    }
}
