package com.example.garmentix;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class MyDesignAdapter extends RecyclerView.Adapter<MyDesignAdapter.MyDesignViewHolder> {
    interface OnDesignActionListener {
        void onAction(item_MyDesign design, String action);
    }

    private final ArrayList<item_MyDesign> designList;
    private final OnDesignActionListener actionListener;

    public MyDesignAdapter(ArrayList<item_MyDesign> designList,
                           OnDesignActionListener actionListener) {
        this.designList = designList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public MyDesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_design, parent, false);
        return new MyDesignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDesignViewHolder holder, int position) {
        item_MyDesign design = designList.get(position);
        holder.txtDesignName.setText(design.name);
        holder.txtDesignDate.setText(design.date);
        if (design.previewPath != null && new File(design.previewPath).isFile()) {
            holder.imgDesign.setImageBitmap(BitmapFactory.decodeFile(design.previewPath));
        } else {
            holder.imgDesign.setImageResource(design.image != 0
                    ? design.image : R.drawable.ic_cloth_sample);
        }
        holder.btnMore.setOnClickListener(v -> actionListener.onAction(design, "menu"));
        holder.itemView.setOnClickListener(v -> actionListener.onAction(design, "open"));
    }

    @Override
    public int getItemCount() {
        return designList.size();
    }

    static class MyDesignViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgDesign;
        final TextView txtDesignName;
        final TextView txtDesignDate;
        final TextView btnMore;

        MyDesignViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDesign = itemView.findViewById(R.id.imgDesign);
            txtDesignName = itemView.findViewById(R.id.txtDesignName);
            txtDesignDate = itemView.findViewById(R.id.txtDesignDate);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
