package com.example.garmentix;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.btnSettingsBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSettingsEditProfile).setOnClickListener(v -> {
            String email = getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                    .getString("userEmail", "");
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("userEmail", email);
            startActivity(intent);
        });
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePassword());
        Switch notifications = findViewById(R.id.switchNotifications);
        notifications.setChecked(getSharedPreferences("GARMENTIX_SETTINGS", MODE_PRIVATE)
                .getBoolean("notifications_enabled", true));
        notifications.setOnCheckedChangeListener((button, checked) ->
                getSharedPreferences("GARMENTIX_SETTINGS", MODE_PRIVATE).edit()
                        .putBoolean("notifications_enabled", checked).apply());
        findViewById(R.id.btnAppInformation).setOnClickListener(v ->
                showInfo("About GARMENTIX", "GARMENTIX\nDesign Today, Stitch Tomorrow.\nVersion 1.0"));
        findViewById(R.id.btnPrivacyInformation).setOnClickListener(v ->
                showInfo("Privacy", "Your profile, measurements, and designs are stored locally on this device. "
                        + "The app does not upload this information to a server."));
    }

    private void showChangePassword() {
        EditText current = passwordField("Current password");
        EditText next = passwordField("New password");
        EditText confirm = passwordField("Confirm new password");
        android.widget.LinearLayout fields = new android.widget.LinearLayout(this);
        fields.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        fields.setPadding(padding, 0, padding, 0);
        fields.addView(current);
        fields.addView(next);
        fields.addView(confirm);
        new AlertDialog.Builder(this).setTitle("Change password").setView(fields)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String oldPassword = current.getText().toString();
                    String newPassword = next.getText().toString();
                    String confirmation = confirm.getText().toString();
                    if (newPassword.length() < 8 || !newPassword.equals(confirmation)) {
                        Toast.makeText(this, "Use at least 8 characters and confirm the new password.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    String email = getSharedPreferences("GARMENTIX_SESSION", MODE_PRIVATE)
                            .getString("userEmail", "");
                    if (!new DatabaseHelper(this).changePassword(email, oldPassword, newPassword)) {
                        Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Password updated.", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private EditText passwordField(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return field;
    }

    private void showInfo(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }
}
