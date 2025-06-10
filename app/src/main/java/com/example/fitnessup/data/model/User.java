package com.example.fitnessup.data.model;

import com.google.firebase.firestore.DocumentId;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a user in the application.
 * This class maps to documents in the 'users' collection in Firestore.
 */
public class User {
    @DocumentId
    private String userId; // Same as Firebase Auth UID
    private String name;
    private String email;
    private int age;
    private String gender; // "PRIA" or "WANITA"
    private double height; // in cm
    private String activityLevel; // e.g., "Aktivitas Sedang"
    private double targetWeight; // in kg
    private double initialWeight; // in kg, saved when account is created

    // Empty constructor required for Firestore
    public User() {
    }

    public User(String userId, String name, String email, int age, String gender,
                double height, String activityLevel, double targetWeight, double initialWeight) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.activityLevel = activityLevel;
        this.targetWeight = targetWeight;
        this.initialWeight = initialWeight;
    }

    // Convert User object to Firestore document
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nama", name);
        map.put("email", email);
        map.put("usia", age);
        map.put("jenisKelamin", gender);
        map.put("tinggiBadan", height);
        map.put("tingkatAktivitas", activityLevel);
        map.put("beratBadanTujuan", targetWeight);
        map.put("beratBadanAwal", initialWeight);
        return map;
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public double getInitialWeight() {
        return initialWeight;
    }

    public void setInitialWeight(double initialWeight) {
        this.initialWeight = initialWeight;
    }
}