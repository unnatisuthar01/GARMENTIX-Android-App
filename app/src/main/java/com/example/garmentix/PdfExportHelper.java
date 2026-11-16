package com.example.garmentix;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

final class PdfExportHelper {
    private PdfExportHelper() {
    }

    static void export(Activity activity, item_MyDesign design) {
        try {
            File directory = new File(activity.getFilesDir(), "exports");
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Unable to create PDF export directory");
            }
            File file = new File(directory, "GARMENTIX_" + design.id + ".pdf");
            createPdf(activity, design, file);
            Intent preview = new Intent(activity, PdfPreviewActivity.class);
            preview.putExtra("pdfPath", file.getAbsolutePath());
            activity.startActivity(preview);
        } catch (IOException e) {
            Toast.makeText(activity, "PDF export failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private static void createPdf(Activity activity, item_MyDesign design, File file)
            throws IOException {
        JSONObject data;
        try {
            data = design.designData == null ? new JSONObject() : new JSONObject(design.designData);
        } catch (JSONException e) {
            throw new IOException("Saved design details are invalid", e);
        }
        PdfDocument document = new PdfDocument();
        PdfDocument.Page page = document.startPage(
                new PdfDocument.PageInfo.Builder(595, 842, 1).create());
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawColor(0xFFFAF8F5);
        paint.setColor(0xFF111827);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextSize(25);
        canvas.drawText("GARMENTIX", 40, 54, paint);
        paint.setColor(0xFFD4AF37);
        paint.setTextSize(12);
        canvas.drawText("Design Today, Stitch Tomorrow.", 40, 75, paint);
        paint.setColor(0xFF111827);
        paint.setTextSize(19);
        canvas.drawText(design.name == null ? "Untitled design" : design.name, 40, 112, paint);

        Bitmap preview = null;
        if (design.previewPath != null) {
            preview = BitmapFactory.decodeFile(design.previewPath);
        }
        if (preview == null && design.image != 0) {
            preview = BitmapFactory.decodeResource(activity.getResources(), design.image);
        }
        if (preview != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(preview, 350, 370, true);
            canvas.drawBitmap(scaled, 122, 132, paint);
            if (scaled != preview) scaled.recycle();
            if (!preview.isRecycled()) preview.recycle();
        } else {
            paint.setTextSize(13);
            canvas.drawText("Preview image unavailable", 40, 160, paint);
        }

        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextSize(12);
        int y = 535;
        y = drawRow(canvas, paint, y, "Category", design.category);
        y = drawRow(canvas, paint, y, "Garment template", data.optString("template", "Not selected"));
        y = drawRow(canvas, paint, y, "Fabric", data.optString("fabric", "Not selected"));
        y = drawRow(canvas, paint, y, "Color", data.optString("colorName", "Not selected"));
        y = drawRow(canvas, paint, y, "Sleeve", data.optString("sleeve", "Not selected"));
        y = drawRow(canvas, paint, y, "Neckline", data.optString("neckline", "Not selected"));
        y = drawRow(canvas, paint, y, "Pocket", data.optString("pocket", "Not selected"));
        y = drawRow(canvas, paint, y, "Cuff / button", data.optString("cuff", "Not selected")
                + " / " + data.optString("button", "Not selected"));
        y = drawRow(canvas, paint, y, "Length / fit", data.optString("dressLength", "Not selected")
                + " / " + data.optString("fitStyle", "Not selected"));
        y = drawRow(canvas, paint, y, "Pattern / waist", data.optString("pattern", "Not selected")
                + " / " + data.optString("waist", "Not selected"));
        drawRow(canvas, paint, y, "Created", design.date);
        document.finishPage(page);
        try (FileOutputStream stream = new FileOutputStream(file)) {
            document.writeTo(stream);
        } finally {
            document.close();
        }
    }

    private static int drawRow(Canvas canvas, Paint paint, int y, String label, String value) {
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        canvas.drawText(label + ":", 40, y, paint);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        canvas.drawText(value == null || value.isEmpty() ? "Not selected" : value, 170, y, paint);
        return y + 24;
    }
}
