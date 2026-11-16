package com.example.garmentix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FabricAdapter extends RecyclerView.Adapter<FabricAdapter.FabricViewHolder> {

    ArrayList<item_Fabric> fabricList;
    OnFabricClickListener listener;
    private String selectedName;

     public interface OnFabricClickListener{
         void onFabricClick(item_Fabric fabric);
     }
     public FabricAdapter(ArrayList<item_Fabric> fabricList, OnFabricClickListener listener){
         this.fabricList = fabricList;
         this.listener = listener;
     }
     public void setSelectedName(String name) {
         selectedName = name;
         notifyDataSetChanged();
     }

    @NonNull
    @Override
    public FabricAdapter.FabricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fabric,parent,false);
        return new FabricViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FabricAdapter.FabricViewHolder holder, int position) {
         item_Fabric fabric = fabricList.get(position);
         holder.imgFabric.setImageResource(fabric.image);
         holder.txtName.setText(fabric.textureAvailable
                 ? fabric.name : fabric.name + " · texture image needed");
         holder.itemView.setBackgroundResource(fabric.name.equals(selectedName)
                 ? R.drawable.bg_icon_box : R.drawable.bg_input_field);
         holder.itemView.setOnClickListener(view ->{
             listener.onFabricClick(fabric);
         });
    }

    @Override
    public int getItemCount() {
        return fabricList.size();
    }

    public static class FabricViewHolder extends RecyclerView.ViewHolder{
        ImageView imgFabric;
        TextView txtName;

        public FabricViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFabric = itemView.findViewById(R.id.imgFabric);
            txtName = itemView.findViewById(R.id.txtFabricName);
        }
    }
}
