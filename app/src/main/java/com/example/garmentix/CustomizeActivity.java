package com.example.garmentix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomizeActivity extends AppCompatActivity {
    private final Map<String, String> selections = new LinkedHashMap<>();
    private final String[] categories = {
            "Sleeve", "Neckline", "Pocket", "Dress Length", "Fit Style",
            "Cuff", "Button", "Pattern", "Waist"
    };
    private final String[][] options = {
            {"Full Sleeve", "Half Sleeve", "Sleeveless", "Puff Sleeve", "Bell Sleeve", "Cap Sleeve", "Three-Quarter Sleeve"},
            {"Round Neck", "V-Neck", "Square Neck", "Boat Neck", "Collar Neck", "Sweetheart Neck"},
            {"No Pocket", "Single Pocket", "Double Pocket"},
            {"Short", "Knee Length", "Midi", "Full Length"},
            {"Regular", "Slim", "Loose"},
            {"Regular", "Button Cuff", "Elastic Cuff"},
            {"Simple", "Decorative"},
            {"Plain", "Floral", "Striped", "Printed"},
            {"Regular", "Fitted", "Flared"}
    };
    private LinearLayout categoryContainer;
    private LinearLayout optionContainer;
    private int activeCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        categoryContainer = findViewById(R.id.customizationCategories);
        optionContainer = findViewById(R.id.customizationOptions);
        readSelections();
        findViewById(R.id.btnBack).setOnClickListener(v -> returnToEditor());
        findViewById(R.id.btnApplyCustomization).setOnClickListener(v -> returnToEditor());
        renderCategories();
        renderOptions();
    }

    private void readSelections() {
        Intent intent = getIntent();
        selections.put("Sleeve", intent.getStringExtra("selectedSleeve") == null
                ? "Full Sleeve" : intent.getStringExtra("selectedSleeve"));
        String neckline = intent.getStringExtra("selectedNeckline");
        if (neckline == null) neckline = intent.getStringExtra("selectedCollar");
        selections.put("Neckline", neckline == null ? "Round Neck" : neckline);
        selections.put("Pocket", intent.getStringExtra("selectedPocket") == null
                ? "No Pocket" : intent.getStringExtra("selectedPocket"));
        selections.put("Dress Length", value(intent, "selectedLength", "Knee Length"));
        selections.put("Fit Style", value(intent, "selectedFit", "Regular"));
        selections.put("Cuff", value(intent, "selectedCuff", "Regular"));
        selections.put("Button", value(intent, "selectedButton", "Simple"));
        selections.put("Pattern", value(intent, "selectedPattern", "Plain"));
        selections.put("Waist", value(intent, "selectedWaist", "Regular"));
    }

    private String value(Intent intent, String key, String fallback) {
        String value = intent.getStringExtra(key);
        return value == null ? fallback : value;
    }

    private void renderCategories() {
        categoryContainer.removeAllViews();
        for (int i = 0; i < categories.length; i++) {
            final int index = i;
            TextView chip = chip(categories[i], i == activeCategory);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, dp(8), dp(8));
            categoryContainer.addView(chip, params);
            chip.setOnClickListener(v -> {
                activeCategory = index;
                renderCategories();
                renderOptions();
            });
        }
    }

    private void renderOptions() {
        optionContainer.removeAllViews();
        TextView heading = new TextView(this);
        heading.setText("Choose " + categories[activeCategory]);
        heading.setTextColor(Color.rgb(17, 24, 39));
        heading.setTextSize(17);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        optionContainer.addView(heading);
        for (String option : options[activeCategory]) {
            TextView choice = chip(option, option.equals(selections.get(categories[activeCategory])));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
            params.setMargins(0, dp(8), 0, 0);
            optionContainer.addView(choice, params);
            choice.setOnClickListener(v -> {
                selections.put(categories[activeCategory], option);
                renderOptions();
            });
        }
    }

    private TextView chip(String label, boolean selected) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(14);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(16), dp(10), dp(16), dp(10));
        view.setTextColor(selected ? Color.WHITE : Color.rgb(17, 24, 39));
        view.setBackgroundResource(selected ? R.drawable.bg_gold_button : R.drawable.bg_input_field);
        return view;
    }

    private void returnToEditor() {
        Intent intent = new Intent(this, DesignScreenActivity.class);
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        intent.putExtra("selectedSleeve", selections.get("Sleeve"));
        intent.putExtra("selectedNeckline", selections.get("Neckline"));
        intent.putExtra("selectedCollar", selections.get("Neckline"));
        intent.putExtra("selectedPocket", selections.get("Pocket"));
        intent.putExtra("selectedLength", selections.get("Dress Length"));
        intent.putExtra("selectedFit", selections.get("Fit Style"));
        intent.putExtra("selectedCuff", selections.get("Cuff"));
        intent.putExtra("selectedButton", selections.get("Button"));
        intent.putExtra("selectedPattern", selections.get("Pattern"));
        intent.putExtra("selectedWaist", selections.get("Waist"));
        setResult(RESULT_OK, intent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
