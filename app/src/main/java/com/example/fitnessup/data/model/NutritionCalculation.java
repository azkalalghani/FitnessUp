package com.example.fitnessup.data.model;

/**
 * Model class for storing the results of nutrition calculations.
 * This class stores calculated values like BMR, TDEE, daily calorie target, and macronutrient distribution.
 */
public class NutritionCalculation {
    private double bmr; // Basal Metabolic Rate in calories
    private double tdee; // Total Daily Energy Expenditure in calories
    private double dailyCalorieTarget; // Target calories per day based on goal
    private double proteinGrams; // Daily protein target in grams
    private double carbGrams; // Daily carbohydrate target in grams
    private double fatGrams; // Daily fat target in grams
    private String goal; // "MAINTENANCE", "WEIGHT_LOSS", or "WEIGHT_GAIN"

    // Empty constructor
    public NutritionCalculation() {
    }

    public NutritionCalculation(double bmr, double tdee, double dailyCalorieTarget, 
                               double proteinGrams, double carbGrams, double fatGrams, 
                               String goal) {
        this.bmr = bmr;
        this.tdee = tdee;
        this.dailyCalorieTarget = dailyCalorieTarget;
        this.proteinGrams = proteinGrams;
        this.carbGrams = carbGrams;
        this.fatGrams = fatGrams;
        this.goal = goal;
    }

    // Getters and Setters
    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }

    public double getTdee() {
        return tdee;
    }

    public void setTdee(double tdee) {
        this.tdee = tdee;
    }

    public double getDailyCalorieTarget() {
        return dailyCalorieTarget;
    }

    public void setDailyCalorieTarget(double dailyCalorieTarget) {
        this.dailyCalorieTarget = dailyCalorieTarget;
    }

    public double getProteinGrams() {
        return proteinGrams;
    }

    public void setProteinGrams(double proteinGrams) {
        this.proteinGrams = proteinGrams;
    }

    public double getCarbGrams() {
        return carbGrams;
    }

    public void setCarbGrams(double carbGrams) {
        this.carbGrams = carbGrams;
    }

    public double getFatGrams() {
        return fatGrams;
    }

    public void setFatGrams(double fatGrams) {
        this.fatGrams = fatGrams;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    /**
     * Calculates the percentage distribution of macronutrients in total daily calories
     * @return An array with protein, carb, and fat percentages respectively
     */
    public double[] getMacroPercentages() {
        double totalCalories = (proteinGrams * 4) + (carbGrams * 4) + (fatGrams * 9);
        double proteinPercentage = (proteinGrams * 4 / totalCalories) * 100;
        double carbPercentage = (carbGrams * 4 / totalCalories) * 100;
        double fatPercentage = (fatGrams * 9 / totalCalories) * 100;
        
        return new double[]{proteinPercentage, carbPercentage, fatPercentage};
    }

    /**
     * Calculates total calories from macronutrients
     * @return Total calories from protein, carbs, and fat
     */
    public double getTotalCaloriesFromMacros() {
        return (proteinGrams * 4) + (carbGrams * 4) + (fatGrams * 9);
    }
}