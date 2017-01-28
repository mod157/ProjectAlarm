package com.nammu.sunjae.projectalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by SunJae on 2017-01-24.
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder>{
    private ArrayList<AlarmItem> itemList = new ArrayList<AlarmItem>();
    private int itemLayout;
    private Context context;
    AlarmAdapter(ArrayList<AlarmItem> items, int layout, Context context){
        itemList = items;
        itemLayout = layout;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlarmAdapter.ViewHolder holder, int position) {
        AlarmItem item = itemList.get(position);
        holder.tv_time.setText(item.getTime());
        setDaysColor(item.getDays(), holder);
        holder.itemView.setTag(item);
    }

    private void setDaysColor(boolean[] days, ViewHolder holder){
        //선택된 날짜만 색을 빨간색으로 표시
        TextView[] tvs = {holder.tv_day_mon, holder.tv_day_tue, holder.tv_day_wendes, holder.tv_day_thurs, holder.tv_day_fri, holder.tv_day_satur, holder.tv_day_sun};
        for(int i = 0; i < tvs.length; i++){
            if(days[i] == true)
                tvs[i].setTextColor(Color.parseColor("#FF0000"));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_edit)
        ImageView iv_edit;
        @BindView(R.id.iv_delete)
        ImageView iv_delete;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.tv_day_mon)
        TextView tv_day_mon;
        @BindView(R.id.tv_day_tue)
        TextView tv_day_tue;
        @BindView(R.id.tv_day_wednes)
        TextView tv_day_wendes;
        @BindView(R.id.tv_day_thurs)
        TextView tv_day_thurs;
        @BindView(R.id.tv_day_fri)
        TextView tv_day_fri;
        @BindView(R.id.tv_day_satur)
        TextView tv_day_satur;
        @BindView(R.id.tv_day_sun)
        TextView tv_day_sun;

        @OnClick({R.id.iv_delete, R.id.iv_edit})
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_delete) {
                delete(getPosition());
            } else {
                edit(getPosition());
            }
        }

        public void delete(int position){
            try {
                //list에서 삭제하고, View에서도 삭제
                itemList.remove(position);
                notifyItemRemoved(position);
                Intent send = new Intent("Alarm_Call");
                context.sendBroadcast(send);
                RealmDB.DeleteData(position, context);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        public void edit(int position){
            try{
                //DetailActivity로 넘어가게 되고 작업은 edit 작업을 수행
                Intent intent = new Intent(context,DetailActivity.class);
                intent.putExtra("action", "edit");
                intent.putExtra("position", position+"");
                context.startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
