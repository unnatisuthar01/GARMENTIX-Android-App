package com.example.garmentix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class DesignScreenActivity extends AppCompatActivity {

    ImageView btnBack, btnFav, imgClothPreview, imgDesignLogo;
    TextView txtDesignText, txtTemplateTitle, txtSelectedPartInfo;
    FrameLayout designPreview;

    FrameLayout layoutKurti01Layers;
    ImageView imgKurtiBody, imgKurtiSleeveLeft, imgKurtiSleeveRight, imgKurtiNeckline, imgKurtiCuffs, imgKurtiShadows;

    LinearLayout btnTemplates, btnFabrics, btnColor;
    LinearLayout btnCustomize, btnTextLogo;
    LinearLayout btnUndo, btnRedo;

    Button btnNext;

    private int selectedTemplateImage = 0;
    private String selectedTemplateName = null;

    private int selectedFabricImage = 0;
    private String selectedFabricName = null;

    private String selectedColorName = null;
    private int selectedColorValue = 0;
    private boolean hasColorValue = false;

    private String selectedPart = "BODY";

    private int colorBody = 0;
    private int colorSleeveLeft = 0;
    private int colorSleeveRight = 0;
    private int colorNeckline = 0;
    private int colorCuffs = 0;

    private boolean isFavorite = false;

    private final List<DesignState> undoStack = new ArrayList<>();
    private final List<DesignState> redoStack = new ArrayList<>();

    private float downRawX, downRawY;
    private float initialX, initialY;

    private static class DesignState {
        float textX, textY, logoX, logoY;
        int textVisibility, logoVisibility;
        String text;

        DesignState(
                float textX,
                float textY,
                float logoX,
                float logoY,
                int textVisibility,
                int logoVisibility,
                String text
        ) {
            this.textX = textX;
            this.textY = textY;
            this.logoX = logoX;
            this.logoY = logoY;
            this.textVisibility = textVisibility;
            this.logoVisibility = logoVisibility;
            this.text = text;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_design_screen);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );
                    return insets;
                }
        );

        initializeViews();
        setupLayerClickListeners();
        readIntentData();
        applyInitialPreview();

        btnBack.setOnClickListener(v -> finish());

        btnTemplates.setOnClickListener(v -> openScreen(templatesActivity.class));
        btnFabrics.setOnClickListener(v -> openScreen(FabricActivity.class));
        btnColor.setOnClickListener(v -> openScreen(ColorActivity.class));
        btnCustomize.setOnClickListener(v -> openScreen(CustomizeActivity.class));
        btnTextLogo.setOnClickListener(v -> openScreen(TextLogoActivity.class));

        btnFav.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            btnFav.setImageResource(R.drawable.ic_favorite);
            showMessage(
                    isFavorite
                            ? "Added to favorites"
                            : "Removed from favorites"
            );
        });

        btnUndo.setOnClickListener(v -> undoDesign());
        btnRedo.setOnClickListener(v -> redoDesign());

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DesignScreenActivity.this,
                    MyDesignActivity.class
            );
            passDesignData(intent);
            intent.putExtra("designText", txtDesignText.getText().toString());
            intent.putExtra("designTextX", txtDesignText.getX());
            intent.putExtra("designTextY", txtDesignText.getY());
            intent.putExtra(
                    "logoVisible",
                    imgDesignLogo.getVisibility() == View.VISIBLE
            );
            startActivity(intent);
        });

        // Drag text on the garment preview
        txtDesignText.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    saveUndoState();
                    downRawX = event.getRawX();
                    downRawY = event.getRawY();
                    initialX = view.getX();
                    initialY = view.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    view.setX(initialX + event.getRawX() - downRawX);
                    view.setY(initialY + event.getRawY() - downRawY);
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;
            }
            return false;
        });

        // Drag logo on the garment preview
        imgDesignLogo.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    saveUndoState();
                    downRawX = event.getRawX();
                    downRawY = event.getRawY();
                    initialX = view.getX();
                    initialY = view.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    view.setX(initialX + event.getRawX() - downRawX);
                    view.setY(initialY + event.getRawY() - downRawY);
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;
            }
            return false;
        });

        saveUndoState();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFav = findViewById(R.id.btnFav);

        designPreview = findViewById(R.id.designPreview);

        imgClothPreview = findViewById(R.id.imgClothPreview);
        txtDesignText = findViewById(R.id.txtDesignText);
        imgDesignLogo = findViewById(R.id.imgDesignLogo);

        txtTemplateTitle = findViewById(R.id.txtTemplateTitle);
        txtSelectedPartInfo = findViewById(R.id.txtSelectedPartInfo);

        layoutKurti01Layers = findViewById(R.id.layoutKurti01Layers);
        imgKurtiBody = findViewById(R.id.imgKurtiBody);
        imgKurtiSleeveLeft = findViewById(R.id.imgKurtiSleeveLeft);
        imgKurtiSleeveRight = findViewById(R.id.imgKurtiSleeveRight);
        imgKurtiNeckline = findViewById(R.id.imgKurtiNeckline);
        imgKurtiCuffs = findViewById(R.id.imgKurtiCuffs);
        imgKurtiShadows = findViewById(R.id.imgKurtiShadows);

        btnTemplates = findViewById(R.id.btnTemplates);
        btnFabrics = findViewById(R.id.btnFabrics);
        btnColor = findViewById(R.id.btnColor);
        btnCustomize = findViewById(R.id.btnCustomize);
        btnTextLogo = findViewById(R.id.btnTextLogo);

        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupLayerClickListeners() {
        if (imgKurtiBody != null) {
            imgKurtiBody.setOnClickListener(v -> setSelectedPart("BODY", "Body"));
        }
        if (imgKurtiSleeveLeft != null) {
            imgKurtiSleeveLeft.setOnClickListener(v -> setSelectedPart("SLEEVE_LEFT", "Left Sleeve"));
        }
        if (imgKurtiSleeveRight != null) {
            imgKurtiSleeveRight.setOnClickListener(v -> setSelectedPart("SLEEVE_RIGHT", "Right Sleeve"));
        }
        if (imgKurtiNeckline != null) {
            imgKurtiNeckline.setOnClickListener(v -> setSelectedPart("NECKLINE", "Neckline / Collar"));
        }
        if (imgKurtiCuffs != null) {
            imgKurtiCuffs.setOnClickListener(v -> setSelectedPart("CUFFS", "Cuffs"));
        }
    }

    private void setSelectedPart(String partKey, String displayName) {
        selectedPart = partKey;
        if (txtSelectedPartInfo != null) {
            txtSelectedPartInfo.setText("Selected Part: " + displayName);
        }
    }

    private String getPartDisplayName(String partKey) {
        if ("SLEEVE_LEFT".equals(partKey)) return "Left Sleeve";
        if ("SLEEVE_RIGHT".equals(partKey)) return "Right Sleeve";
        if ("NECKLINE".equals(partKey)) return "Neckline / Collar";
        if ("CUFFS".equals(partKey)) return "Cuffs";
        return "Body";
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        selectedTemplateImage = intent.getIntExtra("selectedTemplateImage", 0);
        selectedTemplateName = intent.getStringExtra("selectedTemplateName");
        selectedFabricImage = intent.getIntExtra("selectedFabricImage", 0);
        selectedFabricName = intent.getStringExtra("selectedFabricName");
        selectedColorName = intent.getStringExtra("selectedColorName");

        if (intent.hasExtra("selectedPart")) {
            selectedPart = intent.getStringExtra("selectedPart");
        }

        colorBody = intent.getIntExtra("color_BODY", 0);
        colorSleeveLeft = intent.getIntExtra("color_SLEEVE_LEFT", 0);
        colorSleeveRight = intent.getIntExtra("color_SLEEVE_RIGHT", 0);
        colorNeckline = intent.getIntExtra("color_NECKLINE", 0);
        colorCuffs = intent.getIntExtra("color_CUFFS", 0);

        if (intent.hasExtra("selectedColorValue")) {
            selectedColorValue = intent.getIntExtra("selectedColorValue", 0);
            hasColorValue = true;

            // Apply selected color value strictly to current active part
            if ("SLEEVE_LEFT".equals(selectedPart)) {
                colorSleeveLeft = selectedColorValue;
            } else if ("SLEEVE_RIGHT".equals(selectedPart)) {
                colorSleeveRight = selectedColorValue;
            } else if ("NECKLINE".equals(selectedPart)) {
                colorNeckline = selectedColorValue;
            } else if ("CUFFS".equals(selectedPart)) {
                colorCuffs = selectedColorValue;
            } else {
                colorBody = selectedColorValue;
            }
        }

        String text = intent.getStringExtra("designText");
        if (text != null && !text.trim().isEmpty()) {
            txtDesignText.setText(text);
            txtDesignText.setVisibility(View.VISIBLE);
        }

        int logoImage = intent.getIntExtra("designLogoImage", 0);
        if (logoImage != 0) {
            imgDesignLogo.setImageResource(logoImage);
            imgDesignLogo.setVisibility(View.VISIBLE);
        }
    }

    private void applyInitialPreview() {
        boolean isKurti01 = (selectedTemplateImage == 0 || selectedTemplateImage == R.drawable.kurti_01)
                && (selectedTemplateName == null || selectedTemplateName.trim().isEmpty() || selectedTemplateName.equalsIgnoreCase("kurti 01"));

        if (isKurti01 && layoutKurti01Layers != null) {
            layoutKurti01Layers.setVisibility(View.VISIBLE);
            imgClothPreview.setVisibility(View.GONE);

            imgKurtiBody.setImageResource(R.drawable.kurti_01_body);
            imgKurtiSleeveLeft.setImageResource(R.drawable.kurti_01_sleeve_left);
            imgKurtiSleeveRight.setImageResource(R.drawable.kurti_01_sleeve_right);
            imgKurtiNeckline.setImageResource(R.drawable.kurti_01_neckline);
            imgKurtiCuffs.setImageResource(R.drawable.kurti_01_cuffs);
            imgKurtiShadows.setImageResource(R.drawable.kurti_01_shadows);

            applyLayerColorFilter(imgKurtiBody, colorBody);
            applyLayerColorFilter(imgKurtiSleeveLeft, colorSleeveLeft);
            applyLayerColorFilter(imgKurtiSleeveRight, colorSleeveRight);
            applyLayerColorFilter(imgKurtiNeckline, colorNeckline);
            applyLayerColorFilter(imgKurtiCuffs, colorCuffs);

            if (txtTemplateTitle != null) {
                txtTemplateTitle.setText("Template: Kurti 01");
            }
            if (txtSelectedPartInfo != null) {
                txtSelectedPartInfo.setText("Selected Part: " + getPartDisplayName(selectedPart));
            }
        } else {
            if (layoutKurti01Layers != null) {
                layoutKurti01Layers.setVisibility(View.GONE);
            }
            imgClothPreview.setVisibility(View.VISIBLE);

            if (selectedTemplateImage == 0) {
                selectedTemplateImage = R.drawable.kurti_01;
            }
            imgClothPreview.setImageResource(selectedTemplateImage);

            if (txtTemplateTitle != null) {
                String title = (selectedTemplateName != null && !selectedTemplateName.trim().isEmpty())
                        ? selectedTemplateName
                        : "Kurti 01";
                txtTemplateTitle.setText("Template: " + title);
            }
            if (txtSelectedPartInfo != null) {
                txtSelectedPartInfo.setText("Selected Part: Whole Garment");
            }

            if (hasColorValue) {
                imgClothPreview.setColorFilter(selectedColorValue, PorterDuff.Mode.MULTIPLY);
            } else {
                imgClothPreview.clearColorFilter();
            }
        }
    }

    private void applyLayerColorFilter(ImageView view, int color) {
        if (view == null) return;
        if (color != 0) {
            view.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        } else {
            view.clearColorFilter();
        }
    }

    private void openScreen(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        passDesignData(intent);

        intent.putExtra("designText", txtDesignText.getText().toString());
        intent.putExtra("designLogoImage", getIntent().getIntExtra("designLogoImage", 0));

        startActivity(intent);
    }

    private void passDesignData(Intent intent) {
        intent.putExtra("selectedPart", selectedPart);
        intent.putExtra("color_BODY", colorBody);
        intent.putExtra("color_SLEEVE_LEFT", colorSleeveLeft);
        intent.putExtra("color_SLEEVE_RIGHT", colorSleeveRight);
        intent.putExtra("color_NECKLINE", colorNeckline);
        intent.putExtra("color_CUFFS", colorCuffs);

        if (selectedTemplateImage != 0) {
            intent.putExtra("selectedTemplateImage", selectedTemplateImage);
        }
        if (selectedTemplateName != null) {
            intent.putExtra("selectedTemplateName", selectedTemplateName);
        }
        if (selectedFabricImage != 0) {
            intent.putExtra("selectedFabricImage", selectedFabricImage);
        }
        if (selectedFabricName != null) {
            intent.putExtra("selectedFabricName", selectedFabricName);
        }
        if (selectedColorName != null) {
            intent.putExtra("selectedColorName", selectedColorName);
        }
        if (hasColorValue) {
            intent.putExtra("selectedColorValue", selectedColorValue);
        }
    }

    private void saveUndoState() {
        undoStack.add(captureState());
        redoStack.clear();
    }

    private DesignState captureState() {
        return new DesignState(
                txtDesignText.getX(),
                txtDesignText.getY(),
                imgDesignLogo.getX(),
                imgDesignLogo.getY(),
                txtDesignText.getVisibility(),
                imgDesignLogo.getVisibility(),
                txtDesignText.getText().toString()
        );
    }

    private void undoDesign() {
        if (undoStack.size() <= 1) {
            showMessage("Nothing to undo");
            return;
        }

        redoStack.add(captureState());
        undoStack.remove(undoStack.size() - 1);
        restoreState(undoStack.get(undoStack.size() - 1));
    }

    private void redoDesign() {
        if (redoStack.isEmpty()) {
            showMessage("Nothing to redo");
            return;
        }

        DesignState state = redoStack.remove(redoStack.size() - 1);
        undoStack.add(state);
        restoreState(state);
    }

    private void restoreState(DesignState state) {
        txtDesignText.setX(state.textX);
        txtDesignText.setY(state.textY);
        imgDesignLogo.setX(state.logoX);
        imgDesignLogo.setY(state.logoY);

        txtDesignText.setVisibility(state.textVisibility);
        imgDesignLogo.setVisibility(state.logoVisibility);
        txtDesignText.setText(state.text);
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}