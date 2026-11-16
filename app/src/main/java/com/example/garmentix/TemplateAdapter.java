package com.example.garmentix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

    ArrayList<item_Template> templateList;
    OnTemplateClickListener listener;
    private String selectedName;

    public interface  OnTemplateClickListener{
        void onTemplateClick(item_Template template);
    }

    public TemplateAdapter(ArrayList<item_Template> templateList,OnTemplateClickListener listener){
        this.templateList = templateList;
        this.listener = listener;
    }

    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
        notifyDataSetChanged();
    }

    public void setTemplates(ArrayList<item_Template> templates) {
        templateList = templates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template,parent,false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {

        item_Template template = templateList.get(position);
        holder.imgTemplates.setImageResource(template.image);
        holder.txtTemplateName.setText(template.name);
        holder.txtTemplateCategory.setText(template.category);
        holder.itemView.setBackgroundResource(template.name.equals(selectedName)
                ? R.drawable.bg_icon_box : R.drawable.bg_input_field);

        holder.itemView.setOnClickListener(view->{
            listener.onTemplateClick(template);
        });
    }


    @Override
    public int getItemCount() {
        return templateList.size();
    }

    public static class TemplateViewHolder extends RecyclerView.ViewHolder{

        ImageView imgTemplates;
        TextView txtTemplateName;
        TextView txtTemplateCategory;

        public TemplateViewHolder(@NonNull View itemview){
            super(itemview);

            imgTemplates = itemview.findViewById(R.id.imgTemplates);
            txtTemplateName = itemview.findViewById(R.id.txtTemplateName);
            txtTemplateCategory = itemview.findViewById(R.id.txtTemplateCategory);
        }
    }

}
