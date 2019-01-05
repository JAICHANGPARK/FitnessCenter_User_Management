package com.dreamwalker.myapplication103.adapter.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dreamwalker.myapplication103.R;
import com.dreamwalker.myapplication103.model.SearchResult;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    Context context;
    ArrayList<SearchResult> userItems;

    OnSearchItemClickListener listener;

    public void setOnSearchItemClickListener(OnSearchItemClickListener listener) {
        this.listener = listener;
    }

    public SearchAdapter(Context context, ArrayList<SearchResult> userItems) {
        this.context = context;
        this.userItems = userItems;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {


        holder.name.setText(userItems.get(position).getUserName());
        holder.phone.setText(userItems.get(position).getUserPhone());
        holder.regiDate.setText(userItems.get(position).getRegiDate());
        holder.tag.setText(userItems.get(position).getUserTag());
//        holder.group.setText(foodItems.get(position).getFoodGroup());
//        holder.amount.setText(foodItems.get(position).getFoodAmount());
//        holder.kcal.setText(foodItems.get(position).getFoodKcal());
//        holder.carbo.setText(foodItems.get(position).getFoodCarbo());
//        holder.protein.setText(foodItems.get(position).getFoodProtein());
//        holder.fat.setText(foodItems.get(position).getFoodFat());


    }

    @Override
    public int getItemCount() {
        return userItems.size();
    }


    public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, tag, regiDate, phone;

         SearchViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_name_textview);
            tag = itemView.findViewById(R.id.tag_id_textview);
            regiDate = itemView.findViewById(R.id.regi_date_text_view);
            phone = itemView.findViewById(R.id.user_phone_text_view);


            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (listener != null) {
                listener.onSearchItemClick(v, getAdapterPosition());
            }
        }
    }
}
