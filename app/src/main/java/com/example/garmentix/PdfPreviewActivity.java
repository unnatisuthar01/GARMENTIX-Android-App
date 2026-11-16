package com.example.garmentix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfPreviewActivity extends AppCompatActivity {
    private String pdfPath;
    private ActivityResultLauncher<String> createDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);
        pdfPath = getIntent().getStringExtra("pdfPath");
        createDocument = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                this::copyPdfToDownloads
        );
        findViewById(R.id.btnClosePdf).setOnClickListener(v -> finish());
        Button download = findViewById(R.id.btnDownloadPdf);
        Button share = findViewById(R.id.btnSharePdf);
        download.setOnClickListener(v -> createDocument.launch("GARMENTIX_Design.pdf"));
        share.setOnClickListener(v -> sharePdf());
        renderFirstPage();
    }

    private void renderFirstPage() {
        ImageView pageView = findViewById(R.id.imgPdfPage);
        File file = new File(pdfPath == null ? "" : pdfPath);
        if (!file.isFile()) {
            Toast.makeText(this, "PDF file is unavailable", Toast.LENGTH_LONG).show();
            return;
        }
        try (ParcelFileDescriptor descriptor =
                     ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(descriptor);
             PdfRenderer.Page page = renderer.openPage(0)) {
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                    Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Could not preview PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void copyPdfToDownloads(Uri destination) {
        if (destination == null) return;
        try (InputStream input = new FileInputStream(pdfPath);
             OutputStream output = getContentResolver().openOutputStream(destination)) {
            if (output == null) {
                throw new java.io.IOException("Unable to open the selected destination");
            }
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            Toast.makeText(this, "PDF saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not save PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdf() {
        File file = new File(pdfPath == null ? "" : pdfPath);
        if (!file.isFile()) {
            Toast.makeText(this, "PDF file is unavailable", Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share design PDF"));
    }
}
