package com.example.dailycalendar;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

// HorizontalSpaceItemDecoration.java
public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int horizontalSpaceWidth;

    public HorizontalSpaceItemDecoration(int horizontalSpaceWidth) {
        this.horizontalSpaceWidth = horizontalSpaceWidth;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.right = horizontalSpaceWidth;
    }
}