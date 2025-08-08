package com.franco.epos.appnav01;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ItemPOFilterDataAdapter extends RecyclerView.Adapter<ItemPOFilterDataAdapter.ItemViewHolder> {

    // private final OnClickListener mOnClickListener = new MyOnClickListener();
    Context context;
//    public List<ItemFilter> items;
    List<ItemFilter> items = new ArrayList<>();
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView itemLU, descr, in_qty;
        CheckBox checkBox;
        public ItemViewHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.descr);
            itemLU = (TextView) view.findViewById(R.id.itemLU);
            in_qty = (TextView) view.findViewById(R.id.in_qty);
            checkBox = itemView.findViewById(R.id.checkBox_select);

        }
    }

    public ItemPOFilterDataAdapter(Context context, List<ItemFilter> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemPOFilterDataAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /*
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_row, parent, false);

        return new PlayerViewHolder(itemView);
         */

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_fpo,parent,false);



        return new ItemPOFilterDataAdapter.ItemViewHolder(itemView);

        //return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPOFilterDataAdapter.ItemViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final  ItemFilter item = items.get(position);

        holder.descr.setText(item.getDescr());
        holder.itemLU.setText(item.getItemLU());
        holder.in_qty.setText(item.getInQty());

        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setTag(items.get(position));


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "Testing";
                ItemFilter item1 = (ItemFilter)holder.checkBox.getTag();

                item1.setSelected(holder.checkBox.isChecked());

                items.get(position).setSelected(holder.checkBox.isChecked());

                for (int j=0; j<items.size();j++){

                    if (items.get(j).isSelected() == true){
                        data = data + "\n" + items.get(j).getDescr().toString() + "   " + items.get(j).getInQty().toString();
                    }
                }
                Log.d("myTag ", "This is my message Filter" + data + items.size());
//                Toast.makeText(context, "Selected Fruits : \n " + data, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
