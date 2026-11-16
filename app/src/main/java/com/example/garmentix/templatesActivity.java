package com.example.garmentix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class templatesActivity extends AppCompatActivity {
    private final ArrayList<item_Template> allTemplates = new ArrayList<>();
    private final ArrayList<item_Template> filteredTemplates = new ArrayList<>();
    private TemplateAdapter adapter;
    private TextView noTemplates;
    private String selectedCategory = "ALL";
    private String selectedName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates);
        ImageView back = findViewById(R.id.btnBack);
        RecyclerView recycler = findViewById(R.id.rvTemplates);
        noTemplates = findViewById(R.id.txtNoTemplates);
        back.setOnClickListener(v -> finish());
        Intent incoming = getIntent();
        selectedName = incoming.getStringExtra("selectedTemplateName");
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        addTemplates();
        adapter = new TemplateAdapter(filteredTemplates, this::selectTemplate);
        adapter.setSelectedName(selectedName);
        recycler.setAdapter(adapter);
        setupCategoryFilters();
        filterBy("ALL");
    }

    private void addTemplates() {
        allTemplates.add(new item_Template(1, "Kurti", "Kurti 01", R.drawable.kurti_01));
        allTemplates.add(new item_Template(2, "Kurti", "Kurti 02", R.drawable.kurti_02));
        allTemplates.add(new item_Template(3, "Gown", "Gown 01", R.drawable.gown_01));
        allTemplates.add(new item_Template(4, "Gown", "Gown 02", R.drawable.gown_02));
        allTemplates.add(new item_Template(5, "Top", "Top 01", R.drawable.top_01));
        allTemplates.add(new item_Template(6, "Top", "Top 02", R.drawable.top_02));
        allTemplates.add(new item_Template(7, "Dress", "Dress 01", R.drawable.ic_cloth_sample));
        allTemplates.add(new item_Template(8, "Dress", "Dress 02", R.drawable.dress_02));
        allTemplates.add(new item_Template(9, "Shirt", "Shirt 01", R.drawable.shirt_01));
        allTemplates.add(new item_Template(10, "Shirt", "Shirt 02", R.drawable.shirt_02));
    }

    private void setupCategoryFilters() {
        bindFilter(R.id.tabAll, "ALL");
        bindFilter(R.id.tabKurti, "Kurti");
        bindFilter(R.id.tabGrown, "Gown");
        bindFilter(R.id.tabTop, "Top");
        bindFilter(R.id.tabDress, "Dress");
        bindFilter(R.id.tabLehenga, "Lehenga");
        bindFilter(R.id.tabShirt, "Shirt");
    }

    private void bindFilter(int viewId, String category) {
        TextView tab = findViewById(viewId);
        tab.setOnClickListener(v -> filterBy(category));
    }

    private void filterBy(String category) {
        selectedCategory = category;
        filteredTemplates.clear();
        for (item_Template template : allTemplates) {
            if ("ALL".equals(category) || category.equals(template.category)) {
                filteredTemplates.add(template);
            }
        }
        adapter.setTemplates(filteredTemplates);
        updateSelectedFilter();
        noTemplates.setVisibility(filteredTemplates.isEmpty() ? View.VISIBLE : View.GONE);
        if ("Lehenga".equals(category)) {
            Toast.makeText(this, "Lehenga template artwork has not been added yet.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateSelectedFilter() {
        int[] tabIds = {R.id.tabAll, R.id.tabKurti, R.id.tabGrown, R.id.tabTop,
                R.id.tabDress, R.id.tabLehenga, R.id.tabShirt};
        String[] categories = {"ALL", "Kurti", "Gown", "Top", "Dress", "Lehenga", "Shirt"};
        for (int i = 0; i < tabIds.length; i++) {
            TextView tab = findViewById(tabIds[i]);
            boolean selected = categories[i].equals(selectedCategory);
            tab.setBackgroundResource(selected ? R.drawable.bg_gold_button : R.drawable.bg_input_field);
            tab.setTextColor(selected ? 0xFFFFFFFF : 0xFF111827);
        }
    }

    private void selectTemplate(item_Template template) {
        Intent intent = new Intent(this, DesignScreenActivity.class);
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        intent.putExtra("selectedTemplateImage", template.image);
        intent.putExtra("selectedTemplateName", template.name);
        intent.putExtra("selectedCategory", template.category);
        setResult(RESULT_OK, intent);
        finish();
    }
}
