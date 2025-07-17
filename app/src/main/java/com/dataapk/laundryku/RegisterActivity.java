package com.dataapk.laundryku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFullName, etUsername, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> validateAndRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void validateAndRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Nama lengkap harus diisi");
            return;
        }
        if (username.isEmpty()) {
            etUsername.setError("Username harus diisi");
            return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email tidak valid");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Nomor telepon harus diisi");
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Alamat harus diisi");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password tidak sama");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Check if username exists
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            btnRegister.setEnabled(true);
                            etUsername.setError("Username sudah digunakan");
                        } else {
                            // Create user in Firebase Auth
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            // Save additional user data to Firestore
                                            String userId = mAuth.getCurrentUser().getUid();

                                            Map<String, Object> user = new HashMap<>();
                                            user.put("userId", userId);
                                            user.put("username", username);
                                            user.put("fullName", fullName);
                                            user.put("email", email);
                                            user.put("phone", phone);
                                            user.put("address", address);

                                            db.collection("users").document(userId)
                                                    .set(user)
                                                    .addOnCompleteListener(dbTask -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        btnRegister.setEnabled(true);

                                                        if (dbTask.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this,
                                                                    "Registrasi berhasil! Silakan login",
                                                                    Toast.LENGTH_SHORT).show();
                                                            navigateToLogin();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this,
                                                                    "Gagal menyimpan data pengguna: " + dbTask.getException().getMessage(),
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            btnRegister.setEnabled(true);
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registrasi gagal: " + authTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Gagal memeriksa username: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}