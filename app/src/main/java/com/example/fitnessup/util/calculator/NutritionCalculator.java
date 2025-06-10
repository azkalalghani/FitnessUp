package com.example.fitnessup.util.calculator;

import com.example.fitnessup.data.model.NutritionCalculation;
import com.example.fitnessup.data.model.User;
import com.example.fitnessup.data.model.WeightProgress;

/**
 * Utility class for calculating nutrition requirements including BMR, TDEE,
 * and macronutrient distributions based on user data.
 */
public class NutritionCalculator {

    // Activity level multipliers
    private static final double SEDENTARY_MULTIPLIER = 1.2;      // Little or no exercise
    private static final double LIGHT_MULTIPLIER = 1.375;        // Light exercise 1-3 days/week
    private static final double MODERATE_MULTIPLIER = 1.55;      // Moderate exercise 3-5 days/week
    private static final double ACTIVE_MULTIPLIER = 1.725;       // Heavy exercise 6-7 days/week
    private static final double VERY_ACTIVE_MULTIPLIER = 1.9;    // Very heavy exercise, physical job or 2x training

    // Calorie adjustment for weight goals
    private static final double WEIGHT_LOSS_MULTIPLIER = 0.85;   // 15% calorie deficit
    private static final double WEIGHT_GAIN_MULTIPLIER = 1.15;   // 15% calorie surplus

    // Macronutrient ratios based on goals (protein/carbs/fats)
    private static final double[] WEIGHT_LOSS_RATIO = {0.35, 0.40, 0.25};    // Higher protein for muscle preservation
    private static final double[] MAINTENANCE_RATIO = {0.30, 0.45, 0.25};    // Balanced macros
    private static final double[] WEIGHT_GAIN_RATIO = {0.25, 0.50, 0.25};    // Higher carbs for energy

    // Calorie content per gram of macronutrients
    private static final int PROTEIN_CALORIES_PER_GRAM = 4;
    private static final int CARB_CALORIES_PER_GRAM = 4;
    private static final int FAT_CALORIES_PER_GRAM = 9;

    /**
     * Calculate full nutrition requirements based on user data and current weight
     * @param user The user's profile data
     * @param currentWeight The user's current weight (from latest progress entry)
     * @return NutritionCalculation object with all calculated values
     */
    public static NutritionCalculation calculateNutrition(User user, double currentWeight) {
        // Step A: Determine goal based on target weight vs current weight
        String goal = determineGoal(currentWeight, user.getTargetWeight());
        
        // Step B: Calculate BMR using Mifflin-St Jeor formula
        double bmr = calculateBMR(user.getGender(), currentWeight, user.getHeight(), user.getAge());
        
        // Step C: Calculate TDEE based on activity level
        double tdee = calculateTDEE(bmr, user.getActivityLevel());
        
        // Step D: Determine daily calorie target based on goal
        double dailyCalorieTarget = calculateDailyCalories(tdee, goal);
        
        // Step E: Calculate macronutrient distribution
        double[] macros = calculateMacronutrients(dailyCalorieTarget, goal);
        
        return new NutritionCalculation(
                bmr,
                tdee,
                dailyCalorieTarget,
                macros[0], // protein
                macros[1], // carbs
                macros[2], // fat
                goal
        );
    }
    
    /**
     * Determine the user's goal based on current and target weights
     * @param currentWeight Current weight in kg
     * @param targetWeight Target weight in kg
     * @return Goal as string: "WEIGHT_LOSS", "MAINTENANCE", or "WEIGHT_GAIN"
     */
    private static String determineGoal(double currentWeight, double targetWeight) {
        double difference = Math.abs(currentWeight - targetWeight);
        
        // If the difference is less than 1kg, consider it maintenance
        if (difference < 1.0) {
            return "MAINTENANCE";
        } else if (currentWeight > targetWeight) {
            return "WEIGHT_LOSS";
        } else {
            return "WEIGHT_GAIN";
        }
    }
    
    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor formula
     * @param gender "PRIA" (male) or "WANITA" (female)
     * @param weight Weight in kg
     * @param height Height in cm
     * @param age Age in years
     * @return BMR in calories
     */
    private static double calculateBMR(String gender, double weight, double height, int age) {
        // Mifflin-St Jeor Equation:
        // Men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age in years) + 5
        // Women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age in years) - 161
        
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        
        if (gender.equalsIgnoreCase("PRIA")) {
            bmr += 5;
        } else {
            bmr -= 161;
        }
        
        return Math.round(bmr);
    }
    
    /**
     * Calculate Total Daily Energy Expenditure (TDEE) based on BMR and activity level
     * @param bmr Basal Metabolic Rate
     * @param activityLevel Activity level as string
     * @return TDEE in calories
     */
    private static double calculateTDEE(double bmr, String activityLevel) {
        double activityMultiplier;
        
        switch (activityLevel.toUpperCase()) {
            case "SEDENTARY":
            case "SANGAT RENDAH":
                activityMultiplier = SEDENTARY_MULTIPLIER;
                break;
            case "LIGHT":
            case "AKTIVITAS RENDAH":
                activityMultiplier = LIGHT_MULTIPLIER;
                break;
            case "MODERATE":
            case "AKTIVITAS SEDANG":
                activityMultiplier = MODERATE_MULTIPLIER;
                break;
            case "ACTIVE":
            case "AKTIVITAS TINGGI":
                activityMultiplier = ACTIVE_MULTIPLIER;
                break;
            case "VERY ACTIVE":
            case "AKTIVITAS SANGAT TINGGI":
                activityMultiplier = VERY_ACTIVE_MULTIPLIER;
                break;
            default:
                activityMultiplier = MODERATE_MULTIPLIER; // Default to moderate if unknown
        }
        
        return Math.round(bmr * activityMultiplier);
    }
    
    /**
     * Calculate daily calorie target based on TDEE and goal
     * @param tdee Total Daily Energy Expenditure
     * @param goal User's goal (WEIGHT_LOSS, MAINTENANCE, or WEIGHT_GAIN)
     * @return Daily calorie target
     */
    private static double calculateDailyCalories(double tdee, String goal) {
        switch (goal) {
            case "WEIGHT_LOSS":
                return Math.round(tdee * WEIGHT_LOSS_MULTIPLIER);
            case "WEIGHT_GAIN":
                return Math.round(tdee * WEIGHT_GAIN_MULTIPLIER);
            case "MAINTENANCE":
            default:
                return tdee;
        }
    }
    
    /**
     * Calculate macronutrient distribution in grams based on daily calorie target and goal
     * @param dailyCalories Daily calorie target
     * @param goal User's goal (WEIGHT_LOSS, MAINTENANCE, or WEIGHT_GAIN)
     * @return Array containing protein, carbs, and fat in grams
     */
    private static double[] calculateMacronutrients(double dailyCalories, String goal) {
        double[] macroRatios;
        
        // Select macro ratio based on goal
        switch (goal) {
            case "WEIGHT_LOSS":
                macroRatios = WEIGHT_LOSS_RATIO;
                break;
            case "WEIGHT_GAIN":
                macroRatios = WEIGHT_GAIN_RATIO;
                break;
            case "MAINTENANCE":
            default:
                macroRatios = MAINTENANCE_RATIO;
                break;
        }
        
        // Calculate grams of each macronutrient
        double proteinCalories = dailyCalories * macroRatios[0];
        double carbCalories = dailyCalories * macroRatios[1];
        double fatCalories = dailyCalories * macroRatios[2];
        
        double proteinGrams = Math.round(proteinCalories / PROTEIN_CALORIES_PER_GRAM);
        double carbGrams = Math.round(carbCalories / CARB_CALORIES_PER_GRAM);
        double fatGrams = Math.round(fatCalories / FAT_CALORIES_PER_GRAM);
        
        return new double[]{proteinGrams, carbGrams, fatGrams};
    }
}