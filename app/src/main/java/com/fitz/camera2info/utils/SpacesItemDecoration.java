package com.fitz.camera2info.utils;

/**
 * @ProjectName: ABus
 * @Package: com.fitz.abus.utils
 * @Author: Fitz
 * @CreateDate: 2019/1/11
 */

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int left;
    private int right;
    private int top;
    private int bottom;

    public SpacesItemDecoration(int l, int r, int t, int b) {
        left = l;
        right = r;
        top = t;
        bottom = b;
    }

    @Override
    public void getItemOffsets(
            Rect outRect, View view,
            RecyclerView parent, RecyclerView.State state) {
        outRect.left = left;
        outRect.right = right;
        outRect.bottom = bottom;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = top;
        }

    }
}
