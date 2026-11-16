package com.example.garmentix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ProfileSettingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgProfile;
    private ImageView btnCamera;

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtMobile;

    private Button btnEditProfile;
    private Button btnLogout;

    private LinearLayout menuMeasurements;
    private LinearLayout menuSavedDesigns;
    private LinearLayout menuSettings;
    private LinearLayout menuHelp;

    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    private static final String PREF_NAME = "GARMENTIX_PREF";
    private static final String SESSION_PREF = "GARMENTIX_SESSION";
    private static final String PHOTO_PATH_KEY_PREFIX = "profilePhotoPath_";

    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setting);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        userEmail = getSharedPreferences(SESSION_PREF, MODE_PRIVATE)
                .getString("userEmail", "");
        if (userEmail.isEmpty()) {
            Intent login = new Intent(this, LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
            finish();
            return;
        }

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Initialize views
        initializeViews();

        // Register camera and gallery launchers
        registerPhotoLaunchers();

        // Load user details and profile photo
        loadUserDetails();
        migrateLegacyProfilePhoto();
        loadProfilePhoto();

        // Back Button
        btnBack.setOnClickListener(view -> finish());

        // Profile Photo Options
        imgProfile.setOnClickListener(view -> showPhotoOptions());
        btnCamera.setOnClickListener(view -> showPhotoOptions());

        // Edit Profile Button
        btnEditProfile.setOnClickListener(view -> {

            Intent intent = new Intent(
                    ProfileSettingActivity.this,
                    EditProfileActivity.class
            );

            intent.putExtra("userEmail", userEmail);

            startActivity(intent);
        });

        // My Measurements
        menuMeasurements.setOnClickListener(view -> {

            Intent intent = new Intent(
                    ProfileSettingActivity.this,
                    MyMeasurementsActivity.class
            );

            intent.putExtra("userEmail", userEmail);

            startActivity(intent);
        });

        menuSavedDesigns.setOnClickListener(view ->
                startActivity(new Intent(this, SavedDesignActivity.class)));
        menuSettings.setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
        menuHelp.setOnClickListener(view ->
                startActivity(new Intent(this, HelpActivity.class)));

        // Logout
        btnLogout.setOnClickListener(view -> {

            new AlertDialog.Builder(ProfileSettingActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Logout", (dialog, which) -> {

                        // Clear login session only
                        SharedPreferences sessionPreferences =
                                getSharedPreferences(SESSION_PREF, MODE_PRIVATE);

                        sessionPreferences.edit().clear().apply();

                        // Open Login Screen
                        Intent intent = new Intent(
                                ProfileSettingActivity.this,
                                LoginActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();
                    })
                    .show();
        });
    }

    // Initialize all views
    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgProfile);
        btnCamera = findViewById(R.id.btnCamera);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtMobile = findViewById(R.id.txtMobile);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        menuMeasurements = findViewById(R.id.menuMeasurements);
        menuSavedDesigns = findViewById(R.id.menuSavedDesigns);
        menuSettings = findViewById(R.id.menuSettings);
        menuHelp = findViewById(R.id.menuHelp);
    }

    // Register Camera and Gallery Launchers
    private void registerPhotoLaunchers() {

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {

                    if (bitmap != null) {
                        saveBitmap(bitmap);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {
                        saveGalleryImage(uri);
                    }
                }
        );
    }

    // Show Profile Photo Options
    private void showPhotoOptions() {

        String[] options = {
                "Take Photo",
                "Choose from Gallery",
                "Remove Photo"
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options, (dialogInterface, which) -> {

                    if (which == 0) {

                        cameraLauncher.launch(null);

                    } else if (which == 1) {

                        galleryLauncher.launch("image/*");

                    } else if (which == 2) {

                        confirmRemovePhoto();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    // Save Camera Image
    private void saveBitmap(Bitmap bitmap) {

        File photoFile = new File(
                getFilesDir(),
                getPhotoFileName()
        );

        try (FileOutputStream outputStream =
                     new FileOutputStream(photoFile)) {

            boolean compressed = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    outputStream
            );

            outputStream.flush();

            if (!compressed) {
                showMessage("Could not save photo.");
                return;
            }

            preferences.edit()
                    .putString(
                            getPhotoPathKey(),
                            photoFile.getAbsolutePath()
                    )
                    .apply();

            imgProfile.setImageBitmap(bitmap);
            showMessage("Profile photo saved.");

        } catch (Exception e) {
            showMessage("Unable to save photo.");
        }
    }

    // Load Image Selected from Gallery
    private void saveGalleryImage(Uri uri) {

        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                showMessage("Unable to open selected image.");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                showMessage("Invalid image selected.");
                return;
            }

            saveBitmap(bitmap);

        } catch (Exception e) {
            showMessage("Unable to load selected image.");
        }
    }

    // Load Saved Profile Photo
    private void loadProfilePhoto() {

        String photoPath = preferences.getString(
                getPhotoPathKey(),
                null
        );

        if (photoPath == null || photoPath.trim().isEmpty()) {
            imgProfile.setImageResource(R.drawable.ic_person);
            return;
        }

        File photoFile = new File(photoPath);

        if (photoFile.exists() && photoFile.isFile()) {

            Bitmap bitmap = BitmapFactory.decodeFile(
                    photoFile.getAbsolutePath()
            );

            if (bitmap != null) {
                imgProfile.setImageBitmap(bitmap);
                return;
            }
        }

        // Remove invalid photo path
        preferences.edit()
                .remove(getPhotoPathKey())
                .apply();

        imgProfile.setImageResource(R.drawable.ic_person);
    }

    private void migrateLegacyProfilePhoto() {
        if (databaseHelper.getUserCount() != 1
                || preferences.contains(getPhotoPathKey())
                || !userEmail.equalsIgnoreCase(preferences.getString("email", ""))) {
            return;
        }
        String legacyPath = preferences.getString("profilePhotoPath", null);
        if (legacyPath == null || legacyPath.trim().isEmpty()
                || !new File(legacyPath).isFile()) {
            return;
        }
        preferences.edit().putString(getPhotoPathKey(), legacyPath).apply();
    }

    // Confirm Profile Photo Removal
    private void confirmRemovePhoto() {

        new AlertDialog.Builder(this)
                .setTitle("Remove Profile Photo")
                .setMessage(
                        "Are you sure you want to remove your profile photo?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) ->
                        removeProfilePhoto())
                .show();
    }

    // Remove Saved Profile Photo
    private void removeProfilePhoto() {

        String photoPath = preferences.getString(
                getPhotoPathKey(),
                null
        );

        if (photoPath != null) {

            File photoFile = new File(photoPath);
            File internalDirectory = getFilesDir();

            try {

                if (photoFile.getCanonicalFile()
                        .getParentFile()
                        .equals(internalDirectory.getCanonicalFile())) {

                    if (photoFile.exists()) {
                        photoFile.delete();
                    }
                }

            } catch (Exception ignored) {
                // Default image will still be restored
            }
        }

        preferences.edit()
                .remove(getPhotoPathKey())
                .apply();

        imgProfile.setImageResource(R.drawable.ic_person);

        showMessage("Profile photo removed.");
    }

    private String getPhotoPathKey() {
        return PHOTO_PATH_KEY_PREFIX + getPhotoFileToken();
    }

    private String getPhotoFileName() {
        return "profile_photo_" + getPhotoFileToken() + ".jpg";
    }

    private String getPhotoFileToken() {
        return android.util.Base64.encodeToString(
                userEmail.toLowerCase(java.util.Locale.ROOT)
                        .getBytes(StandardCharsets.UTF_8),
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP
        );
    }

    // Load User Details from SQLite Database
    private void loadUserDetails() {

        if (userEmail == null || userEmail.trim().isEmpty()) {

            txtName.setText("User");
            txtEmail.setText("No email");
            txtMobile.setText("No mobile number");

            return;
        }

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getUserDetails(userEmail);

            if (cursor != null && cursor.moveToFirst()) {

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                String mobile = cursor.getString(
                        cursor.getColumnIndexOrThrow("mobile")
                );

                String email = cursor.getString(
                        cursor.getColumnIndexOrThrow("email")
                );

                txtName.setText(name);
                txtEmail.setText(email);

                if (mobile == null || mobile.trim().isEmpty()) {
                    txtMobile.setText("No mobile number");
                } else {
                    txtMobile.setText("+91 " + mobile);
                }

            } else {

                txtName.setText("User");
                txtEmail.setText("No email");
                txtMobile.setText("No mobile number");
            }

        } catch (Exception e) {

            txtName.setText("User");
            txtEmail.setText("No email");
            txtMobile.setText("No mobile number");

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Refresh Profile Details When Returning to This Screen
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                preferences != null &&
                txtName != null) {

            loadUserDetails();
            loadProfilePhoto();
        }
    }

    // Show Toast Message
    private void showMessage(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}