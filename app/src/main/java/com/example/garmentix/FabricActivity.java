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

public class FabricActivity extends AppCompatActivity {
    private final ArrayList<item_Fabric> allFabrics = new ArrayList<>();
    private final ArrayList<item_Fabric> visibleFabrics = new ArrayList<>();
    private FabricAdapter adapter;
    private String selectedName;
    private String activeCategory = "ALL";
    private TextView noItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabric);
        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());
        noItems = findViewById(R.id.txtNoFabrics);
        selectedName = getIntent().getStringExtra("selectedFabricName");
        RecyclerView recycler = findViewById(R.id.rvFabric);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        addFabrics();
        adapter = new FabricAdapter(visibleFabrics, fabric -> returnToEditor(fabric));
        adapter.setSelectedName(selectedName);
        recycler.setAdapter(adapter);
        setupFilters();
        filter("ALL");
    }

    private void addFabrics() {
        allFabrics.add(new item_Fabric(1, "Cotton", "Cotton 01", R.drawable.cotton));
        allFabrics.add(new item_Fabric(2, "Cotton", "Cotton 02", R.drawable.cotton));
        allFabrics.add(new item_Fabric(3, "Silk", "Silk 01", R.drawable.silk));
        allFabrics.add(new item_Fabric(4, "Silk", "Silk 02", R.drawable.silk));
        allFabrics.add(new item_Fabric(5, "Linen", "Linen 01", R.drawable.linen));
        allFabrics.add(new item_Fabric(6, "Linen", "Linen 02", R.drawable.linen));
        allFabrics.add(new item_Fabric(7, "Denim", "Denim 01", R.drawable.denim));
        allFabrics.add(new item_Fabric(8, "Denim", "Denim 02", R.drawable.denim));
        allFabrics.add(new item_Fabric(9, "Satin", "Satin 01", R.drawable.satin));
        allFabrics.add(new item_Fabric(10, "Satin", "Satin 02", R.drawable.satin));
        allFabrics.add(new item_Fabric(11, "Velvet", "Velvet 01", R.drawable.velvet));
        allFabrics.add(new item_Fabric(12, "Velvet", "Velvet 02", R.drawable.velvet));
        allFabrics.add(new item_Fabric(13, "Rayon", "Rayon 01", R.drawable.rayon));
        allFabrics.add(new item_Fabric(14, "Rayon", "Rayon 02", R.drawable.rayon));
        allFabrics.add(new item_Fabric(15, "Georgette", "Georgette", R.drawable.ic_fabric, false));
        allFabrics.add(new item_Fabric(16, "Chiffon", "Chiffon", R.drawable.ic_fabric, false));
    }

    private void setupFilters() {
        bindFilter(R.id.categoryAll, "ALL");
        bindFilter(R.id.categoryCotton, "Cotton");
        bindFilter(R.id.categorySilk, "Silk");
        bindFilter(R.id.categoryLinen, "Linen");
        bindFilter(R.id.categoryDenim, "Denim");
        bindFilter(R.id.categorySatin, "Satin");
        bindFilter(R.id.categoryVelvet, "Velvet");
        bindFilter(R.id.categoryRayon, "Rayon");
        bindFilter(R.id.categoryGeorgette, "Georgette");
        bindFilter(R.id.categoryChiffon, "Chiffon");
    }

    private void bindFilter(int id, String category) {
        findViewById(id).setOnClickListener(v -> filter(category));
    }

    private void filter(String category) {
        activeCategory = category;
        visibleFabrics.clear();
        for (item_Fabric fabric : allFabrics) {
            if ("ALL".equals(category) || category.equals(fabric.category)) {
                visibleFabrics.add(fabric);
            }
        }
        adapter.notifyDataSetChanged();
        noItems.setVisibility(visibleFabrics.isEmpty() ? View.VISIBLE : View.GONE);
        int[] ids = {R.id.categoryAll, R.id.categoryCotton, R.id.categorySilk, R.id.categoryLinen,
                R.id.categoryDenim, R.id.categorySatin, R.id.categoryVelvet, R.id.categoryRayon,
                R.id.categoryGeorgette, R.id.categoryChiffon};
        String[] names = {"ALL", "Cotton", "Silk", "Linen", "Denim", "Satin", "Velvet",
                "Rayon", "Georgette", "Chiffon"};
        for (int i = 0; i < ids.length; i++) {
            TextView tab = findViewById(ids[i]);
            boolean selected = names[i].equals(activeCategory);
            tab.setBackgroundColor(selected ? 0xFF111827 : 0x00000000);
            tab.setTextColor(selected ? 0xFFFFFFFF : 0xFF374151);
        }
        if ("Georgette".equals(category) || "Chiffon".equals(category)) {
            Toast.makeText(this, "Fabric texture artwork is not included; this choice is saved as a specification.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void returnToEditor(item_Fabric fabric) {
        Intent intent = new Intent(this, DesignScreenActivity.class);
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        if (fabric != null) {
            intent.putExtra("selectedFabricName", fabric.name);
            intent.putExtra("selectedFabricImage", fabric.image);
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
