package com.example.garmentix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        findViewById(R.id.btnHelpBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAboutGarmentix).setOnClickListener(v ->
                show("About GARMENTIX",
                        "A clothing design workspace for exploring garment templates, fabric, color, and saved design specifications."));
        findViewById(R.id.btnContactSupport).setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
            email.putExtra(Intent.EXTRA_SUBJECT, "GARMENTIX support");
            if (email.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(email, "Contact support"));
            } else {
                Toast.makeText(this, "No email application is available.", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btnFaq).setOnClickListener(v -> show("Frequently Asked Questions",
                "How do I save a design?\nOpen Design Cloth, choose the available options, then tap Save Design.\n\n"
                        + "Where are my saved designs?\nFind them under My Designs. Favorite designs appear under Saved Designs.\n\n"
                        + "Are sleeve and neckline silhouettes rendered?\nShape-changing artwork is currently available only where matching transparent assets exist."));
    }

    private void show(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }
}
