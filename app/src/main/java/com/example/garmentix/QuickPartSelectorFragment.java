package com.example.garmentix;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class QuickPartSelectorFragment extends Fragment {

    public interface OnPartColorSelectedListener {
        void onPartSelectedFromFragment(String partKey, String displayName);
        void onColorSelectedFromFragment(int colorValue);
    }

    private TextView chipBody, chipSleeveLeft, chipSleeveRight, chipNeckline, chipCuffs;
    private View swatchRed, swatchBlue, swatchGreen, swatchGold, swatchMaroon, swatchBlack, swatchWhite, swatchPink, swatchPurple, swatchOrange;

    private String currentSelectedPart = "BODY";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quick_part_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipBody = view.findViewById(R.id.chipBody);
        chipSleeveLeft = view.findViewById(R.id.chipSleeveLeft);
        chipSleeveRight = view.findViewById(R.id.chipSleeveRight);
        chipNeckline = view.findViewById(R.id.chipNeckline);
        chipCuffs = view.findViewById(R.id.chipCuffs);

        swatchRed = view.findViewById(R.id.swatchRed);
        swatchBlue = view.findViewById(R.id.swatchBlue);
        swatchGreen = view.findViewById(R.id.swatchGreen);
        swatchGold = view.findViewById(R.id.swatchGold);
        swatchMaroon = view.findViewById(R.id.swatchMaroon);
        swatchBlack = view.findViewById(R.id.swatchBlack);
        swatchWhite = view.findViewById(R.id.swatchWhite);
        swatchPink = view.findViewById(R.id.swatchPink);
        swatchPurple = view.findViewById(R.id.swatchPurple);
        swatchOrange = view.findViewById(R.id.swatchOrange);

        setupPartChipListeners();
        setupColorSwatchListeners();
    }

    private void setupPartChipListeners() {
        chipBody.setOnClickListener(v -> selectPart("BODY", "Body", chipBody));
        chipSleeveLeft.setOnClickListener(v -> selectPart("SLEEVE_LEFT", "Left Sleeve", chipSleeveLeft));
        chipSleeveRight.setOnClickListener(v -> selectPart("SLEEVE_RIGHT", "Right Sleeve", chipSleeveRight));
        chipNeckline.setOnClickListener(v -> selectPart("NECKLINE", "Neckline / Collar", chipNeckline));
        chipCuffs.setOnClickListener(v -> selectPart("CUFFS", "Cuffs", chipCuffs));
    }

    public void updateActivePartChip(String partKey) {
        this.currentSelectedPart = partKey;
        resetChipStyles();
        if ("SLEEVE_LEFT".equals(partKey)) {
            highlightChip(chipSleeveLeft);
        } else if ("SLEEVE_RIGHT".equals(partKey)) {
            highlightChip(chipSleeveRight);
        } else if ("NECKLINE".equals(partKey)) {
            highlightChip(chipNeckline);
        } else if ("CUFFS".equals(partKey)) {
            highlightChip(chipCuffs);
        } else {
            highlightChip(chipBody);
        }
    }

    private void selectPart(String partKey, String displayName, TextView selectedChip) {
        this.currentSelectedPart = partKey;
        resetChipStyles();
        highlightChip(selectedChip);

        if (getActivity() instanceof OnPartColorSelectedListener) {
            ((OnPartColorSelectedListener) getActivity()).onPartSelectedFromFragment(partKey, displayName);
        }
    }

    private void resetChipStyles() {
        TextView[] chips = {chipBody, chipSleeveLeft, chipSleeveRight, chipNeckline, chipCuffs};
        for (TextView chip : chips) {
            if (chip != null) {
                chip.setBackgroundColor(Color.parseColor("#E5E7EB"));
                chip.setTextColor(Color.parseColor("#111827"));
            }
        }
    }

    private void highlightChip(TextView chip) {
        if (chip != null) {
            chip.setBackgroundResource(R.drawable.bg_gold_button);
            chip.setTextColor(Color.WHITE);
        }
    }

    private void setupColorSwatchListeners() {
        swatchRed.setOnClickListener(v -> applyColor(Color.parseColor("#DC2626")));
        swatchBlue.setOnClickListener(v -> applyColor(Color.parseColor("#2563EB")));
        swatchGreen.setOnClickListener(v -> applyColor(Color.parseColor("#16A34A")));
        swatchGold.setOnClickListener(v -> applyColor(Color.parseColor("#D4AF37")));
        swatchMaroon.setOnClickListener(v -> applyColor(Color.parseColor("#800020")));
        swatchBlack.setOnClickListener(v -> applyColor(Color.parseColor("#111827")));
        swatchWhite.setOnClickListener(v -> applyColor(Color.parseColor("#FFFFFF")));
        swatchPink.setOnClickListener(v -> applyColor(Color.parseColor("#EC4899")));
        swatchPurple.setOnClickListener(v -> applyColor(Color.parseColor("#9333EA")));
        swatchOrange.setOnClickListener(v -> applyColor(Color.parseColor("#EA580C")));
    }

    private void applyColor(int colorValue) {
        if (getActivity() instanceof OnPartColorSelectedListener) {
            ((OnPartColorSelectedListener) getActivity()).onColorSelectedFromFragment(colorValue);
        }
    }
}