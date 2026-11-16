package com.example.garmentix;

import android.os.Bundle;

public class SavedDesignActivity extends MyDesignActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra("favoritesOnly", true);
        super.onCreate(savedInstanceState);
    }
}
