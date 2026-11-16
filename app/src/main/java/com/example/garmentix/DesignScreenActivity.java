package com.example.garmentix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class DesignScreenActivity extends AppCompatActivity implements QuickPartSelectorFragment.OnPartColorSelectedListener {

    ImageView btnBack, btnFav, imgClothPreview, imgDesignLogo;
    TextView txtDesignText, txtTemplateTitle, txtSelectedPartInfo;
    TextView txtCustomizationPreview;
    FrameLayout designPreview;

    FrameLayout layoutKurti01Layers;
    ImageView imgKurtiBody, imgKurtiSleeveLeft, imgKurtiSleeveRight, imgKurtiNeckline, imgKurtiCuffs, imgKurtiShadows;

    LinearLayout btnTemplates, btnFabrics, btnColor;
    LinearLayout btnCustomize, btnTextLogo;
    LinearLayout btnUndo, btnRedo;
    TextView btnReset;

    Button btnNext;

    private QuickPartSelectorFragment quickFragment;

    private int selectedTemplateImage = 0;
    private String selectedTemplateName = null;
    private String selectedCategory = null;

    private int selectedFabricImage = 0;
    private String selectedFabricName = null;

    private String selectedColorName = null;
    private int selectedColorValue = 0;
    private boolean hasColorValue = false;

    private String selectedPart = "BODY";
    private String selectedSleeve;
    private String selectedCollar;
    private String selectedPocket;
    private String selectedNeckline;
    private String selectedCuff;
    private String selectedButton;
    private String selectedLength;
    private String selectedFit;
    private String selectedPattern;
    private String selectedWaist;
    private String logoPath;
    private String selectedFont = "Classic";
    private long editingDesignId = -1;

    private int colorBody = 0;
    private int colorSleeveLeft = 0;
    private int colorSleeveRight = 0;
    private int colorNeckline = 0;
    private int colorCuffs = 0;

    private boolean isFavorite = false;

    private final List<DesignState> undoStack = new ArrayList<>();
    private final List<DesignState> redoStack = new ArrayList<>();
    private ActivityResultLauncher<Intent> editorScreenLauncher;
    private DesignState pendingEditorState;

    private float downRawX, downRawY;
    private float initialX, initialY;

    private static class DesignState {
        float textX, textY, logoX, logoY;
        int textVisibility, logoVisibility;
        String text;
        String details;
        int colorBody, colorSleeveLeft, colorSleeveRight, colorNeckline, colorCuffs;

        DesignState(
                float textX,
                float textY,
                float logoX,
                float logoY,
                int textVisibility,
                int logoVisibility,
                String text,
                int colorBody,
                int colorSleeveLeft,
                int colorSleeveRight,
                int colorNeckline,
                int colorCuffs,
                String details
        ) {
            this.textX = textX;
            this.textY = textY;
            this.logoX = logoX;
            this.logoY = logoY;
            this.textVisibility = textVisibility;
            this.logoVisibility = logoVisibility;
            this.text = text;
            this.colorBody = colorBody;
            this.colorSleeveLeft = colorSleeveLeft;
            this.colorSleeveRight = colorSleeveRight;
            this.colorNeckline = colorNeckline;
            this.colorCuffs = colorCuffs;
            this.details = details;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_design_screen);
        editorScreenLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (pendingEditorState != null) {
                            if (undoStack.size() == 50) undoStack.remove(0);
                            undoStack.add(pendingEditorState);
                            redoStack.clear();
                        }
                        pendingEditorState = null;
                        result.getData().removeExtra("designData");
                        setIntent(result.getData());
                        readIntentData();
                        applyInitialPreview();
                    } else {
                        pendingEditorState = null;
                    }
                });

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
        setupQuickFragment();
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
            if (editingDesignId > 0) {
                String email = getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                        .getString("userEmail", "");
                new DatabaseHelper(this).setDesignFavorite(editingDesignId, email, isFavorite);
            }
            btnFav.setImageResource(R.drawable.ic_favorite);
            showMessage(
                    isFavorite
                            ? "Added to favorites"
                            : "Removed from favorites"
            );
        });

        btnUndo.setOnClickListener(v -> undoDesign());
        btnRedo.setOnClickListener(v -> redoDesign());
        btnReset.setOnClickListener(v -> confirmResetDesign());

        btnNext.setOnClickListener(v -> showSaveDesignDialog());

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
        txtCustomizationPreview = findViewById(R.id.txtCustomizationPreview);

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
        btnReset = findViewById(R.id.btnReset);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupQuickFragment() {
        if (findViewById(R.id.fragmentContainer) != null) {
            quickFragment = new QuickPartSelectorFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, quickFragment)
                    .commit();
        }
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
        if (quickFragment != null) {
            quickFragment.updateActivePartChip(partKey);
        }
    }

    @Override
    public void onPartSelectedFromFragment(String partKey, String displayName) {
        setSelectedPart(partKey, displayName);
    }

    @Override
    public void onColorSelectedFromFragment(int colorValue) {
        saveUndoState();
        this.selectedColorValue = colorValue;
        this.hasColorValue = true;

        if ("SLEEVE_LEFT".equals(selectedPart)) {
            colorSleeveLeft = colorValue;
        } else if ("SLEEVE_RIGHT".equals(selectedPart)) {
            colorSleeveRight = colorValue;
        } else if ("NECKLINE".equals(selectedPart)) {
            colorNeckline = colorValue;
        } else if ("CUFFS".equals(selectedPart)) {
            colorCuffs = colorValue;
        } else {
            colorBody = colorValue;
        }

        applyInitialPreview();
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
        selectedCategory = intent.getStringExtra("selectedCategory");
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
        selectedSleeve = intent.getStringExtra("selectedSleeve");
        selectedCollar = intent.getStringExtra("selectedCollar");
        selectedPocket = intent.getStringExtra("selectedPocket");
        selectedNeckline = intent.getStringExtra("selectedNeckline");
        selectedCuff = intent.getStringExtra("selectedCuff");
        selectedButton = intent.getStringExtra("selectedButton");
        selectedLength = intent.getStringExtra("selectedLength");
        selectedFit = intent.getStringExtra("selectedFit");
        selectedPattern = intent.getStringExtra("selectedPattern");
        selectedWaist = intent.getStringExtra("selectedWaist");
        isFavorite = intent.getBooleanExtra("isFavorite", isFavorite);

        if (intent.hasExtra("selectedColorValue")) {
            selectedColorValue = intent.getIntExtra("selectedColorValue", 0);
            hasColorValue = true;

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
        if (text == null) {
            text = intent.getStringExtra("selectedText");
        }
        if (intent.hasExtra("designText") || intent.hasExtra("selectedText")) {
            txtDesignText.setText(text == null ? "" : text);
            txtDesignText.setVisibility(text == null || text.trim().isEmpty()
                    ? View.GONE : View.VISIBLE);
        }

        if (intent.hasExtra("selectedTextColor")) {
            txtDesignText.setTextColor(intent.getIntExtra("selectedTextColor", 0xFFD4AF37));
        }
        if (intent.hasExtra("selectedTextSize")) {
            txtDesignText.setTextSize(intent.getFloatExtra("selectedTextSize", 18f));
        }
        if (intent.hasExtra("selectedRotation")) {
            txtDesignText.setRotation(intent.getFloatExtra("selectedRotation", 0f));
        }
        selectedFont = intent.getStringExtra("selectedFont") == null
                ? "Classic" : intent.getStringExtra("selectedFont");
        applyTextFont();
        if (intent.hasExtra("designTextX")) {
            txtDesignText.setX(intent.getFloatExtra("designTextX", txtDesignText.getX()));
        }
        if (intent.hasExtra("designTextY")) {
            txtDesignText.setY(intent.getFloatExtra("designTextY", txtDesignText.getY()));
        }

        int logoImage = intent.getIntExtra("designLogoImage", 0);
        if (logoImage != 0) {
            imgDesignLogo.setImageResource(logoImage);
            imgDesignLogo.setVisibility(View.VISIBLE);
        }
        logoPath = intent.getStringExtra("designLogoPath");
        float logoScale = intent.getFloatExtra("designLogoScale", 1f);
        imgDesignLogo.setScaleX(logoScale);
        imgDesignLogo.setScaleY(logoScale);
        if (logoPath != null && new File(logoPath).isFile()) {
            imgDesignLogo.setImageURI(android.net.Uri.fromFile(new File(logoPath)));
            imgDesignLogo.setVisibility(View.VISIBLE);
        } else if (intent.hasExtra("designLogoPath") && logoImage == 0) {
            imgDesignLogo.setImageDrawable(null);
            imgDesignLogo.setVisibility(View.GONE);
        }

        editingDesignId = intent.getLongExtra("editDesignId", -1);
        String designData = intent.getStringExtra("designData");
        if (designData != null) {
            restoreDesignData(designData);
        }

    }

    private void restoreDesignData(String json) {
        try {
            JSONObject data = new JSONObject(json);
            selectedTemplateName = data.optString("template", selectedTemplateName);
            selectedCategory = data.optString("category", selectedCategory);
            selectedTemplateImage = data.optInt("templateImage", selectedTemplateImage);
            selectedFabricName = data.optString("fabric", selectedFabricName);
            selectedFabricImage = data.optInt("fabricImage", selectedFabricImage);
            selectedColorName = data.optString("colorName", selectedColorName);
            selectedColorValue = data.optInt("colorValue", selectedColorValue);
            hasColorValue = data.optBoolean("hasColor",
                    data.has("colorValue") && data.optInt("colorValue") != 0);
            colorBody = data.optInt("color_BODY", colorBody);
            colorSleeveLeft = data.optInt("color_SLEEVE_LEFT", colorSleeveLeft);
            colorSleeveRight = data.optInt("color_SLEEVE_RIGHT", colorSleeveRight);
            colorNeckline = data.optInt("color_NECKLINE", colorNeckline);
            colorCuffs = data.optInt("color_CUFFS", colorCuffs);
            selectedSleeve = data.optString("sleeve", selectedSleeve);
            selectedPart = data.optString("selectedPart", selectedPart);
            selectedNeckline = data.optString("neckline", selectedNeckline);
            selectedCollar = selectedNeckline;
            selectedPocket = data.optString("pocket", selectedPocket);
            selectedCuff = data.optString("cuff", selectedCuff);
            selectedButton = data.optString("button", selectedButton);
            selectedLength = data.optString("dressLength", selectedLength);
            selectedFit = data.optString("fitStyle", selectedFit);
            selectedPattern = data.optString("pattern", selectedPattern);
            selectedWaist = data.optString("waist", selectedWaist);
            String text = data.optString("text", "");
            txtDesignText.setText(text);
            txtDesignText.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
            txtDesignText.setTextColor(data.optInt("textColor", txtDesignText.getCurrentTextColor()));
            txtDesignText.setTextSize(data.optInt("textSize", 18));
            txtDesignText.setRotation(data.optInt("textRotation", 0));
            selectedFont = data.optString("font", "Classic");
            applyTextFont();
            logoPath = data.optString("logoPath", "");
            int logoImage = data.optInt("logoImage", 0);
            if (logoImage != 0) {
                imgDesignLogo.setImageResource(logoImage);
                imgDesignLogo.setVisibility(View.VISIBLE);
            } else if (logoPath.isEmpty()) {
                imgDesignLogo.setVisibility(View.GONE);
            }
            if (!logoPath.isEmpty() && new File(logoPath).isFile()) {
                imgDesignLogo.setImageURI(android.net.Uri.fromFile(new File(logoPath)));
                imgDesignLogo.setVisibility(View.VISIBLE);
            }
            float logoScale = (float) data.optDouble("logoScale", 1.0);
            imgDesignLogo.setScaleX(logoScale);
            imgDesignLogo.setScaleY(logoScale);
            txtDesignText.setX((float) data.optDouble("textX", txtDesignText.getX()));
            txtDesignText.setY((float) data.optDouble("textY", txtDesignText.getY()));
            imgDesignLogo.setX((float) data.optDouble("logoX", imgDesignLogo.getX()));
            imgDesignLogo.setY((float) data.optDouble("logoY", imgDesignLogo.getY()));
            isFavorite = getIntent().getBooleanExtra(
                    "isFavorite", data.optBoolean("favorite", false));
        } catch (JSONException e) {
            showMessage("Could not load this design's customization data.");
        }
    }

    private void applyTextFont() {
        if ("Elegant".equals(selectedFont)) {
            txtDesignText.setTypeface(android.graphics.Typeface.create("serif",
                    android.graphics.Typeface.ITALIC));
        } else if ("Modern".equals(selectedFont)) {
            txtDesignText.setTypeface(android.graphics.Typeface.create(
                    android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL));
        } else if ("Bold".equals(selectedFont)) {
            txtDesignText.setTypeface(android.graphics.Typeface.create(
                    android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD));
        } else {
            txtDesignText.setTypeface(android.graphics.Typeface.create(
                    android.graphics.Typeface.SERIF, android.graphics.Typeface.NORMAL));
        }
    }

    private void applyInitialPreview() {
        updateCustomizationPreview();
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
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        passDesignData(intent);

        intent.putExtra("designText", txtDesignText.getText().toString());
        intent.putExtra("selectedTextColor", txtDesignText.getCurrentTextColor());
        intent.putExtra("selectedTextSize", txtDesignText.getTextSize() / getResources().getDisplayMetrics().scaledDensity);
        intent.putExtra("selectedRotation", txtDesignText.getRotation());
        intent.putExtra("designLogoImage", getIntent().getIntExtra("designLogoImage", 0));
        intent.putExtra("designTextX", txtDesignText.getX());
        intent.putExtra("designTextY", txtDesignText.getY());
        intent.putExtra("designLogoScale", imgDesignLogo.getScaleX());

        pendingEditorState = captureState();
        editorScreenLauncher.launch(intent);
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
        if (selectedCategory != null) {
            intent.putExtra("selectedCategory", selectedCategory);
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
        if (selectedSleeve != null) intent.putExtra("selectedSleeve", selectedSleeve);
        if (selectedCollar != null) intent.putExtra("selectedCollar", selectedCollar);
        if (selectedPocket != null) intent.putExtra("selectedPocket", selectedPocket);
        if (selectedNeckline != null) intent.putExtra("selectedNeckline", selectedNeckline);
        if (selectedCuff != null) intent.putExtra("selectedCuff", selectedCuff);
        if (selectedButton != null) intent.putExtra("selectedButton", selectedButton);
        if (selectedLength != null) intent.putExtra("selectedLength", selectedLength);
        if (selectedFit != null) intent.putExtra("selectedFit", selectedFit);
        if (selectedPattern != null) intent.putExtra("selectedPattern", selectedPattern);
        if (selectedWaist != null) intent.putExtra("selectedWaist", selectedWaist);
        if (logoPath != null) intent.putExtra("designLogoPath", logoPath);
        intent.putExtra("selectedFont", selectedFont);
        intent.putExtra("isFavorite", isFavorite);
    }

    private void showSaveDesignDialog() {
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setHint("Design name");
        if (editingDesignId > 0) {
            String existing = getIntent().getStringExtra("designName");
            nameInput.setText(existing == null ? "" : existing);
        }
        new AlertDialog.Builder(this)
                .setTitle(editingDesignId > 0 ? "Update design" : "Save design")
                .setView(nameInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        nameInput.setError("Enter a design name");
                        showSaveDesignDialog();
                    } else {
                        saveDesign(name);
                    }
                })
                .show();
    }

    private void saveDesign(String name) {
        String email = getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                .getString("userEmail", "");
        if (email.isEmpty()) {
            showMessage("Please log in before saving a design.");
            return;
        }
        try {
            JSONObject data = buildDesignData();
            String previewPath = savePreviewImage();
            DatabaseHelper database = new DatabaseHelper(this);
            if (editingDesignId > 0) {
                if (!database.updateDesign(editingDesignId, email, name, data, previewPath)) {
                    showMessage("Could not update this design.");
                    return;
                }
                database.setDesignFavorite(editingDesignId, email, isFavorite);
            } else {
                long id = database.saveDesign(email, name, data, previewPath, isFavorite);
                if (id < 0) {
                    showMessage("Could not save this design.");
                    return;
                }
                editingDesignId = id;
            }
            showMessage("Design saved.");
            startActivity(new Intent(this, MyDesignActivity.class));
            finish();
        } catch (JSONException | java.io.IOException e) {
            showMessage("Could not save design: " + e.getMessage());
        }
    }

    private JSONObject buildDesignData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("category", selectedCategory == null ? "Garment" : selectedCategory);
        data.put("template", selectedTemplateName == null ? "Kurti 01" : selectedTemplateName);
        data.put("templateImage", selectedTemplateImage == 0 ? R.drawable.kurti_01 : selectedTemplateImage);
        data.put("fabric", selectedFabricName == null ? "" : selectedFabricName);
        data.put("fabricImage", selectedFabricImage);
        data.put("colorName", selectedColorName == null ? "" : selectedColorName);
        data.put("colorValue", hasColorValue ? selectedColorValue : 0);
        data.put("hasColor", hasColorValue);
        data.put("color_BODY", colorBody);
        data.put("color_SLEEVE_LEFT", colorSleeveLeft);
        data.put("color_SLEEVE_RIGHT", colorSleeveRight);
        data.put("color_NECKLINE", colorNeckline);
        data.put("color_CUFFS", colorCuffs);
        data.put("sleeve", selectedSleeve == null ? "" : selectedSleeve);
        data.put("selectedPart", selectedPart);
        data.put("neckline", selectedNeckline == null ? selectedCollar : selectedNeckline);
        data.put("pocket", selectedPocket == null ? "" : selectedPocket);
        data.put("cuff", selectedCuff == null ? "" : selectedCuff);
        data.put("button", selectedButton == null ? "" : selectedButton);
        data.put("dressLength", selectedLength == null ? "" : selectedLength);
        data.put("fitStyle", selectedFit == null ? "" : selectedFit);
        data.put("pattern", selectedPattern == null ? "" : selectedPattern);
        data.put("waist", selectedWaist == null ? "" : selectedWaist);
        data.put("text", txtDesignText.getText().toString());
        data.put("font", selectedFont);
        data.put("textColor", txtDesignText.getCurrentTextColor());
        data.put("textSize", txtDesignText.getTextSize() / getResources().getDisplayMetrics().scaledDensity);
        data.put("textRotation", txtDesignText.getRotation());
        data.put("textX", txtDesignText.getX());
        data.put("textY", txtDesignText.getY());
        data.put("logoPath", logoPath == null ? "" : logoPath);
        data.put("logoScale", imgDesignLogo.getScaleX());
        data.put("logoX", imgDesignLogo.getX());
        data.put("logoY", imgDesignLogo.getY());
        data.put("favorite", isFavorite);
        return data;
    }

    private String savePreviewImage() throws java.io.IOException {
        if (designPreview.getWidth() <= 0 || designPreview.getHeight() <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(designPreview.getWidth(),
                designPreview.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        designPreview.draw(canvas);
        File directory = new File(getFilesDir(), "design_previews");
        if (!directory.exists() && !directory.mkdirs()) {
            bitmap.recycle();
            throw new java.io.IOException("Unable to create preview directory");
        }
        File file = new File(directory, "design_" + System.currentTimeMillis() + ".png");
        try (FileOutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } finally {
            bitmap.recycle();
        }
        return file.getAbsolutePath();
    }

    private void updateCustomizationPreview() {
        if (txtCustomizationPreview == null) return;
        StringBuilder summary = new StringBuilder();
        appendOption(summary, "Fabric", selectedFabricName);
        appendOption(summary, "Color", selectedColorName);
        appendOption(summary, "Sleeve", selectedSleeve);
        appendOption(summary, "Neckline", selectedNeckline == null ? selectedCollar : selectedNeckline);
        appendOption(summary, "Pocket", selectedPocket);
        appendOption(summary, "Length", selectedLength);
        appendOption(summary, "Fit", selectedFit);
        appendOption(summary, "Cuff", selectedCuff);
        appendOption(summary, "Button", selectedButton);
        appendOption(summary, "Pattern", selectedPattern);
        appendOption(summary, "Waist", selectedWaist);
        if (summary.length() > 0) {
            summary.append("\nSaved design specifications. Alternate garment shapes require matching layer artwork.");
        }
        txtCustomizationPreview.setText(summary.toString());
        txtCustomizationPreview.setVisibility(summary.length() > 0 ? View.VISIBLE : View.GONE);
    }

    private void appendOption(StringBuilder summary, String label, String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (summary.length() > 0) summary.append("  •  ");
        summary.append(label).append(": ").append(value);
    }

    private void saveUndoState() {
        if (undoStack.size() == 50) {
            undoStack.remove(0);
        }
        undoStack.add(captureState());
        redoStack.clear();
    }

    private DesignState captureState() {
        String details;
        try {
            details = buildDesignData().toString();
        } catch (JSONException e) {
            details = "{}";
        }
        return new DesignState(
                txtDesignText.getX(),
                txtDesignText.getY(),
                imgDesignLogo.getX(),
                imgDesignLogo.getY(),
                txtDesignText.getVisibility(),
                imgDesignLogo.getVisibility(),
                txtDesignText.getText().toString(),
                colorBody,
                colorSleeveLeft,
                colorSleeveRight,
                colorNeckline,
                colorCuffs,
                details
        );
    }

    private void undoDesign() {
        if (undoStack.isEmpty()) {
            showMessage("Nothing to undo");
            return;
        }

        redoStack.add(captureState());
        restoreState(undoStack.remove(undoStack.size() - 1));
    }

    private void redoDesign() {
        if (redoStack.isEmpty()) {
            showMessage("Nothing to redo");
            return;
        }

        DesignState state = redoStack.remove(redoStack.size() - 1);
        undoStack.add(captureState());
        restoreState(state);
    }

    private void restoreState(DesignState state) {
        restoreDesignData(state.details);
        txtDesignText.setX(state.textX);
        txtDesignText.setY(state.textY);
        imgDesignLogo.setX(state.logoX);
        imgDesignLogo.setY(state.logoY);

        txtDesignText.setVisibility(state.textVisibility);
        imgDesignLogo.setVisibility(state.logoVisibility);
        txtDesignText.setText(state.text);

        this.colorBody = state.colorBody;
        this.colorSleeveLeft = state.colorSleeveLeft;
        this.colorSleeveRight = state.colorSleeveRight;
        this.colorNeckline = state.colorNeckline;
        this.colorCuffs = state.colorCuffs;

        applyInitialPreview();
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void confirmResetDesign() {
        new AlertDialog.Builder(this)
                .setTitle("Reset design?")
                .setMessage("Clear the current fabric, color, customizations, text, and logo?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset", (dialog, which) -> {
                    saveUndoState();
                    selectedFabricImage = 0;
                    selectedFabricName = null;
                    selectedColorName = null;
                    hasColorValue = false;
                    selectedColorValue = 0;
                    colorBody = colorSleeveLeft = colorSleeveRight = colorNeckline = colorCuffs = 0;
                    selectedSleeve = selectedCollar = selectedNeckline = selectedPocket = null;
                    selectedCuff = selectedButton = selectedLength = selectedFit = null;
                    selectedPattern = selectedWaist = null;
                    txtDesignText.setText("");
                    txtDesignText.setVisibility(View.GONE);
                    txtDesignText.setTextColor(0xFFD4AF37);
                    txtDesignText.setTextSize(18f);
                    txtDesignText.setRotation(0f);
                    txtDesignText.setTypeface(null, android.graphics.Typeface.BOLD);
                    imgDesignLogo.setImageDrawable(null);
                    imgDesignLogo.setVisibility(View.GONE);
                    imgDesignLogo.setScaleX(1f);
                    imgDesignLogo.setScaleY(1f);
                    logoPath = null;
                    selectedFont = "Classic";
                    applyInitialPreview();
                })
                .show();
    }
}