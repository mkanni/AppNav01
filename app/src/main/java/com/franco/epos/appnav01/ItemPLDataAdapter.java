package com.franco.epos.appnav01;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ItemPLDataAdapter extends RecyclerView.Adapter<ItemPLDataAdapter.ItemViewHolder> {

    // private final OnClickListener mOnClickListener = new MyOnClickListener();
    public List<Item> items;
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView  descr, itemLU, price, in_qty;
        public ItemViewHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.descr);
            itemLU = (TextView) view.findViewById(R.id.itemLU);
            price = (TextView) view.findViewById(R.id.price);
            in_qty = (TextView) view.findViewById(R.id.in_qty);
        }
    }

    public ItemPLDataAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemPLDataAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /*
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_row, parent, false);

        return new PlayerViewHolder(itemView);
         */

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_pl,parent,false);



        return new ItemPLDataAdapter.ItemViewHolder(itemView);

        //return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPLDataAdapter.ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.descr.setText(item.getDescr());
        holder.itemLU.setText(item.getItemCode());
        holder.price.setText(item.getPrice());
        holder.in_qty.setText(item.getInQty());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
