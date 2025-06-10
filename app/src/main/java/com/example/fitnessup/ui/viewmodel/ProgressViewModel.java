package com.example.fitnessup.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessup.data.model.WeightProgress;
import com.example.fitnessup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel for tracking and visualizing weight progress over time.
 * Manages data for the progress chart and weight history list.
 */
public class ProgressViewModel extends AndroidViewModel {
    private static final String TAG = "ProgressViewModel";
    
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<ChartEntry>> chartData = new MutableLiveData<>();
    private final MutableLiveData<String> weightTrend = new MutableLiveData<>();
    
    // Time range options for chart
    public enum TimeRange {
        WEEK, MONTH, THREE_MONTHS, SIX_MONTHS, YEAR, ALL
    }
    
    private final MutableLiveData<TimeRange> selectedTimeRange = new MutableLiveData<>(TimeRange.MONTH);
    
    // Cache for weight history
    private LiveData<List<WeightProgress>> allWeightHistory;
    
    public ProgressViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
        
        // Initialize data if user is logged in
        FirebaseUser currentUser = userRepository.getCurrentUser();
        if (currentUser != null) {
            loadWeightHistory(currentUser.getUid());
        }
    }
    
    /**
     * Load weight history for the given user
     * @param userId User ID to load history for
     */
    public void loadWeightHistory(String userId) {
        isLoading.setValue(true);
        
        // Load full weight history
        allWeightHistory = userRepository.getWeightProgressHistory(userId);
        
        // Observe changes to weight history and update chart data
        allWeightHistory.observeForever(weightProgressList -> {
            if (weightProgressList != null) {
                // Update chart data based on selected time range
                updateChartData(weightProgressList, selectedTimeRange.getValue());
                
                // Calculate weight trend
                calculateWeightTrend(weightProgressList);
                
                isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Change the time range for the chart
     * @param timeRange New time range to display
     */
    public void setTimeRange(TimeRange timeRange) {
        selectedTimeRange.setValue(timeRange);
        
        // Update chart data with new time range
        List<WeightProgress> weightProgressList = allWeightHistory.getValue();
        if (weightProgressList != null) {
            updateChartData(weightProgressList, timeRange);
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
                    // The observer on allWeightHistory will automatically update chart data
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to add weight progress: " + e.getMessage());
                });
    }
    
    /**
     * Update chart data based on weight history and selected time range
     */
    private void updateChartData(List<WeightProgress> weightProgressList, TimeRange timeRange) {
        if (weightProgressList == null || weightProgressList.isEmpty()) {
            chartData.setValue(new ArrayList<>());
            return;
        }
        
        // Filter data based on time range
        List<WeightProgress> filteredList = filterByTimeRange(weightProgressList, timeRange);
        
        // Convert to chart entries
        List<ChartEntry> entries = new ArrayList<>();
        for (WeightProgress progress : filteredList) {
            entries.add(new ChartEntry(progress.getDate(), progress.getWeight()));
        }
        
        chartData.setValue(entries);
    }
    
    /**
     * Filter weight progress list by time range
     */
    private List<WeightProgress> filterByTimeRange(List<WeightProgress> weightProgressList, TimeRange timeRange) {
        if (timeRange == TimeRange.ALL) {
            return new ArrayList<>(weightProgressList);
        }
        
        Date cutoffDate = getCutoffDate(timeRange);
        List<WeightProgress> filteredList = new ArrayList<>();
        
        for (WeightProgress progress : weightProgressList) {
            if (progress.getDate().after(cutoffDate)) {
                filteredList.add(progress);
            }
        }
        
        return filteredList;
    }
    
    /**
     * Get cutoff date based on selected time range
     */
    private Date getCutoffDate(TimeRange timeRange) {
        Calendar calendar = Calendar.getInstance();
        
        switch (timeRange) {
            case WEEK:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case MONTH:
                calendar.add(Calendar.MONTH, -1);
                break;
            case THREE_MONTHS:
                calendar.add(Calendar.MONTH, -3);
                break;
            case SIX_MONTHS:
                calendar.add(Calendar.MONTH, -6);
                break;
            case YEAR:
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
                // ALL time range, no cutoff
                calendar.add(Calendar.YEAR, -100);
        }
        
        return calendar.getTime();
    }
    
    /**
     * Calculate weight trend over time
     */
    private void calculateWeightTrend(List<WeightProgress> weightProgressList) {
        if (weightProgressList == null || weightProgressList.size() < 2) {
            weightTrend.setValue("Belum cukup data untuk menunjukkan tren.");
            return;
        }
        
        // Sort by timestamp (oldest first)
        List<WeightProgress> sortedList = new ArrayList<>(weightProgressList);
        sortedList.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        
        // Get first and last entry
        WeightProgress first = sortedList.get(0);
        WeightProgress last = sortedList.get(sortedList.size() - 1);
        
        // Calculate change
        double weightChange = last.getWeight() - first.getWeight();
        
        // Calculate time difference in days
        long diffInMillis = last.getDate().getTime() - first.getDate().getTime();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        
        // Prevent division by zero
        if (diffInDays == 0) {
            weightTrend.setValue("Belum cukup data untuk menunjukkan tren.");
            return;
        }
        
        // Calculate rate of change per week
        double changePerWeek = (weightChange / diffInDays) * 7;
        
        // Format message based on change
        String trend;
        if (Math.abs(changePerWeek) < 0.1) {
            trend = "Berat badan Anda stabil.";
        } else if (changePerWeek > 0) {
            trend = String.format("Tren: +%.1f kg per minggu", changePerWeek);
        } else {
            trend = String.format("Tren: %.1f kg per minggu", changePerWeek);
        }
        
        weightTrend.setValue(trend);
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
     * Get chart data
     */
    public LiveData<List<ChartEntry>> getChartData() {
        return chartData;
    }
    
    /**
     * Get weight history
     */
    public LiveData<List<WeightProgress>> getWeightHistory() {
        return allWeightHistory;
    }
    
    /**
     * Get selected time range
     */
    public LiveData<TimeRange> getSelectedTimeRange() {
        return selectedTimeRange;
    }
    
    /**
     * Get weight trend information
     */
    public LiveData<String> getWeightTrend() {
        return weightTrend;
    }
    
    /**
     * Class representing a data point for the weight chart
     */
    public static class ChartEntry {
        private final Date date;
        private final double weight;
        
        public ChartEntry(Date date, double weight) {
            this.date = date;
            this.weight = weight;
        }
        
        public Date getDate() {
            return date;
        }
        
        public double getWeight() {
            return weight;
        }
    }
}