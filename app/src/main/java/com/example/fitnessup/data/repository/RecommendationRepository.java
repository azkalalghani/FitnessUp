package com.example.fitnessup.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessup.data.model.NutritionCalculation;
import com.example.fitnessup.data.remote.GeminiApiService;

/**
 * Repository class for managing interactions with the Gemini API for nutritional recommendations.
 * Follows the Repository pattern to abstract API implementation details from the rest of the app.
 */
public class RecommendationRepository {
    private static final String TAG = "RecommendationRepository";
    
    private final GeminiApiService geminiApiService;
    private final MutableLiveData<String> recommendationLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    private static RecommendationRepository instance;
    
    /**
     * Get singleton instance of RecommendationRepository
     * @param apiKey Gemini API key
     * @return RecommendationRepository instance
     */
    public static RecommendationRepository getInstance(String apiKey) {
        if (instance == null) {
            instance = new RecommendationRepository(apiKey);
        }
        return instance;
    }
    
    private RecommendationRepository(String apiKey) {
        geminiApiService = new GeminiApiService(apiKey);
    }
    
    /**
     * Get nutrition recommendations based on calculated nutrition data
     * @param nutritionCalculation The calculated nutrition data
     * @return LiveData containing the recommendation text
     */
    public LiveData<String> getNutritionRecommendation(NutritionCalculation nutritionCalculation) {
        // Reset error state
        errorLiveData.setValue(null);
        
        // Call the Gemini API service
        geminiApiService.getNutritionRecommendation(nutritionCalculation, new GeminiApiService.RecommendationCallback() {
            @Override
            public void onSuccess(String recommendation) {
                recommendationLiveData.postValue(recommendation);
            }
            
            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.postValue(errorMessage);
                // Set a fallback recommendation message
                recommendationLiveData.postValue("Maaf, rekomendasi tidak tersedia saat ini. Silakan coba lagi nanti.");
            }
        });
        
        return recommendationLiveData;
    }
    
    /**
     * Get error status from API calls
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrors() {
        return errorLiveData;
    }
}