package com.franco.epos.appnav01;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ItemPODataAdapter extends RecyclerView.Adapter<ItemPODataAdapter.ItemViewHolder> {

    // private final OnClickListener mOnClickListener = new MyOnClickListener();
    public List<Item> items;
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView po_code, descr, s_date, status;
        public ItemViewHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.descr);
            po_code = (TextView) view.findViewById(R.id.po_code);
            s_date = (TextView) view.findViewById(R.id.s_date);
            status = (TextView) view.findViewById(R.id.status);
        }
    }

    public ItemPODataAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemPODataAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /*
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_row, parent, false);

        return new PlayerViewHolder(itemView);
         */

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_po,parent,false);



        return new ItemPODataAdapter.ItemViewHolder(itemView);

        //return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPODataAdapter.ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.descr.setText(item.getDescr());
        holder.po_code.setText(item.getItemCode());
        String sDate = item.getSDate();
        Log.d("myTag ", "This is my date " + sDate);
        holder.s_date.setText(sDate.substring(8,10) +"-"+ sDate.substring(5,7) +"-"+ sDate.substring(0,4));

        holder.status.setText(item.getStatus());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
