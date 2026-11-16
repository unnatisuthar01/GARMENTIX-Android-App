package com.example.garmentix;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    TextView btnBack;

    EditText edtfullname;
    EditText edtMobilenumber;
    EditText edtEmail;
    EditText edtpassword;
    EditText edtconfpass;

    Button btnRegister;

    DatabaseHelper databaseHelper;
    ScrollView registerScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);

        edtfullname = findViewById(R.id.edtfullname);
        edtMobilenumber = findViewById(R.id.edtMobilenumber);
        edtEmail = findViewById(R.id.edtEmail);
        edtpassword = findViewById(R.id.edtpassword);
        edtconfpass = findViewById(R.id.edtconfpass);
        registerScrollView = findViewById(R.id.registerScrollView);

        btnRegister = findViewById(R.id.btnRegister);

        // Back button
        btnBack.setOnClickListener(view -> finish());
        findViewById(R.id.txtLogin).setOnClickListener(view -> finish());
        setNextField(edtfullname, edtMobilenumber);
        setNextField(edtMobilenumber, edtEmail);
        setNextField(edtEmail, edtpassword);
        setNextField(edtpassword, edtconfpass);
        edtconfpass.setOnEditorActionListener((view, actionId, event) -> {
            boolean pressedDone = actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (pressedDone) {
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Register button
        btnRegister.setOnClickListener(view -> registerUser());
    }

    private void setNextField(EditText current, EditText next) {
        current.setOnEditorActionListener((view, actionId, event) -> {
            boolean pressedNext = actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (!pressedNext) return false;
            next.requestFocus();
            next.post(() -> {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(next, InputMethodManager.SHOW_IMPLICIT);
                }
                if (registerScrollView != null) {
                    Rect visibleBounds = new Rect();
                    next.getDrawingRect(visibleBounds);
                    next.requestRectangleOnScreen(visibleBounds, true);
                }
            });
            return true;
        });
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    edtconfpass.getWindowToken(), 0);
        }
    }

    private void registerUser() {

        String fullname = edtfullname.getText().toString().trim();
        String mobile = edtMobilenumber.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        String password = edtpassword.getText().toString();
        String confirmPassword = edtconfpass.getText().toString();

        // Validate full name
        if (fullname.isEmpty()) {
            edtfullname.setError("Please enter your full name");
            edtfullname.requestFocus();
            return;
        }

        // Validate mobile number
        if (mobile.isEmpty()) {
            edtMobilenumber.setError("Please enter mobile number");
            edtMobilenumber.requestFocus();
            return;
        }

        if (!mobile.matches("[0-9]{10}")) {
            edtMobilenumber.setError(
                    "Enter a valid 10-digit mobile number"
            );
            edtMobilenumber.requestFocus();
            return;
        }

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
            edtpassword.setError("Please enter password");
            edtpassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtpassword.setError(
                    "Password must contain at least 6 characters"
            );
            edtpassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            edtconfpass.setError("Please confirm your password");
            edtconfpass.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtconfpass.setError("Passwords do not match");
            edtconfpass.requestFocus();
            return;
        }

        // Save user details in SQLite
        boolean isRegistered = databaseHelper.registerUser(
                fullname,
                mobile,
                email,
                password
        );

        if (isRegistered) {
            Toast.makeText(
                    RegisterActivity.this,
                    "Registration successful. Please log in.",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );
            intent.putExtra("registeredEmail", email);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

        } else {

            Toast.makeText(
                    RegisterActivity.this,
                    "Registration failed. Email may already exist.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}