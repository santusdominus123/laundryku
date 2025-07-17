package com.dataapk.laundryku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private ImageView ivLogout;
    private CardView cardNewOrder, cardOrderList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        ivLogout = findViewById(R.id.ivLogout);
        cardNewOrder = findViewById(R.id.cardNewOrder);
        cardOrderList = findViewById(R.id.cardOrderList);
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        String fullName = doc.getString("fullName");
                        tvWelcome.setText("Welcome, " + fullName + "!");
                    }
                });
    }

    private void setupClickListeners() {
        ivLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        cardNewOrder.setOnClickListener(v ->
                startActivity(new Intent(this, NewOrderActivity.class)));

        cardOrderList.setOnClickListener(v ->
                startActivity(new Intent(this, OrderListActivity.class)));
    }
}