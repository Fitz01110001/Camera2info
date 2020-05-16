package com.fitz.camera2info.recyclelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.camerainfo.CameraItem;

import java.util.List;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.Adapter
 * @ClassName: MyListAdapter
 * @Author: Fitz
 * @CreateDate: 2019/12/16 1:38
 */
public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    private Context mContext;
    private List<CameraItem> mList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cameraName;
        TextView cameradetail;

        public ViewHolder(View view) {
            super(view);
            cameraName = (TextView) view.findViewById(R.id.camera_item_id);
            cameradetail = (TextView) view.findViewById(R.id.camera_item_detail);
        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.camera_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CameraItem item = mList.get(position);
        holder.cameraName.setText(setCameraNameText(item));
        holder.cameradetail.setText(setCameraDetailText(item));
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public MyListAdapter(Context context, List<CameraItem> list) {
        mContext = context;
        mList = list;
    }

    private String setCameraNameText(CameraItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.getCameraFront()) {
            sb.append(mContext.getResources().getString(R.string.front_camera));
            sb.append(" id: ");
            sb.append(item.getCameraId());

            return sb.toString();
        }else {
            sb.append(mContext.getResources().getString(R.string.rear_camera));
            sb.append(" id: ");
            sb.append(item.getCameraId());

            return sb.toString();
        }

    }

    private String setCameraDetailText(CameraItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.isMulitCamera()) {
            sb.append(mContext.getResources().getString(R.string.mulit_logic_camera));
            sb.append(": ");
            sb.append(item.getPhysicalCameraIds());

            return sb.toString();
        } else {
            sb.append(mContext.getResources().getString(R.string.single_physical_id));
            sb.append(": ");
            sb.append(item.getCameraId());

            return sb.toString();
        }
    }

    public static class ItemOnClickListener implements SimpleOnItemTouchListener.RvItemClickListener {

        private static final String TAG = "ItemOnClickListener";
        @Override
        public void singleTab(int position, RecyclerView.ViewHolder viewHolder) {

            CameraLog.d(TAG, "singleTab, position: " + position);
        }

        @Override
        public void longPress(int position) {
            CameraLog.d(TAG,"singleTab, position: " + position);
        }
    }

}
