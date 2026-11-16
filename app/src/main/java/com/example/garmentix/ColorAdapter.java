package com.example.garmentix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {
    ArrayList<item_Color> colorList;
    OnColorClickListener listener;
    int selectedPosition = -1;
    int selectedColor = Integer.MIN_VALUE;
    public interface OnColorClickListener{
        void onColorClick(item_Color color);
    }
    public void setSelectedColor(int color) {
        selectedColor = color;
        selectedPosition = -1;
        notifyDataSetChanged();
    }
    public ColorAdapter(ArrayList<item_Color> colorList,OnColorClickListener listener)
    {
        this.colorList = colorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorAdapter.ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color,parent,false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorAdapter.ColorViewHolder holder, int position) {
        item_Color color = colorList.get(position);
        holder.colorSwatch.setBackgroundColor(color.color);

        if (selectedPosition == position || color.color == selectedColor){
            holder.imgColorSelected.setVisibility(View.VISIBLE);
        }
        else {
            holder.imgColorSelected.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view->{
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onColorClick(color);
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorSwatch;
        android.widget.ImageView imgColorSelected;
        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorSwatch = itemView.findViewById(R.id.colorSwatch);
            imgColorSelected = itemView.findViewById(R.id.imgColorSelected);
        }
    }
}
