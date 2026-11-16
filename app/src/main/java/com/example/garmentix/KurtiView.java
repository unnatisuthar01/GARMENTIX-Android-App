package com.example.garmentix;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class KurtiView extends View {

    public enum GarmentPart {
        BODY("Body"),
        LEFT_SLEEVE("Left Sleeve"),
        RIGHT_SLEEVE("Right Sleeve"),
        NECKLINE("Neckline / Collar"),
        LEFT_CUFF("Left Cuff"),
        RIGHT_CUFF("Right Cuff"),
        LEFT_POCKET("Left Pocket"),
        RIGHT_POCKET("Right Pocket");

        private final String displayName;

        GarmentPart(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public interface OnPartSelectedListener {
        void onPartSelected(GarmentPart part, String partName);
    }

    private static final float CANVAS_WIDTH = 1000f;
    private static final float CANVAS_HEIGHT = 1400f;

    private final Map<GarmentPart, Path> partPaths = new EnumMap<>(GarmentPart.class);
    private final Map<GarmentPart, Integer> partColors = new EnumMap<>(GarmentPart.class);

    private GarmentPart selectedPart = GarmentPart.BODY;
    private OnPartSelectedListener onPartSelectedListener;

    private Paint fillPaint;
    private Paint strokePaint;
    private Paint highlightPaint;
    private Paint seamPaint;

    private Matrix transformMatrix = new Matrix();
    private Matrix inverseMatrix = new Matrix();

    public KurtiView(Context context) {
        super(context);
        init();
    }

    public KurtiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KurtiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.parseColor("#111827"));
        strokePaint.setStrokeWidth(4f);

        seamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        seamPaint.setStyle(Paint.Style.STROKE);
        seamPaint.setColor(Color.parseColor("#374151"));
        seamPaint.setStrokeWidth(2f);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setColor(Color.parseColor("#D4AF37"));
        highlightPaint.setStrokeWidth(8f);
        highlightPaint.setPathEffect(new DashPathEffect(new float[]{16f, 12f}, 0f));

        // Default Kurti section colors (distinct elegant defaults)
        partColors.put(GarmentPart.BODY, Color.parseColor("#F5E6D3"));        // Cream / Off-white
        partColors.put(GarmentPart.LEFT_SLEEVE, Color.parseColor("#800020"));  // Maroon
        partColors.put(GarmentPart.RIGHT_SLEEVE, Color.parseColor("#800020")); // Maroon
        partColors.put(GarmentPart.NECKLINE, Color.parseColor("#D4AF37"));     // Gold accent
        partColors.put(GarmentPart.LEFT_CUFF, Color.parseColor("#D4AF37"));    // Gold accent
        partColors.put(GarmentPart.RIGHT_CUFF, Color.parseColor("#D4AF37"));   // Gold accent
        partColors.put(GarmentPart.LEFT_POCKET, Color.parseColor("#800020"));  // Maroon
        partColors.put(GarmentPart.RIGHT_POCKET, Color.parseColor("#800020")); // Maroon

        buildPartPaths();
    }

    private void buildPartPaths() {
        // 1. Neckline
        Path necklinePath = new Path();
        necklinePath.moveTo(380f, 160f);
        necklinePath.cubicTo(420f, 320f, 580f, 320f, 620f, 160f);
        necklinePath.lineTo(500f, 380f);
        necklinePath.close();
        partPaths.put(GarmentPart.NECKLINE, necklinePath);

        // 2. Body
        Path bodyPath = new Path();
        bodyPath.moveTo(380f, 160f);
        bodyPath.lineTo(280f, 360f); // left armpit
        bodyPath.cubicTo(300f, 650f, 260f, 950f, 220f, 1280f); // left flare
        bodyPath.lineTo(500f, 1250f); // center hem
        bodyPath.lineTo(780f, 1280f); // right flare
        bodyPath.cubicTo(740f, 950f, 700f, 650f, 720f, 360f); // right armpit
        bodyPath.lineTo(620f, 160f); // right neck
        bodyPath.lineTo(500f, 380f); // neck cutout
        bodyPath.close();
        partPaths.put(GarmentPart.BODY, bodyPath);

        // 3. Left Sleeve
        Path leftSleevePath = new Path();
        leftSleevePath.moveTo(380f, 160f);
        leftSleevePath.lineTo(120f, 500f);
        leftSleevePath.lineTo(100f, 620f);
        leftSleevePath.lineTo(180f, 650f);
        leftSleevePath.lineTo(280f, 360f);
        leftSleevePath.close();
        partPaths.put(GarmentPart.LEFT_SLEEVE, leftSleevePath);

        // 4. Right Sleeve
        Path rightSleevePath = new Path();
        rightSleevePath.moveTo(620f, 160f);
        rightSleevePath.lineTo(880f, 500f);
        rightSleevePath.lineTo(900f, 620f);
        rightSleevePath.lineTo(820f, 650f);
        rightSleevePath.lineTo(720f, 360f);
        rightSleevePath.close();
        partPaths.put(GarmentPart.RIGHT_SLEEVE, rightSleevePath);

        // 5. Left Cuff
        Path leftCuffPath = new Path();
        leftCuffPath.moveTo(100f, 620f);
        leftCuffPath.lineTo(180f, 650f);
        leftCuffPath.lineTo(170f, 700f);
        leftCuffPath.lineTo(90f, 670f);
        leftCuffPath.close();
        partPaths.put(GarmentPart.LEFT_CUFF, leftCuffPath);

        // 6. Right Cuff
        Path rightCuffPath = new Path();
        rightCuffPath.moveTo(900f, 620f);
        rightCuffPath.lineTo(820f, 650f);
        rightCuffPath.lineTo(830f, 700f);
        rightCuffPath.lineTo(910f, 670f);
        rightCuffPath.close();
        partPaths.put(GarmentPart.RIGHT_CUFF, rightCuffPath);

        // 7. Left Pocket
        Path leftPocketPath = new Path();
        leftPocketPath.moveTo(330f, 740f);
        leftPocketPath.lineTo(430f, 740f);
        leftPocketPath.lineTo(430f, 850f);
        leftPocketPath.cubicTo(430f, 880f, 330f, 880f, 330f, 850f);
        leftPocketPath.close();
        partPaths.put(GarmentPart.LEFT_POCKET, leftPocketPath);

        // 8. Right Pocket
        Path rightPocketPath = new Path();
        rightPocketPath.moveTo(570f, 740f);
        rightPocketPath.lineTo(670f, 740f);
        rightPocketPath.lineTo(670f, 850f);
        rightPocketPath.cubicTo(670f, 880f, 570f, 880f, 570f, 850f);
        rightPocketPath.close();
        partPaths.put(GarmentPart.RIGHT_POCKET, rightPocketPath);
    }

    public void setOnPartSelectedListener(OnPartSelectedListener listener) {
        this.onPartSelectedListener = listener;
    }

    public GarmentPart getSelectedPart() {
        return selectedPart;
    }

    public void setSelectedPart(GarmentPart part) {
        if (part != null) {
            this.selectedPart = part;
            invalidate();
            if (onPartSelectedListener != null) {
                onPartSelectedListener.onPartSelected(part, part.getDisplayName());
            }
        }
    }

    public void setPartColor(GarmentPart part, int color) {
        if (part != null) {
            partColors.put(part, color);
            invalidate();
        }
    }

    public int getPartColor(GarmentPart part) {
        Integer color = partColors.get(part);
        return color != null ? color : Color.WHITE;
    }

    public Map<GarmentPart, Integer> getPartColorsMap() {
        return new EnumMap<>(partColors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) return;

        // Calculate scaling & centering matrix
        float scale = Math.min(viewWidth / CANVAS_WIDTH, viewHeight / CANVAS_HEIGHT) * 0.92f;
        float dx = (viewWidth - (CANVAS_WIDTH * scale)) / 2f;
        float dy = (viewHeight - (CANVAS_HEIGHT * scale)) / 2f;

        transformMatrix.reset();
        transformMatrix.postScale(scale, scale);
        transformMatrix.postTranslate(dx, dy);

        transformMatrix.invert(inverseMatrix);

        canvas.save();
        canvas.concat(transformMatrix);

        // Draw parts in layered back-to-front order
        GarmentPart[] renderOrder = new GarmentPart[]{
                GarmentPart.BODY,
                GarmentPart.LEFT_SLEEVE,
                GarmentPart.RIGHT_SLEEVE,
                GarmentPart.NECKLINE,
                GarmentPart.LEFT_CUFF,
                GarmentPart.RIGHT_CUFF,
                GarmentPart.LEFT_POCKET,
                GarmentPart.RIGHT_POCKET
        };

        // 1. Draw Fills & Outlines
        for (GarmentPart part : renderOrder) {
            Path path = partPaths.get(part);
            if (path == null) continue;

            Integer color = partColors.get(part);
            fillPaint.setColor(color != null ? color : Color.LTGRAY);

            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }

        // 2. Highlight currently selected part
        if (selectedPart != null && partPaths.containsKey(selectedPart)) {
            Path path = partPaths.get(selectedPart);
            if (path != null) {
                canvas.drawPath(path, highlightPaint);
            }
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float[] pts = new float[]{event.getX(), event.getY()};
            inverseMatrix.mapPoints(pts);

            float touchX = pts[0];
            float touchY = pts[1];

            // Priority check order: top layers first
            GarmentPart[] hitCheckOrder = new GarmentPart[]{
                    GarmentPart.LEFT_POCKET,
                    GarmentPart.RIGHT_POCKET,
                    GarmentPart.LEFT_CUFF,
                    GarmentPart.RIGHT_CUFF,
                    GarmentPart.NECKLINE,
                    GarmentPart.LEFT_SLEEVE,
                    GarmentPart.RIGHT_SLEEVE,
                    GarmentPart.BODY
            };

            for (GarmentPart part : hitCheckOrder) {
                Path path = partPaths.get(part);
                if (path != null && isPointInPath(path, touchX, touchY)) {
                    setSelectedPart(part);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isPointInPath(Path path, float x, float y) {
        Region region = new Region();
        region.setPath(
                path,
                new Region(0, 0, (int) CANVAS_WIDTH, (int) CANVAS_HEIGHT)
        );

        return region.contains((int) x, (int) y);
    }
}