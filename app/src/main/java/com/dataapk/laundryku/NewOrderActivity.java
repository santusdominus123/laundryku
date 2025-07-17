package com.dataapk.laundryku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.*;

public class NewOrderActivity extends AppCompatActivity {
    private EditText etCustomerName, etCustomerPhone, etCustomerAddress, etWeight, etNotes;
    private Spinner spinnerServiceType;
    private TextView tvTotalPrice;
    private Button btnSubmit;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etCustomerAddress = findViewById(R.id.etCustomerAddress);
        spinnerServiceType = findViewById(R.id.spinnerServiceType);
        etWeight = findViewById(R.id.etWeight);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        etNotes = findViewById(R.id.etNotes);
        btnSubmit = findViewById(R.id.btnSubmitOrder);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceType.setAdapter(adapter);
    }

    private void setupListeners() {
        // Listener untuk menghitung total saat weight berubah
        etWeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) calculateTotal();
        });

        // Listener untuk spinner service type
        spinnerServiceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) saveOrder();
        });
    }

    private boolean validateInput() {
        if (etCustomerName.getText().toString().trim().isEmpty()) {
            etCustomerName.setError("Nama pelanggan harus diisi");
            etCustomerName.requestFocus();
            return false;
        }

        if (etCustomerPhone.getText().toString().trim().isEmpty()) {
            etCustomerPhone.setError("No telepon harus diisi");
            etCustomerPhone.requestFocus();
            return false;
        }

        if (etCustomerAddress.getText().toString().trim().isEmpty()) {
            etCustomerAddress.setError("Alamat harus diisi");
            etCustomerAddress.requestFocus();
            return false;
        }

        if (etWeight.getText().toString().trim().isEmpty()) {
            etWeight.setError("Berat harus diisi");
            etWeight.requestFocus();
            return false;
        }

        try {
            double weight = Double.parseDouble(etWeight.getText().toString());
            if (weight <= 0) {
                etWeight.setError("Berat harus lebih dari 0");
                etWeight.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etWeight.setError("Format berat tidak valid");
            etWeight.requestFocus();
            return false;
        }

        return true;
    }

    private void calculateTotal() {
        try {
            String weightStr = etWeight.getText().toString().trim();
            if (weightStr.isEmpty()) {
                tvTotalPrice.setText("Rp 0");
                return;
            }

            String service = spinnerServiceType.getSelectedItem().toString();
            double weight = Double.parseDouble(weightStr);
            double price = getPricePerKg(service);
            double total = weight * price;
            tvTotalPrice.setText(String.format(Locale.getDefault(), "Rp %,.0f", total));
        } catch (Exception e) {
            tvTotalPrice.setText("Rp 0");
        }
    }

    private double getPricePerKg(String service) {
        switch (service) {
            case "Cuci Kering": return 8000;
            case "Cuci Setrika": return 10000;
            case "Dry Cleaning": return 15000;
            default: return 0;
        }
    }

    private void saveOrder() {
        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double submission
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Menyimpan...");

        String orderId = UUID.randomUUID().toString();
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("customerName", etCustomerName.getText().toString().trim());
        order.put("customerPhone", etCustomerPhone.getText().toString().trim());
        order.put("customerAddress", etCustomerAddress.getText().toString().trim());
        order.put("serviceType", spinnerServiceType.getSelectedItem().toString());
        order.put("weight", Double.parseDouble(etWeight.getText().toString()));
        order.put("totalPrice", getTotalPriceFromTextView());
        order.put("status", "Pending");
        order.put("notes", etNotes.getText().toString().trim());
        order.put("userId", userId);
        order.put("createdAt", Timestamp.now());

        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pesanan berhasil disimpan!", Toast.LENGTH_SHORT).show();

                    // Navigate to OrderListActivity to show the new order
                    Intent intent = new Intent(NewOrderActivity.this, OrderListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // Finish current activity
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Re-enable button if error occurs
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Order");
                });
    }

    private double getTotalPriceFromTextView() {
        try {
            String priceStr = tvTotalPrice.getText().toString()
                    .replace("Rp ", "")
                    .replace(",", "");
            return Double.parseDouble(priceStr);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Re-enable button when activity is destroyed
        if (btnSubmit != null) {
            btnSubmit.setEnabled(true);
            btnSubmit.setText("Submit Order");
        }
    }
}