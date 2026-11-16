package com.example.garmentix;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyDesignActivity extends AppCompatActivity {
    private final ArrayList<item_MyDesign> designs = new ArrayList<>();
    private DatabaseHelper database;
    private String email;
    private boolean favoritesOnly;
    private boolean exportRequested;
    private MyDesignAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_design);
        database = new DatabaseHelper(this);
        email = getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                .getString("userEmail", "");
        favoritesOnly = getIntent().getBooleanExtra("favoritesOnly", false);
        exportRequested = getIntent().getBooleanExtra("exportPdf", false);

        ImageView back = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvMyDesigns);
        emptyView = findViewById(R.id.txtEmptyDesigns);
        TextView title = findViewById(R.id.txtMyDesignTitle);
        title.setText(favoritesOnly ? "Saved Designs"
                : exportRequested ? "Choose Design to Export" : "My Designs");
        back.setOnClickListener(v -> finish());
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MyDesignAdapter(designs, this::showDesignActions);
        recyclerView.setAdapter(adapter);
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        migrateLegacyLocalDesign();
        loadDesigns();
    }

    private void migrateLegacyLocalDesign() {
        if (email.isEmpty()) return;
        android.content.SharedPreferences preferences =
                getSharedPreferences("GARMENTIX_PREF", MODE_PRIVATE);
        String migrationKey = "legacyDesignMigrated";
        String legacyOwnerEmail = preferences.getString("email", "");
        if (database.getUserCount() != 1 || legacyOwnerEmail.isEmpty()
                || !legacyOwnerEmail.equalsIgnoreCase(email)) return;
        if (!preferences.contains("saved_template_image")
                || preferences.getBoolean(migrationKey, false)) return;
        try {
            org.json.JSONObject data = new org.json.JSONObject();
            String template = preferences.getString("saved_template_name", "Kurti 01");
            data.put("template", template);
            data.put("category", getCategory(template));
            data.put("templateImage", preferences.getInt("saved_template_image", R.drawable.kurti_01));
            data.put("fabric", preferences.getString("saved_fabric_name", ""));
            data.put("fabricImage", preferences.getInt("saved_fabric_image", 0));
            data.put("colorName", preferences.getString("saved_color_name", ""));
            data.put("colorValue", preferences.getInt("saved_color_value", 0));
            data.put("hasColor", preferences.getBoolean("saved_has_color_value", false));
            data.put("color_BODY", preferences.getInt("saved_color_BODY", 0));
            data.put("color_SLEEVE_LEFT", preferences.getInt("saved_color_SLEEVE_LEFT", 0));
            data.put("color_SLEEVE_RIGHT", preferences.getInt("saved_color_SLEEVE_RIGHT", 0));
            data.put("color_NECKLINE", preferences.getInt("saved_color_NECKLINE", 0));
            data.put("color_CUFFS", preferences.getInt("saved_color_CUFFS", 0));
            data.put("sleeve", preferences.getString("saved_sleeve", ""));
            data.put("neckline", preferences.getString("saved_collar", ""));
            data.put("pocket", preferences.getString("saved_pocket", ""));
            data.put("text", preferences.getString("saved_text", ""));
            data.put("textColor", preferences.getInt("saved_text_color", 0xFFD4AF37));
            data.put("textSize", preferences.getFloat("saved_text_size", 18f));
            data.put("textRotation", preferences.getFloat("saved_text_rotation", 0f));
            data.put("textX", preferences.getFloat("saved_text_x", 0f));
            data.put("textY", preferences.getFloat("saved_text_y", 0f));
            data.put("logoImage", preferences.getInt("saved_logo_image", 0));
            data.put("logoX", preferences.getFloat("saved_logo_x", 0f));
            data.put("logoY", preferences.getFloat("saved_logo_y", 0f));
            long id = database.saveDesign(email, template, data, null, false);
            if (id > 0) preferences.edit().putBoolean(migrationKey, true).apply();
            else showMessage("Unable to migrate your previous saved design.");
        } catch (org.json.JSONException e) {
            showMessage("Unable to migrate your previous saved design.");
        }
    }

    private String getCategory(String templateName) {
        if (templateName == null || templateName.trim().isEmpty()) return "Garment";
        return templateName.trim().split("\\s+")[0];
    }

    private void loadDesigns() {
        designs.clear();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please log in to view your designs", Toast.LENGTH_SHORT).show();
        } else {
            try (Cursor cursor = database.getDesigns(email, favoritesOnly)) {
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndexOrThrow("design_data"));
                    int image = 0;
                    try {
                        if (data != null) {
                            image = new org.json.JSONObject(data).optInt("templateImage", 0);
                        }
                    } catch (org.json.JSONException ignored) {
                        // Older design records may not include JSON metadata.
                    }
                    designs.add(new item_MyDesign(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("design_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                            image,
                            cursor.getString(cursor.getColumnIndexOrThrow("preview_path")),
                            data,
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) != 0
                    ));
                }
            }
        }
        adapter.notifyDataSetChanged();
        boolean hasDesigns = !designs.isEmpty();
        recyclerView.setVisibility(hasDesigns ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(hasDesigns ? View.GONE : View.VISIBLE);
        emptyView.setText(favoritesOnly
                ? "No saved designs yet.\nFavorite a design to find it here."
                : "No designs yet.\nStart designing a garment to see it here.");
    }

    private void showDesignActions(item_MyDesign design, String action) {
        if ("open".equals(action)) {
            if (exportRequested) {
                PdfExportHelper.export(this, design);
            } else {
                Intent edit = new Intent(this, DesignScreenActivity.class);
                edit.putExtra("editDesignId", design.id);
                edit.putExtra("designData", design.designData);
                edit.putExtra("designName", design.name);
                edit.putExtra("isFavorite", design.favorite);
                startActivity(edit);
            }
            return;
        }
        String favoriteAction = design.favorite ? "Remove from Favorites" : "Add to Favorites";
        String[] actions = {"Edit Design", "Rename", "Duplicate", favoriteAction,
                "Delete Design", "Export PDF"};
        new AlertDialog.Builder(this)
                .setTitle(design.name)
                .setItems(actions, (dialog, choice) -> {
                    switch (choice) {
                        case 0:
                            Intent edit = new Intent(this, DesignScreenActivity.class);
                            edit.putExtra("editDesignId", design.id);
                            edit.putExtra("designData", design.designData);
                            edit.putExtra("designName", design.name);
                            edit.putExtra("isFavorite", design.favorite);
                            startActivity(edit);
                            break;
                        case 1:
                            renameDesign(design);
                            break;
                        case 2:
                            long copyId = database.duplicateDesign(
                                    design.id, email, design.name + " copy");
                            if (copyId > 0) loadDesigns();
                            else showMessage("Could not duplicate this design.");
                            break;
                        case 3:
                            if (database.setDesignFavorite(design.id, email, !design.favorite)) {
                                loadDesigns();
                            } else showMessage("Could not update favorite.");
                            break;
                        case 4:
                            new AlertDialog.Builder(this)
                                    .setTitle("Delete design?")
                                    .setMessage("This permanently removes the saved design.")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Delete", (d, w) -> {
                                        if (database.deleteDesign(design.id, email)) loadDesigns();
                                        else showMessage("Could not delete this design.");
                                    }).show();
                            break;
                        case 5:
                            PdfExportHelper.export(this, design);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void renameDesign(item_MyDesign design) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(design.name);
        new AlertDialog.Builder(this)
                .setTitle("Rename design")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        showMessage("Design name cannot be empty.");
                    } else if (database.renameDesign(design.id, email, name)) {
                        loadDesigns();
                    } else {
                        showMessage("Could not rename this design.");
                    }
                }).show();
    }

    private void setupNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navDesign = findViewById(R.id.navDesign);
        LinearLayout navMyDesign = findViewById(R.id.navMyDesign);
        LinearLayout navProfile = findViewById(R.id.navProfile);
        navHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        navDesign.setOnClickListener(v -> startActivity(new Intent(this, DesignScreenActivity.class)));
        navMyDesign.setOnClickListener(v -> {
            if (favoritesOnly) {
                startActivity(new Intent(this, MyDesignActivity.class));
            } else {
                loadDesigns();
            }
        });
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileSettingActivity.class)));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
