package com.franco.epos.appnav01;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class ItemALDataAdapter extends RecyclerView.Adapter<ItemALDataAdapter.ItemViewHolder> {

    // private final OnClickListener mOnClickListener = new MyOnClickListener();
    public List<Item> items;
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView itemLU, descr, in_qty, price;
        CheckBox checkBox;
        public ItemViewHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.descr);
            itemLU = (TextView) view.findViewById(R.id.itemLU);
            in_qty = (TextView) view.findViewById(R.id.in_qty);
            price = (TextView) view.findViewById(R.id.price);
            checkBox = itemView.findViewById(R.id.checkBox_select);
        }
    }

    public ItemALDataAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemALDataAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /*
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_row, parent, false);

        return new PlayerViewHolder(itemView);
         */

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_al,parent,false);



        return new ItemALDataAdapter.ItemViewHolder(itemView);

        //return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemALDataAdapter.ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.descr.setText(item.getDescr());
        holder.itemLU.setText(item.getItemCode());
        //String sDate = item.getInQty();
        //Log.d("myTag ", "This is my date " + sDate);
        holder.in_qty.setText(item.getInQty());

        holder.price.setText(item.getPrice());
        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setTag(items.get(position));

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "Testing";
                Item item1 = (Item)holder.checkBox.getTag();

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
