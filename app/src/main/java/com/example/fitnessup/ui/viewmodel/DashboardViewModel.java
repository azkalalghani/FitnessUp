package com.example.fitnessup.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.fitnessup.R;
import com.example.fitnessup.data.model.NutritionCalculation;
import com.example.fitnessup.data.model.User;
import com.example.fitnessup.data.model.WeightProgress;
import com.example.fitnessup.data.repository.RecommendationRepository;
import com.example.fitnessup.data.repository.UserRepository;
import com.example.fitnessup.util.calculator.NutritionCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * ViewModel for the main dashboard screen.
 * Manages user profile data, nutrition calculations, and recommendations.
 */
public class DashboardViewModel extends AndroidViewModel {
    private static final String TAG = "DashboardViewModel";

    private final UserRepository userRepository;
    private RecommendationRepository recommendationRepository; // Removed final to allow initialization in try-catch
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MediatorLiveData<NutritionCalculation> nutritionCalculation = new MediatorLiveData<>();
    private LiveData<String> recommendation;

    // Cached data
    private LiveData<User> userProfile;
    private LiveData<WeightProgress> latestWeight;
    private LiveData<List<WeightProgress>> weightHistory;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
        
        // Initialize with a null value by default
        recommendationRepository = null;
        
        try {
            // Get the Gemini API key from resources
            String geminiApiKey = application.getApplicationContext().getString(R.string.gemini_api_key);
            recommendationRepository = RecommendationRepository.getInstance(geminiApiKey);
        } catch (Exception e) {
            Log.e(TAG, "Error getting API key: " + e.getMessage());
            errorMessage.setValue("Unable to initialize AI recommendations: " + e.getMessage());
        }

        // Initialize data when current user changes
        FirebaseUser currentUser = userRepository.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        }
    }

    /**
     * Load all user-related data for the given user ID
     * @param userId User ID to load data for
     */
    public void loadUserData(String userId) {
        isLoading.setValue(true);
        
        // Clear any previous data
        nutritionCalculation.setValue(null);
        
        // Load user profile
        userProfile = userRepository.getUserProfile(userId);
        
        // Load latest weight
        latestWeight = userRepository.getLatestWeight(userId);
        
        // Load weight history
        weightHistory = userRepository.getWeightProgressHistory(userId);
        
        // Calculate nutrition when both user profile and latest weight are available
        nutritionCalculation.addSource(userProfile, user -> calculateNutrition(user, latestWeight.getValue()));
        nutritionCalculation.addSource(latestWeight, weight -> calculateNutrition(userProfile.getValue(), weight));
        
        // Get recommendation when nutrition calculation is available
        if (recommendationRepository != null) {
            recommendation = Transformations.switchMap(nutritionCalculation, 
                    nutrition -> nutrition != null ? recommendationRepository.getNutritionRecommendation(nutrition) : null);
        } else {
            // Fallback if recommendation repository initialization failed
            recommendation = new MutableLiveData<>("API key configuration issue. Recommendations unavailable.");
        }
        
        isLoading.setValue(false);
    }
    
    /**
     * Calculate nutrition requirements based on user profile and current weight
     */
    private void calculateNutrition(User user, WeightProgress latestWeight) {
        if (user == null || latestWeight == null) {
            return; // Can't calculate without both pieces of data
        }
        
        try {
            NutritionCalculation calculation = NutritionCalculator.calculateNutrition(user, latestWeight.getWeight());
            nutritionCalculation.setValue(calculation);
        } catch (Exception e) {
            errorMessage.setValue("Error calculating nutrition: " + e.getMessage());
        }
    }
    
    /**
     * Add a new weight progress entry
     * @param weight New weight to record
     */
    public void addWeightProgress(double weight) {
        FirebaseUser user = userRepository.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("User not logged in");
            return;
        }
        
        isLoading.setValue(true);
        WeightProgress newProgress = new WeightProgress(user.getUid(), weight);
        userRepository.addWeightProgress(newProgress)
                .addOnSuccessListener(documentReference -> {
                    isLoading.setValue(false);
                    // Reload the latest weight
                    loadUserData(user.getUid());
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to add weight progress: " + e.getMessage());
                });
    }
    
    /**
     * Update user profile data
     * @param user Updated user object
     */
    public void updateUserProfile(User user) {
        isLoading.setValue(true);
        userRepository.updateUserProfile(user)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    // Reload user data to refresh calculations
                    loadUserData(user.getUserId());
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update profile: " + e.getMessage());
                });
    }
    
    /**
     * Get loading state
     */
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
    
    /**
     * Get error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get user profile data
     */
    public LiveData<User> getUserProfile() {
        return userProfile;
    }
    
    /**
     * Get latest weight
     */
    public LiveData<WeightProgress> getLatestWeight() {
        return latestWeight;
    }
    
    /**
     * Get weight history
     */
    public LiveData<List<WeightProgress>> getWeightHistory() {
        return weightHistory;
    }
    
    /**
     * Get nutrition calculation
     */
    public LiveData<NutritionCalculation> getNutritionCalculation() {
        return nutritionCalculation;
    }
    
    /**
     * Get nutrition recommendations
     */
    public LiveData<String> getRecommendation() {
        return recommendation;
    }
}