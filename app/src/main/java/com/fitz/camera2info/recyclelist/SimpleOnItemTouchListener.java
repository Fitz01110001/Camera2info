package com.fitz.camera2info.recyclelist;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.recyclelist
 * @ClassName: SimpleOnItemTouchListener
 * @Author: Fitz
 * @CreateDate: 2019/12/21 18:10
 */
public class SimpleOnItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {
    GestureDetector gestureDetector;
    RecyclerView rv;

    public SimpleOnItemTouchListener(RecyclerView rv, RvItemClickListener listener) {
        this.rv = rv;
        this.listener = listener;
        gestureDetector = new GestureDetector(rv.getContext(), gestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        super.onTouchEvent(rv, e);
        gestureDetector.onTouchEvent(e);
    }

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null) {
                int position = rv.getChildAdapterPosition(child);
                if (listener != null) {
                    listener.singleTab(position, rv.getChildViewHolder(child));
                }
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    };

    public RvItemClickListener listener;

    public interface RvItemClickListener {
        void singleTab(int position, RecyclerView.ViewHolder viewHolder);

        void longPress(int position);
    }


}
