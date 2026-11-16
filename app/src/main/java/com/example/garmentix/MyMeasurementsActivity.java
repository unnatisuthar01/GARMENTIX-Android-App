package com.example.garmentix;

import android.database.Cursor;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MyMeasurementsActivity extends AppCompatActivity {

    ImageView btnBack;

    EditText edtShoulder, edtChest, edtWaist;
    EditText edtHips, edtSleeveLength, edtDressLength;

    Button btnSaveMeasurements;

    DatabaseHelper databaseHelper;

    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_measurements);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);

        edtShoulder = findViewById(R.id.edtShoulder);
        edtChest = findViewById(R.id.edtChest);
        edtWaist = findViewById(R.id.edtWaist);
        edtHips = findViewById(R.id.edtHips);
        edtSleeveLength = findViewById(R.id.edtSleeveLength);
        edtDressLength = findViewById(R.id.edtDressLength);

        btnSaveMeasurements = findViewById(R.id.btnSaveMeasurements);

        // Get Logged-in User Email
        userEmail = getIntent().getStringExtra("userEmail");

        if (userEmail == null || userEmail.trim().isEmpty()) {

            SharedPreferences preferences =
                    getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE);

            userEmail = preferences.getString("userEmail", "");

        }

        // Load Previously Saved Measurements
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            loadMeasurements();
        } else {
            Toast.makeText(
                    this,
                    "User email not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();
        }

        // Back Button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Save Measurements Button
        btnSaveMeasurements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMeasurements();
            }
        });
    }

    // Save or Update Measurements
    private void saveMeasurements() {

        String shoulder = edtShoulder.getText().toString().trim();
        String chest = edtChest.getText().toString().trim();
        String waist = edtWaist.getText().toString().trim();
        String hips = edtHips.getText().toString().trim();
        String sleeveLength = edtSleeveLength.getText().toString().trim();
        String dressLength = edtDressLength.getText().toString().trim();

        // Check Empty Fields
        if (shoulder.isEmpty() ||
                chest.isEmpty() ||
                waist.isEmpty() ||
                hips.isEmpty() ||
                sleeveLength.isEmpty() ||
                dressLength.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter all measurements",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Check User Email
        if (userEmail == null || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "User email not found. Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Save Measurements in SQLite
        boolean isSaved = databaseHelper.saveMeasurements(
                userEmail,
                shoulder,
                chest,
                waist,
                hips,
                sleeveLength,
                dressLength
        );

        if (isSaved) {

            Toast.makeText(
                    this,
                    "Measurements saved successfully!",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Unable to save measurements",
                    Toast.LENGTH_SHORT
            ).show();

        }
    }

    // Load Saved Measurements
    private void loadMeasurements() {

        Cursor cursor = databaseHelper.getMeasurements(userEmail);

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                edtShoulder.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow("shoulder"))
                );

                edtChest.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow("chest"))
                );

                edtWaist.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow("waist"))
                );

                edtHips.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow("hips"))
                );

                edtSleeveLength.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow("sleeve_length"))
                );

                edtDressLength.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow("dress_length"))
                );
            }

            cursor.close();
        }
    }
}