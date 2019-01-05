package com.dreamwalker.myapplication103.adapter.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dreamwalker.myapplication103.R;
import com.dreamwalker.myapplication103.adapter.device.scan.DeviceItemClickListener;
import com.dreamwalker.myapplication103.model.SearchResult;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class SearchAdapterV2 extends RecyclerView.Adapter<SearchAdapterV2.SearchViewHolderV2> {

    Context context;
    ArrayList<SearchResult> userItems;

    DeviceItemClickListener deviceItemClickListener;


    public void setDeviceItemClickListener(DeviceItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public SearchAdapterV2(ArrayList<SearchResult> deviceArrayList, Context context) {
        this.context = context;
        this.userItems = deviceArrayList;
    }

    @NonNull
    @Override
    public SearchViewHolderV2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search_result, parent, false);
        return new SearchViewHolderV2(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolderV2 holder, int position) {


        holder.name.setText(userItems.get(position).getUserName());
        holder.phone.setText(userItems.get(position).getUserPhone());
        holder.regiDate.setText(userItems.get(position).getRegiDate());
        holder.tag.setText(userItems.get(position).getUserTag());

    }

    @Override
    public int getItemCount() {
        return userItems.size();
    }


    class SearchViewHolderV2 extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, tag, regiDate, phone;

        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        LinearLayout container;

        SearchViewHolderV2(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_name_textview);
            tag = itemView.findViewById(R.id.tag_id_textview);
            regiDate = itemView.findViewById(R.id.regi_date_text_view);
            phone = itemView.findViewById(R.id.user_phone_text_view);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (deviceItemClickListener != null) {
                deviceItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

    }


}
