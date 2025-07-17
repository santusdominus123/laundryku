package com.dataapk.laundryku;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onOrderUpdated();
        void onOrderDeleted();
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getOrderId());
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvServiceType.setText(order.getServiceType());
        holder.tvWeight.setText(order.getWeight() + " kg");
        holder.tvTotalPrice.setText("Rp " + String.format(Locale.getDefault(), "%,d", order.getTotalPrice()));
        holder.tvStatus.setText(order.getStatus());

        // Format created date
        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvCreatedAt.setText(sdf.format(order.getCreatedAt()));
        }

        // Set status background color
        setStatusBackground(holder.tvStatus, order.getStatus());

        // Set click listeners for action buttons
        holder.btnUpdate.setOnClickListener(v -> showUpdateDialog(order, position));
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(order, position));
    }

    private void setStatusBackground(TextView tvStatus, String status) {
        int backgroundColor;
        switch (status.toLowerCase()) {
            case "pending":
                backgroundColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "processing":
                backgroundColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case "completed":
                backgroundColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
                backgroundColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                backgroundColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        tvStatus.setBackgroundColor(backgroundColor);
    }

    private void showUpdateDialog(Order order, int position) {
        String[] statusOptions = {"Pending", "Processing", "Completed", "Cancelled"};
        int currentStatusIndex = 0;

        // Find current status index
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(order.getStatus())) {
                currentStatusIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Order Status")
                .setSingleChoiceItems(statusOptions, currentStatusIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (selectedIndex != -1) {
                        updateOrderStatus(order, statusOptions[selectedIndex], position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Order order, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Order")
                .setMessage("Are you sure you want to delete this order?\n\nOrder ID: " + order.getOrderId())
                .setPositiveButton("Delete", (dialog, which) -> deleteOrder(order, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus, int position) {
        // Update in Firestore
        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Update local list
                    order.setStatus(newStatus);
                    notifyItemChanged(position);

                    Toast.makeText(context, "Order status updated successfully", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onOrderUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteOrder(Order order, int position) {
        // Delete from Firestore
        db.collection("orders").document(order.getOrderId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    orderList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, orderList.size());

                    Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onOrderDeleted();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvServiceType, tvWeight, tvTotalPrice, tvStatus, tvCreatedAt;
        ImageButton btnUpdate, btnDelete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}