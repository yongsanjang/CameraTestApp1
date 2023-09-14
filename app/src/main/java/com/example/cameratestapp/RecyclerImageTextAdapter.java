package com.example.cameratestapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class RecyclerImageTextAdapter extends RecyclerView.Adapter<RecyclerImageTextAdapter.ViewHolder> {
    private ArrayList<RecyclerItem> mData = new ArrayList<>() ;

    // 생성자에서 데이터 리스트 객체를 전달받음.
    RecyclerImageTextAdapter(ArrayList<RecyclerItem> list) {
        mData = list ;
    }

    RecyclerImageTextAdapter(){

    }

    public RecyclerImageTextAdapter setDataList(ArrayList<RecyclerItem> list){
        mData = list ;
        notifyDataSetChanged();
        return this;
    }

    public RecyclerImageTextAdapter insertItem(RecyclerItem item){
        mData.add(item);
        notifyItemChanged(mData.size() -1);
        return this;
    }

    public RecyclerImageTextAdapter removeItem(int position){
        if (mData != null && mData.size() > 0 && position < mData.size() && position > -1) {
            mData.remove(position);
            notifyItemRemoved(position);
        }
        return this;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public RecyclerImageTextAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.recycler_item, parent, false) ;
        RecyclerImageTextAdapter.ViewHolder vh = new RecyclerImageTextAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(RecyclerImageTextAdapter.ViewHolder holder, int position) {

        RecyclerItem item = mData.get(position) ;

        holder.icon.setImageDrawable(item.getIcon()) ;
        holder.title.setText(item.getTitle()) ;
        holder.desc.setText(item.getDesc()) ;
        holder.title.setText(item.getImageFullPath()+"") ;
        Glide.with(holder.icon.getContext())
                .load(new File(item.getImageFullPath()))
                .apply(new RequestOptions().override(300, 300)
                        .transform(new RoundedCorners(20)))
                .into(holder.icon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.icon.getContext());
                builder.setMessage("이미지 삭제")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File file = new File(mData.get(position).getImageFullPath());
                                if (file.exists()) {
                                    boolean isDelete = file.delete();
                                    removeItem(position);
                                }
                            }
                        })
                        .setNegativeButton("취소", (dialog, id) -> {
                            dialog.dismiss();
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
            }
        });
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon ;
        TextView title ;
        TextView desc ;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            icon = itemView.findViewById(R.id.icon) ;
            title = itemView.findViewById(R.id.title) ;
            desc = itemView.findViewById(R.id.desc) ;
        }
    }
}
