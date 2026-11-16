package com.example.garmentix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnDesign;

    LinearLayout
            cardDesignCloth,
            cardSavedDesign,
            cardProfile,
            cardLogout;

    LinearLayout
            navHome,
            navDesign,
            navMyDesign,
            navProfile;

    TextView txtHello;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_home);

        // =========================
        // WINDOW INSETS
        // =========================

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    androidx.core.graphics.Insets systemBars =
                            insets.getInsets(
                                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // =========================
        // FIND VIEWS
        // =========================

        btnDesign = findViewById(R.id.btnDesign);

        cardDesignCloth = findViewById(R.id.cardDesignCloth);
        cardSavedDesign = findViewById(R.id.cardSavedDesign);
        cardProfile = findViewById(R.id.cardProfile);
        cardLogout = findViewById(R.id.cardLogout);

        navHome = findViewById(R.id.navHome);
        navDesign = findViewById(R.id.navDesign);
        navMyDesign = findViewById(R.id.navMyDesign);
        navProfile = findViewById(R.id.navProfile);

        txtHello = findViewById(R.id.txtHello);

        // =========================
        // SHOW USER NAME
        // =========================

        txtHello.setText("Hello User! 👋");

        ImageView btnHomeBack = findViewById(R.id.btnHomeBack);
        btnHomeBack.setOnClickListener(view -> finish());

        // =========================
        // =========================
        // START DESIGN BUTTON
        // =========================

        btnDesign.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            DesignScreenActivity.class
                    );

            startActivity(intent);

        });

        // =========================
        // DESIGN CLOTH CARD
        // =========================

        cardDesignCloth.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            DesignScreenActivity.class
                    );

            startActivity(intent);

        });

        // =========================
        // =========================
        // SAVED DESIGN CARD
        // =========================

        cardSavedDesign.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, SavedDesignActivity.class));
        });

        findViewById(R.id.cardMyDesigns).setOnClickListener(view ->
                startActivity(new Intent(this, MyDesignActivity.class)));
        findViewById(R.id.cardExportPdf).setOnClickListener(view -> {
            Intent intent = new Intent(this, MyDesignActivity.class);
            intent.putExtra("exportPdf", true);
            startActivity(intent);
        });
        findViewById(R.id.cardSettings).setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.cardHelp).setOnClickListener(view ->
                startActivity(new Intent(this, HelpActivity.class)));

        // =========================
        // PROFILE CARD
        // =========================

        cardProfile.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ProfileSettingActivity.class
                    );

            startActivity(intent);

        });

        // =========================
        // LOGOUT
        // =========================

        cardLogout.setOnClickListener(view -> {
            getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                    .edit().clear().apply();

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            LoginActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();

        });

        // =========================
        // BOTTOM HOME
        // =========================

        navHome.setOnClickListener(view -> {

            // Already on Home screen

        });

        // =========================
        // BOTTOM DESIGN
        // =========================

        navDesign.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            DesignScreenActivity.class
                    );

            startActivity(intent);

        });

        // =========================
        // BOTTOM MY DESIGN
        // =========================

        navMyDesign.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            MyDesignActivity.class
                    );

            startActivity(intent);

        });

        // =========================
        // BOTTOM PROFILE
        // =========================

        navProfile.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ProfileSettingActivity.class
                    );

            startActivity(intent);

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView greeting = findViewById(R.id.txtHello);
        if (greeting == null) return;
        String email = getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                .getString("userEmail", "");
        String name = "User";
        if (!email.isEmpty()) {
            DatabaseHelper database = new DatabaseHelper(this);
            try (android.database.Cursor cursor = database.getUserDetails(email)) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                }
            }
        }
        greeting.setText("Hello " + name + "!");
    }
}