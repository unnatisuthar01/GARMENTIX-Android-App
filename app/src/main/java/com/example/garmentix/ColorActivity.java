package com.example.garmentix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class ColorActivity extends AppCompatActivity {

    ImageView btnBack;
    RecyclerView rvRecentColors;
    RecyclerView rvBasicColors;
    RecyclerView rvPremiumColors;

    ArrayList<item_Color> recentColorList;
    ArrayList<item_Color> basicColorList;
    ArrayList<item_Color> premiumColorList;

    ColorAdapter recentColorAdapter;
    ColorAdapter basicColorAdapter;
    ColorAdapter premiumColorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_color);

        btnBack = findViewById(R.id.btnBack);
        rvRecentColors = findViewById(R.id.rvRecentColors);
        rvBasicColors = findViewById(R.id.rvBasicColors);
        rvPremiumColors = findViewById(R.id.rvPremiumColors);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack.setOnClickListener(view -> {
            finish();
        });

        recentColorList = new ArrayList<>();
        android.content.SharedPreferences preferences =
                getSharedPreferences("GARMENTIX_PREF", MODE_PRIVATE);
        for (int i = 0; i < 5; i++) {
            if (preferences.contains("recentColorValue" + i)) {
                recentColorList.add(new item_Color(i, preferences.getString(
                        "recentColorName" + i, "Recent color"),
                        preferences.getInt("recentColorValue" + i, Color.BLACK)));
            }
        }
        if (recentColorList.isEmpty()) {
            recentColorList.add(new item_Color(1, "Dark Gray", Color.rgb(55, 65, 81)));
            recentColorList.add(new item_Color(2, "Red", Color.RED));
            recentColorList.add(new item_Color(3, "Light Pink", Color.rgb(244, 180, 190)));
            recentColorList.add(new item_Color(4, "Green", Color.rgb(34, 197, 94)));
            recentColorList.add(new item_Color(5, "Blue", Color.rgb(37, 99, 235)));
            android.content.SharedPreferences.Editor recentEditor = preferences.edit();
            for (int i = 0; i < recentColorList.size(); i++) {
                item_Color recent = recentColorList.get(i);
                recentEditor.putInt("recentColorValue" + i, recent.color)
                        .putString("recentColorName" + i, recent.name);
            }
            recentEditor.apply();
        }

        basicColorList = new ArrayList<>();
        basicColorList.add(new item_Color(101, "Red", Color.rgb(239, 68, 68)));
        basicColorList.add(new item_Color(102, "Light Red", Color.rgb(252, 165, 164)));
        basicColorList.add(new item_Color(103, "Orange", Color.rgb(249, 115, 22)));
        basicColorList.add(new item_Color(104, "Yellow", Color.rgb(250, 204, 21)));
        basicColorList.add(new item_Color(105, "Green", Color.rgb(34, 197, 94)));
        basicColorList.add(new item_Color(106, "Blue", Color.rgb(59, 130, 246)));
        basicColorList.add(new item_Color(107, "Dark Blue", Color.rgb(30, 64, 175)));
        basicColorList.add(new item_Color(108, "Pink", Color.rgb(236, 72, 153)));
        basicColorList.add(new item_Color(109, "Peach", Color.rgb(251, 146, 120)));
        basicColorList.add(new item_Color(110, "Gold", Color.rgb(234, 179, 8)));
        basicColorList.add(new item_Color(111, "Cyan", Color.rgb(6, 182, 212)));
        basicColorList.add(new item_Color(112, "Purple", Color.rgb(147, 51, 234)));

        premiumColorList = new ArrayList<>();
        premiumColorList.add(new item_Color(201, "Gold", Color.rgb(202, 138, 4)));
        premiumColorList.add(new item_Color(202, "Brown Gold", Color.rgb(146, 104, 50)));
        premiumColorList.add(new item_Color(203, "Amethyst", Color.rgb(126, 34, 206)));
        premiumColorList.add(new item_Color(204, "Plum", Color.rgb(88, 28, 135)));
        premiumColorList.add(new item_Color(205, "Teal", Color.rgb(13, 148, 136)));

        recentColorAdapter = new ColorAdapter(recentColorList, this::openDesignScreen);
        basicColorAdapter = new ColorAdapter(basicColorList, this::openDesignScreen);
        premiumColorAdapter = new ColorAdapter(premiumColorList, this::openDesignScreen);
        int selectedColor = getIntent().getIntExtra("selectedColorValue", Integer.MIN_VALUE);
        recentColorAdapter.setSelectedColor(selectedColor);
        basicColorAdapter.setSelectedColor(selectedColor);
        premiumColorAdapter.setSelectedColor(selectedColor);

        rvRecentColors.setLayoutManager(new GridLayoutManager(ColorActivity.this, 5));
        rvRecentColors.setAdapter(recentColorAdapter);

        rvBasicColors.setLayoutManager(new GridLayoutManager(ColorActivity.this, 6));
        rvBasicColors.setAdapter(basicColorAdapter);

        rvPremiumColors.setLayoutManager(new GridLayoutManager(ColorActivity.this, 5));
        rvPremiumColors.setAdapter(premiumColorAdapter);
        findViewById(R.id.btnCustomColor).setOnClickListener(v -> showCustomColorPicker());
    }

    private void showCustomColorPicker() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("#D4AF37");
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Custom color")
                .setMessage("Enter a hexadecimal color such as #B23854")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                String name = input.getText().toString().trim();
                int value = Color.parseColor(name);
                dialog.dismiss();
                openDesignScreen(new item_Color(0, name, value));
            } catch (IllegalArgumentException e) {
                input.setError("Enter a valid color such as #B23854");
            }
        });
    }

    private void openDesignScreen(item_Color color) {
        Intent intent = new Intent(ColorActivity.this, DesignScreenActivity.class);
        copyAllIntentExtras(intent);

        String selectedPart = "BODY";
        Intent currentIntent = getIntent();
        if (currentIntent != null && currentIntent.hasExtra("selectedPart")) {
            selectedPart = currentIntent.getStringExtra("selectedPart");
        }

        if (selectedPart == null) {
            selectedPart = "BODY";
        }

        intent.putExtra("selectedPart", selectedPart);
        intent.putExtra("color_" + selectedPart, color.color);
        intent.putExtra("selectedColorName", color.name);
        intent.putExtra("selectedColorValue", color.color);
        android.content.SharedPreferences preferences =
                getSharedPreferences("GARMENTIX_PREF", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = preferences.edit();
        for (int i = 4; i > 0; i--) {
            String previousValueKey = "recentColorValue" + (i - 1);
            String previousNameKey = "recentColorName" + (i - 1);
            if (preferences.contains(previousValueKey)) {
                editor.putInt("recentColorValue" + i, preferences.getInt(previousValueKey, color.color));
                editor.putString("recentColorName" + i, preferences.getString(previousNameKey, color.name));
            } else {
                editor.remove("recentColorValue" + i).remove("recentColorName" + i);
            }
        }
        editor.putInt("recentColorValue0", color.color)
                .putString("recentColorName0", color.name)
                .apply();

        setResult(RESULT_OK, intent);
        finish();
    }

    private void copyAllIntentExtras(Intent targetIntent) {
        Intent currentIntent = getIntent();
        if (currentIntent == null) return;

        if (currentIntent.getExtras() != null) {
            targetIntent.putExtras(currentIntent.getExtras());
        }
    }
}