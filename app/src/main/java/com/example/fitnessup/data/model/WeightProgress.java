package com.example.fitnessup.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a weight progress entry in the application.
 * This class maps to documents in the 'progress' collection in Firestore.
 */
public class WeightProgress {
    @DocumentId
    private String progressId;
    private String userId; // Reference to the user who created this entry
    private double weight; // Weight in kg
    private Timestamp timestamp; // Date and time when the entry was created

    // Empty constructor required for Firestore
    public WeightProgress() {
    }

    public WeightProgress(String userId, double weight) {
        this.userId = userId;
        this.weight = weight;
        this.timestamp = Timestamp.now();
    }

    public WeightProgress(String progressId, String userId, double weight, Timestamp timestamp) {
        this.progressId = progressId;
        this.userId = userId;
        this.weight = weight;
        this.timestamp = timestamp;
    }

    // Convert WeightProgress object to Firestore document
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("beratBadan", weight);
        map.put("timestamp", timestamp);
        return map;
    }

    // Getters and Setters

    public String getProgressId() {
        return progressId;
    }

    public void setProgressId(String progressId) {
        this.progressId = progressId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return timestamp.toDate();
    }
}