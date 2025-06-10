package com.example.fitnessup.data.remote;

import android.util.Log;

import com.example.fitnessup.data.model.NutritionCalculation;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Service class for interacting with the Gemini API to get AI-powered nutrition recommendations
 */
public class GeminiApiService {
    private static final String TAG = "GeminiApiService";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static final String API_VERSION = "v1beta";
    private static final String MODEL = "gemini-pro";
    
    private final GeminiApi geminiApi;
    private final String apiKey;
    
    // Callback interface for asynchronous recommendation results
    public interface RecommendationCallback {
        void onSuccess(String recommendation);
        void onFailure(String errorMessage);
    }
    
    // Constructor
    public GeminiApiService(String apiKey) {
        this.apiKey = apiKey;
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        geminiApi = retrofit.create(GeminiApi.class);
    }
    
    /**
     * Get personalized nutrition recommendations based on calculated nutrition data
     * @param nutritionCalculation The calculated nutrition data
     * @param callback Callback to handle the result
     */
    public void getNutritionRecommendation(NutritionCalculation nutritionCalculation, RecommendationCallback callback) {
        // Construct the prompt for Gemini
        String goal = nutritionCalculation.getGoal();
        String goalInIndonesian;
        
        // Convert goal to Indonesian
        switch (goal) {
            case "WEIGHT_LOSS":
                goalInIndonesian = "penurunan berat badan";
                break;
            case "WEIGHT_GAIN":
                goalInIndonesian = "penambahan berat badan";
                break;
            case "MAINTENANCE":
            default:
                goalInIndonesian = "pemeliharaan berat badan";
                break;
        }
        
        // Create the prompt
        String prompt = String.format(
                "Anda adalah seorang ahli gizi. " +
                "Berikan anjuran makanan dan larangan untuk seorang pengguna dengan tujuan %s " +
                "dan target %.0f kkal per hari. " +
                "Kebutuhan makronya adalah %.0fg Protein, %.0fg Karbohidrat, dan %.0fg Lemak. " +
                "Berikan contoh makanan lokal Indonesia dan sajikan dalam format Markdown yang menarik.",
                goalInIndonesian,
                nutritionCalculation.getDailyCalorieTarget(),
                nutritionCalculation.getProteinGrams(),
                nutritionCalculation.getCarbGrams(),
                nutritionCalculation.getFatGrams()
        );
        
        // Create request body
        Map<String, Object> requestBody = createRequestBody(prompt);
        
        // Make API call
        Call<GeminiResponse> call = geminiApi.generateContent(
                "Bearer " + apiKey,
                MODEL,
                requestBody
        );
        
        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse geminiResponse = response.body();
                    if (geminiResponse.candidates != null && !geminiResponse.candidates.isEmpty()) {
                        String recommendation = extractTextFromResponse(geminiResponse);
                        callback.onSuccess(recommendation);
                    } else {
                        callback.onFailure("Empty response from Gemini API");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onFailure("API Error: " + errorBody);
                    } catch (IOException e) {
                        callback.onFailure("API Error: " + e.getMessage());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                callback.onFailure("Network Error: " + t.getMessage());
                Log.e(TAG, "API call failed", t);
            }
        });
    }
    
    /**
     * Extract text content from Gemini API response
     */
    private String extractTextFromResponse(GeminiResponse response) {
        StringBuilder result = new StringBuilder();
        for (GeminiResponse.Candidate candidate : response.candidates) {
            if (candidate.content != null && candidate.content.parts != null) {
                for (GeminiResponse.Candidate.Content.Part part : candidate.content.parts) {
                    if (part.text != null) {
                        result.append(part.text);
                    }
                }
            }
        }
        return result.toString();
    }
    
    /**
     * Create the request body for the Gemini API
     */
    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> textPart = new HashMap<>();
        
        textPart.put("text", prompt);
        parts.add(textPart);
        contents.put("parts", parts);
        
        List<Map<String, Object>> contentsList = new ArrayList<>();
        contentsList.add(contents);
        
        requestBody.put("contents", contentsList);
        
        // Add optional parameters
        requestBody.put("temperature", 0.7);
        requestBody.put("maxOutputTokens", 800);
        
        return requestBody;
    }
    
    /**
     * Retrofit interface for Gemini API
     */
    interface GeminiApi {
        @POST("{model}:generateContent")
        Call<GeminiResponse> generateContent(
                @Header("Authorization") String authorization,
                @retrofit2.http.Path("model") String model,
                @Body Map<String, Object> body
        );
    }
    
    /**
     * Response model for Gemini API
     */
    static class GeminiResponse {
        @SerializedName("candidates")
        List<Candidate> candidates;
        
        static class Candidate {
            @SerializedName("content")
            Content content;
            
            static class Content {
                @SerializedName("parts")
                List<Part> parts;
                
                static class Part {
                    @SerializedName("text")
                    String text;
                }
            }
        }
    }
}