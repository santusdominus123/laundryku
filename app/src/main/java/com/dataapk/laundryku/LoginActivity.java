package com.dataapk.laundryku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Firebase initialized");

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "User already logged in");
            redirectToMainActivity();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String emailOrUsername = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email/Username and password must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        Log.d(TAG, "Starting login process for: " + emailOrUsername);

        // Check if input is email or username
        if (emailOrUsername.contains("@")) {
            // Direct login with email
            Log.d(TAG, "Login with email");
            mAuth.signInWithEmailAndPassword(emailOrUsername, password)
                    .addOnCompleteListener(authTask -> {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        if (authTask.isSuccessful()) {
                            Log.d(TAG, "Email login successful");
                            Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                            redirectToMainActivity();
                        } else {
                            Log.e(TAG, "Email login failed", authTask.getException());
                            Toast.makeText(LoginActivity.this, "Login gagal: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Login with username - get email from Firestore first
            Log.d(TAG, "Login with username, searching in Firestore");
            db.collection("users")
                    .whereEqualTo("username", emailOrUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firestore query successful");
                            if (!task.getResult().isEmpty()) {
                                String email = task.getResult().getDocuments().get(0).getString("email");
                                Log.d(TAG, "Found email for username: " + email);

                                // Login with email and password
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(authTask -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnLogin.setEnabled(true);

                                            if (authTask.isSuccessful()) {
                                                Log.d(TAG, "Username login successful");
                                                Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                                                redirectToMainActivity();
                                            } else {
                                                Log.e(TAG, "Username login failed", authTask.getException());
                                                Toast.makeText(LoginActivity.this, "Login gagal: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);
                                Log.d(TAG, "Username not found in Firestore");
                                Toast.makeText(LoginActivity.this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            Log.e(TAG, "Firestore query failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}