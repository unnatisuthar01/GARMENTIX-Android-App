package com.example.garmentix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtEmail;
    TextInputEditText edtPassword;

    Button btnLogin;
    TextView txtRegister;

    DatabaseHelper databaseHelper;

    SharedPreferences sessionPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize session preferences
        sessionPreferences = getSharedPreferences(
                "GARMENTIX_SESSION",
                MODE_PRIVATE
        );

        // Check existing login session
        boolean isLoggedIn = sessionPreferences.getBoolean(
                "isLoggedIn",
                false
        );

        if (isLoggedIn) {

            String savedEmail = sessionPreferences.getString(
                    "userEmail",
                    ""
            );

            if (!savedEmail.trim().isEmpty()) {
                openHomeScreen(savedEmail);
                return;
            } else {
                // Invalid session: clear it and show Login
                sessionPreferences.edit().clear().apply();
            }
        }

        // Initialize views
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        android.widget.ScrollView loginScrollView = findViewById(R.id.loginScrollView);
        ViewCompat.setOnApplyWindowInsetsListener(loginScrollView, (view, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navigationBottom =
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.bottomMargin = Math.max(imeBottom, navigationBottom);
            view.setLayoutParams(layoutParams);
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    0
            );
            if (imeBottom > 0 && edtPassword.hasFocus()) {
                edtPassword.post(() -> {
                    Rect visibleBounds = new Rect();
                    edtPassword.getDrawingRect(visibleBounds);
                    edtPassword.requestRectangleOnScreen(visibleBounds, true);
                });
            }
            return insets;
        });
        String registeredEmail = getIntent().getStringExtra("registeredEmail");
        if (registeredEmail != null) {
            edtEmail.setText(registeredEmail);
        }

        // Login button
        btnLogin.setOnClickListener(view -> loginUser());

        edtEmail.setOnEditorActionListener((view, actionId, event) -> {
            boolean pressedEnter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId != EditorInfo.IME_ACTION_NEXT && !pressedEnter) {
                return false;
            }
            focusAndShowPassword(loginScrollView);
            return true;
        });

        // Open Register screen
        txtRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

        });

    }

    private void focusAndShowPassword(android.widget.ScrollView loginScrollView) {
        edtPassword.requestFocus();
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        edtPassword.post(() -> {
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(
                        edtPassword,
                        InputMethodManager.SHOW_IMPLICIT
                );
            }
            ViewCompat.requestApplyInsets(loginScrollView);
            Rect visibleBounds = new Rect();
            edtPassword.getDrawingRect(visibleBounds);
            edtPassword.requestRectangleOnScreen(visibleBounds, true);
        });
    }

    private void loginUser() {

        String email = edtEmail.getText() != null
                ? edtEmail.getText().toString().trim()
                : "";
        email = email.toLowerCase(java.util.Locale.ROOT);

        String password = edtPassword.getText() != null
                ? edtPassword.getText().toString()
                : "";

        // Validate email
        if (email.isEmpty()) {
            edtEmail.setError("Please enter email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email address");
            edtEmail.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            edtPassword.setError("Please enter password");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError(
                    "Password must contain at least 6 characters"
            );
            edtPassword.requestFocus();
            return;
        }

        // Check credentials using SQLite
        boolean isValidUser = databaseHelper.checkLogin(
                email,
                password
        );

        if (isValidUser) {

            // Save login session
            sessionPreferences.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("userEmail", email)
                    .apply();

            Toast.makeText(
                    LoginActivity.this,
                    "Login successful!",
                    Toast.LENGTH_SHORT
            ).show();

            // Open Home screen
            openHomeScreen(email);

        } else {
            String message = databaseHelper.userExists(email)
                    ? "Invalid email or password"
                    : "Account not found. Please register first.";
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        }

    }

    private void openHomeScreen(String email) {

        Intent intent = new Intent(
                LoginActivity.this,
                HomeActivity.class
        );

        intent.putExtra("userEmail", email);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}