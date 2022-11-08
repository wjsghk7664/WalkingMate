package com.example.walkingmate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<RecyclerViewItem> mData;
    private Context context;

    public RecyclerViewAdapter(ArrayList<RecyclerViewItem> data, Context context) {
        this.mData = data;
        this.context = context;
    }

    // onCreateViewHolder : 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    // onBindViewHolder : position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mainText.setText(mData.get(position).getMainTitle());
        holder.userNameText.setText(mData.get(position).getUserName());
        holder.rDateText.setText(mData.get(position).getRDate());
    }

    // getItemCount : 전체 데이터의 개수를 리턴
    @Override
    public int getItemCount() {
        return (mData != null ? mData.size() : 0);
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mainText;
        TextView userNameText;
        TextView rDateText;

        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조
            this.mainText = itemView.findViewById(R.id.TitleText);
            this.userNameText = itemView.findViewById(R.id.username);
            this.rDateText = itemView.findViewById(R.id.datetext);
        }
    }
}