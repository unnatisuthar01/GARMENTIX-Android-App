package com.example.garmentix;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    ImageView btnBack;

    EditText edtName, edtMobile;

    TextView txtEmail;

    Button btnSave;

    DatabaseHelper databaseHelper;

    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        edtName = findViewById(R.id.edtName);
        edtMobile = findViewById(R.id.edtMobile);
        txtEmail = findViewById(R.id.txtEmail);
        btnSave = findViewById(R.id.btnSave);

        // Get email from previous screen
        userEmail = getIntent().getStringExtra("userEmail");

        // Back button
        btnBack.setOnClickListener(view -> finish());

        // Load existing user details
        loadUserDetails();

        // Save updated profile
        btnSave.setOnClickListener(view -> updateProfile());
    }

    // Load existing user details from SQLite
    private void loadUserDetails() {

        if (userEmail == null || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "User email not found",
                    Toast.LENGTH_SHORT
            ).show();

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

                // Display existing details
                edtName.setText(name);
                edtMobile.setText(mobile);
                txtEmail.setText(email);

            } else {

                Toast.makeText(
                        this,
                        "User details not found",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to load profile",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Update user profile in SQLite
    private void updateProfile() {

        String name = edtName.getText().toString().trim();
        String mobile = edtMobile.getText().toString().trim();

        // Validate name
        if (name.isEmpty()) {

            edtName.setError("Please enter your name");
            edtName.requestFocus();

            return;
        }

        // Validate mobile number
        if (mobile.isEmpty()) {

            edtMobile.setError("Please enter mobile number");
            edtMobile.requestFocus();

            return;
        }

        // Check 10-digit mobile number
        if (!mobile.matches("[0-9]{10}")) {

            edtMobile.setError(
                    "Enter a valid 10-digit mobile number"
            );

            edtMobile.requestFocus();

            return;
        }

        // Check user email
        if (userEmail == null || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "User email not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {
            if (databaseHelper.updateUserProfile(userEmail, name, mobile)) {
                Toast.makeText(
                        this,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                ).show();

                // Return to Profile screen
                finish();

            } else {

                Toast.makeText(
                        this,
                        "Profile update failed",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to update profile",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}