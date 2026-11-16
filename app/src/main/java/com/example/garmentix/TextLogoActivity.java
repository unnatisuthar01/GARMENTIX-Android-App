package com.example.garmentix;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TextLogoActivity extends AppCompatActivity {

    ImageView btnBack;

    TextView tabText, tabLogo;
    TextView fontClassic, fontElegant, fontModern, fontBold;

    TextView colorBlack, colorGold, colorPink,
            colorPurple, colorBlue, colorGreen;

    EditText edtText;

    SeekBar seekTextSize, seekRotation;

    TextView txtTextSizeValue, txtRotationValue;
    TextView txtPreview;
    ImageView imgLogoPreview;
    Button btnSelectLogo, btnRemoveLogo, btnRemoveText;
    SeekBar seekLogoSize;

    Button btnApply;

    int selectedTextColor = Color.rgb(17, 24, 39);
    float selectedTextSize = 24f;
    float selectedRotation = 0f;

    String selectedFont = "Classic";
    String logoPath;
    float logoScale = 1f;
    ActivityResultLauncher<String> logoPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_logo);

        // -------------------------
        // FIND VIEWS
        // -------------------------

        btnBack = findViewById(R.id.btnBack);

        tabText = findViewById(R.id.tabText);
        tabLogo = findViewById(R.id.tabLogo);

        edtText = findViewById(R.id.edtText);

        fontClassic = findViewById(R.id.fontClassic);
        fontElegant = findViewById(R.id.fontElegant);
        fontModern = findViewById(R.id.fontModern);
        fontBold = findViewById(R.id.fontBold);

        colorBlack = findViewById(R.id.colorBlack);
        colorGold = findViewById(R.id.colorGold);
        colorPink = findViewById(R.id.colorPink);
        colorPurple = findViewById(R.id.colorPurple);
        colorBlue = findViewById(R.id.colorBlue);
        colorGreen = findViewById(R.id.colorGreen);

        seekTextSize = findViewById(R.id.seekTextSize);
        seekRotation = findViewById(R.id.seekRotation);

        txtTextSizeValue = findViewById(R.id.txtTextSizeValue);
        txtRotationValue = findViewById(R.id.txtRotationValue);

        txtPreview = findViewById(R.id.txtPreview);

        btnApply = findViewById(R.id.btnApply);
        imgLogoPreview = findViewById(R.id.imgLogoPreview);
        btnSelectLogo = findViewById(R.id.btnSelectLogo);
        btnRemoveLogo = findViewById(R.id.btnRemoveLogo);
        btnRemoveText = findViewById(R.id.btnRemoveText);
        seekLogoSize = findViewById(R.id.seekLogoSize);

        logoPicker = registerForActivityResult(new ActivityResultContracts.GetContent(),
                this::loadLogo);
        logoPath = getIntent().getStringExtra("designLogoPath");
        selectedFont = getIntent().getStringExtra("selectedFont") == null
                ? "Classic" : getIntent().getStringExtra("selectedFont");
        selectedTextColor = getIntent().getIntExtra("selectedTextColor", selectedTextColor);
        selectedTextSize = getIntent().getFloatExtra("selectedTextSize", selectedTextSize);
        selectedRotation = getIntent().getFloatExtra("selectedRotation", selectedRotation);
        logoScale = getIntent().getFloatExtra("designLogoScale", 1f);
        String initialText = getIntent().getStringExtra("designText");
        if (initialText != null) edtText.setText(initialText);
        txtPreview.setTextColor(selectedTextColor);
        txtPreview.setTextSize(selectedTextSize);
        txtPreview.setRotation(selectedRotation);
        applyFont(selectedFont);
        updatePreview();
        if (logoPath != null && new File(logoPath).isFile()) {
            imgLogoPreview.setImageURI(Uri.fromFile(new File(logoPath)));
            imgLogoPreview.setVisibility(android.view.View.VISIBLE);
        }
        seekLogoSize.setProgress(Math.round((logoScale - 0.5f) * 100));
        seekLogoSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                logoScale = 0.5f + progress / 100f;
                imgLogoPreview.setScaleX(logoScale);
                imgLogoPreview.setScaleY(logoScale);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        btnSelectLogo.setOnClickListener(v -> logoPicker.launch("image/*"));
        btnRemoveLogo.setOnClickListener(v -> {
            logoPath = "";
            imgLogoPreview.setImageDrawable(null);
            imgLogoPreview.setVisibility(android.view.View.GONE);
        });
        btnRemoveText.setOnClickListener(v -> {
            edtText.setText("");
            txtPreview.setText("");
        });


        // -------------------------
        // BACK BUTTON
        // -------------------------

        btnBack.setOnClickListener(view -> {
            returnToDesignScreen();
        });
        edtText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }
            @Override public void afterTextChanged(android.text.Editable editable) { }
        });


        // -------------------------
        // TEXT TAB
        // -------------------------

        tabText.setOnClickListener(view -> {

            tabText.setBackgroundColor(Color.rgb(201, 162, 39));
            tabText.setTextColor(Color.WHITE);

            tabLogo.setBackgroundColor(Color.WHITE);
            tabLogo.setTextColor(Color.rgb(17, 24, 39));

        });


        // -------------------------
        // LOGO TAB
        // -------------------------

        tabLogo.setOnClickListener(view -> {

            tabLogo.setBackgroundColor(Color.rgb(201, 162, 39));
            tabLogo.setTextColor(Color.WHITE);

            tabText.setBackgroundColor(Color.WHITE);
            tabText.setTextColor(Color.rgb(17, 24, 39));
            logoPicker.launch("image/*");

        });


        // -------------------------
        // TEXT INPUT
        // -------------------------

        edtText.setOnFocusChangeListener((view, hasFocus) -> {

            if (!hasFocus) {
                updatePreview();
            }
        });


        edtText.setOnEditorActionListener((v, actionId, event) -> {

            updatePreview();
            return false;

        });


        // -------------------------
        // FONT STYLE
        // -------------------------

        fontClassic.setOnClickListener(view -> {

            selectedFont = "Classic";

            txtPreview.setTypeface(
                    Typeface.create(
                            Typeface.SERIF,
                            Typeface.NORMAL
                    )
            );

        });


        fontElegant.setOnClickListener(view -> {

            selectedFont = "Elegant";

            txtPreview.setTypeface(
                    Typeface.create(
                            "serif",
                            Typeface.ITALIC
                    )
            );

        });


        fontModern.setOnClickListener(view -> {

            selectedFont = "Modern";

            txtPreview.setTypeface(
                    Typeface.create(
                            Typeface.SANS_SERIF,
                            Typeface.NORMAL
                    )
            );

        });


        fontBold.setOnClickListener(view -> {

            selectedFont = "Bold";

            txtPreview.setTypeface(
                    Typeface.create(
                            Typeface.SANS_SERIF,
                            Typeface.BOLD
                    )
            );

        });


        // -------------------------
        // TEXT COLORS
        // -------------------------

        colorBlack.setOnClickListener(view -> {

            selectedTextColor =
                    Color.rgb(17, 24, 39);

            txtPreview.setTextColor(selectedTextColor);

        });


        colorGold.setOnClickListener(view -> {

            selectedTextColor =
                    Color.rgb(201, 162, 39);

            txtPreview.setTextColor(selectedTextColor);

        });


        colorPink.setOnClickListener(view -> {

            selectedTextColor =
                    Color.rgb(236, 72, 153);

            txtPreview.setTextColor(selectedTextColor);

        });


        colorPurple.setOnClickListener(view -> {

            selectedTextColor =
                    Color.rgb(147, 51, 234);

            txtPreview.setTextColor(selectedTextColor);

        });


        colorBlue.setOnClickListener(view -> {

            selectedTextColor =
                    Color.rgb(59, 130, 246);

            txtPreview.setTextColor(selectedTextColor);

        });


        colorGreen.setOnClickListener(view -> {

            selectedTextColor =
                    Color.rgb(34, 197, 94);

            txtPreview.setTextColor(selectedTextColor);

        });


        // -------------------------
        // TEXT SIZE
        // -------------------------

        seekTextSize.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        int size = progress + 10;

                        selectedTextSize = size;

                        txtPreview.setTextSize(size);

                        txtTextSizeValue.setText(
                                "Size: " + size
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );


        // -------------------------
        // ROTATION
        // -------------------------

        seekRotation.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        selectedRotation = progress;

                        txtPreview.setRotation(
                                selectedRotation
                        );

                        txtRotationValue.setText(
                                "Rotation: "
                                        + progress
                                        + "°"
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );


        // -------------------------
        // APPLY
        // -------------------------

        btnApply.setOnClickListener(view -> {

            updatePreview();

            returnToDesignScreen();

        });

    }


    // -------------------------
    // UPDATE PREVIEW
    // -------------------------

    private void updatePreview() {

        String text =
                edtText.getText()
                        .toString()
                        .trim();

        if (text.isEmpty()) {

            txtPreview.setText("Your Text");

        } else {

            txtPreview.setText(text);

        }

    }

    private void applyFont(String font) {
        if ("Elegant".equals(font)) {
            txtPreview.setTypeface(Typeface.create("serif", Typeface.ITALIC));
        } else if ("Modern".equals(font)) {
            txtPreview.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        } else if ("Bold".equals(font)) {
            txtPreview.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        } else {
            txtPreview.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        }
    }

    private void loadLogo(Uri uri) {
        if (uri == null) return;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                showMessage("Unable to read the selected logo.");
                return;
            }
            File directory = new File(getFilesDir(), "design_logos");
            if (!directory.exists() && !directory.mkdirs()) {
                showMessage("Unable to create logo storage.");
                return;
            }
            File file = new File(directory, "logo_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            }
            logoPath = file.getAbsolutePath();
            imgLogoPreview.setImageBitmap(BitmapFactory.decodeFile(logoPath));
            imgLogoPreview.setVisibility(android.view.View.VISIBLE);
            imgLogoPreview.setScaleX(logoScale);
            imgLogoPreview.setScaleY(logoScale);
        } catch (Exception e) {
            showMessage("Could not load logo: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    // -------------------------
    // RETURN TO DESIGN SCREEN
    // -------------------------

    private void returnToDesignScreen() {

        Intent intent =
                new Intent(
                        TextLogoActivity.this,
                        DesignScreenActivity.class
                );


        Intent currentIntent = getIntent();

        if (currentIntent != null) {
            if (currentIntent.getExtras() != null) {
                intent.putExtras(currentIntent.getExtras());
            }

            if (currentIntent.hasExtra("selectedTemplateImage")) {

                intent.putExtra(
                        "selectedTemplateImage",
                        currentIntent.getIntExtra(
                                "selectedTemplateImage",
                                0
                        )
                );
            }

            if (currentIntent.hasExtra("selectedTemplateName")) {

                intent.putExtra(
                        "selectedTemplateName",
                        currentIntent.getStringExtra(
                                "selectedTemplateName"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedFabricImage")) {

                intent.putExtra(
                        "selectedFabricImage",
                        currentIntent.getIntExtra(
                                "selectedFabricImage",
                                0
                        )
                );
            }

            if (currentIntent.hasExtra("selectedFabricName")) {

                intent.putExtra(
                        "selectedFabricName",
                        currentIntent.getStringExtra(
                                "selectedFabricName"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedColorName")) {

                intent.putExtra(
                        "selectedColorName",
                        currentIntent.getStringExtra(
                                "selectedColorName"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedColorValue")) {

                intent.putExtra(
                        "selectedColorValue",
                        currentIntent.getIntExtra(
                                "selectedColorValue",
                                0
                        )
                );
            }

            // Customize data

            if (currentIntent.hasExtra("selectedSleeve")) {

                intent.putExtra(
                        "selectedSleeve",
                        currentIntent.getStringExtra(
                                "selectedSleeve"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedCollar")) {

                intent.putExtra(
                        "selectedCollar",
                        currentIntent.getStringExtra(
                                "selectedCollar"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedPocket")) {

                intent.putExtra(
                        "selectedPocket",
                        currentIntent.getStringExtra(
                                "selectedPocket"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedLength")) {

                intent.putExtra(
                        "selectedLength",
                        currentIntent.getStringExtra(
                                "selectedLength"
                        )
                );
            }

            if (currentIntent.hasExtra("selectedFit")) {

                intent.putExtra(
                        "selectedFit",
                        currentIntent.getStringExtra(
                                "selectedFit"
                        )
                );
            }
        }


        // Text data

        String enteredText =
                edtText.getText()
                        .toString()
                        .trim();

        intent.putExtra(
                "selectedText",
                enteredText
        );
        intent.putExtra("designText", enteredText);

        intent.putExtra(
                "selectedFont",
                selectedFont
        );

        intent.putExtra(
                "selectedTextColor",
                selectedTextColor
        );

        intent.putExtra(
                "selectedTextSize",
                selectedTextSize
        );

        intent.putExtra(
                "selectedRotation",
                selectedRotation
        );
        intent.putExtra("designLogoPath", logoPath == null ? "" : logoPath);
        if (logoPath == null || logoPath.isEmpty()) {
            intent.removeExtra("designLogoImage");
        }
        intent.putExtra("designLogoScale", logoScale);


        setResult(RESULT_OK, intent);
        finish();
    }
}