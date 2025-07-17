package com.dataapk.laundryku;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderListActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {
    private RecyclerView recyclerViewOrders;
    private LinearLayout layoutEmpty;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void initializeViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        orderAdapter.setOnOrderActionListener(this);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Use simpler query to avoid index issues
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(OrderListActivity.this,
                                "Error loading orders: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e("FirestoreError", "Error loading orders", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            try {
                                Order order = document.toObject(Order.class);
                                orderList.add(order);
                            } catch (Exception e) {
                                Log.e("OrderConversion", "Error converting document to Order", e);
                            }
                        }

                        // Sort orders by createdAt descending (newest first)
                        Collections.sort(orderList, new Comparator<Order>() {
                            @Override
                            public int compare(Order o1, Order o2) {
                                if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) {
                                    return 0;
                                }
                                if (o1.getCreatedAt() == null) {
                                    return 1;
                                }
                                if (o2.getCreatedAt() == null) {
                                    return -1;
                                }
                                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                            }
                        });

                        orderAdapter.notifyDataSetChanged();
                        updateUI();
                    } else {
                        orderList.clear();
                        orderAdapter.notifyDataSetChanged();
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (orderList.isEmpty()) {
            recyclerViewOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrderUpdated() {
        // This method is called when an order is successfully updated
        // You can add additional logic here if needed
        Log.d("OrderListActivity", "Order updated successfully");
    }

    @Override
    public void onOrderDeleted() {
        // This method is called when an order is successfully deleted
        // Update UI to show/hide empty state
        updateUI();
        Log.d("OrderListActivity", "Order deleted successfully");
    }

    // Optional: Add method to refresh orders manually
    public void refreshOrders() {
        loadOrders();
    }

    // Optional: Add method to filter orders by status
    public void filterOrdersByStatus(String status) {
        if (orderAdapter != null) {
            List<Order> filteredList = new ArrayList<>();
            for (Order order : orderList) {
                if (order.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(order);
                }
            }
            orderAdapter = new OrderAdapter(this, filteredList);
            orderAdapter.setOnOrderActionListener(this);
            recyclerViewOrders.setAdapter(orderAdapter);
        }
    }

    // Optional: Add method to show all orders (reset filter)
    public void showAllOrders() {
        if (orderAdapter != null) {
            orderAdapter = new OrderAdapter(this, orderList);
            orderAdapter.setOnOrderActionListener(this);
            recyclerViewOrders.setAdapter(orderAdapter);
        }
    }
}