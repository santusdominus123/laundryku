package com.dataapk.laundryku;

import java.util.Date;

public class Order {
    private String orderId;
    private String userId;
    private String customerName;
    private String serviceType;
    private double weight;
    private int totalPrice;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String notes;
    private String phoneNumber;
    private String address;

    // Default constructor required for Firestore
    public Order() {
    }

    // Constructor with essential parameters
    public Order(String orderId, String userId, String customerName, String serviceType,
                 double weight, int totalPrice, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerName = customerName;
        this.serviceType = serviceType;
        this.weight = weight;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public double getWeight() {
        return weight;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date(); // Update timestamp when status changes
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", weight=" + weight +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}