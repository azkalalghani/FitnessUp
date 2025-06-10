package com.example.fitnessup.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessup.R;
import com.example.fitnessup.data.model.WeightProgress;
import com.example.fitnessup.ui.adapter.WeightHistoryAdapter;
import com.example.fitnessup.ui.viewmodel.ProgressViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for displaying and managing weight progress history.
 * Shows a chart of weight changes over time and a list of all weight entries.
 */
public class ProgressActivity extends AppCompatActivity {
    private static final String TAG = "ProgressActivity";
    
    private ProgressViewModel viewModel;
    
    // UI components
    private Spinner timeRangeSpinner;
    private TextView trendTextView;
    private RecyclerView weightHistoryRecyclerView;
    private WeightHistoryAdapter adapter;
    private FloatingActionButton addWeightFab;
    private View chartContainer;
    private View emptyStateView;
    private View loadingView;
    private BottomNavigationView bottomNavigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Riwayat Berat Badan");
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        
        // Initialize UI components
        initializeViews();
        setupTimeRangeSpinner();
        setupRecyclerView();
        setupListeners();
        setupBottomNavigation();
        
        // Observe data
        observeViewModel();
        
        // Load user data
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.loadWeightHistory(userId);
    }
    
    private void initializeViews() {
        timeRangeSpinner = findViewById(R.id.timeRangeSpinner);
        trendTextView = findViewById(R.id.trendTextView);
        weightHistoryRecyclerView = findViewById(R.id.weightHistoryRecyclerView);
        addWeightFab = findViewById(R.id.addWeightFab);
        chartContainer = findViewById(R.id.chartContainer);
        emptyStateView = findViewById(R.id.emptyStateView);
        loadingView = findViewById(R.id.loadingView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_progress);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent homeIntent = new Intent(ProgressActivity.this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.navigation_progress) {
                // Already on progress, do nothing
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent profileIntent = new Intent(ProgressActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }
            return false;
        });
    }
    
    private void setupTimeRangeSpinner() {
        // Create an ArrayAdapter using a string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_ranges, android.R.layout.simple_spinner_item);
        
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Apply the adapter to the spinner
        timeRangeSpinner.setAdapter(adapter);
    }
    
    private void setupRecyclerView() {
        adapter = new WeightHistoryAdapter(new ArrayList<>());
        weightHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weightHistoryRecyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        // Add weight FAB
        addWeightFab.setOnClickListener(v -> {
            showAddWeightDialog();
        });
        
        // Time range spinner
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ProgressViewModel.TimeRange selectedTimeRange;
                
                switch (position) {
                    case 0:
                        selectedTimeRange = ProgressViewModel.TimeRange.WEEK;
                        break;
                    case 1:
                        selectedTimeRange = ProgressViewModel.TimeRange.MONTH;
                        break;
                    case 2:
                        selectedTimeRange = ProgressViewModel.TimeRange.THREE_MONTHS;
                        break;
                    case 3:
                        selectedTimeRange = ProgressViewModel.TimeRange.SIX_MONTHS;
                        break;
                    case 4:
                        selectedTimeRange = ProgressViewModel.TimeRange.YEAR;
                        break;
                    case 5:
                        selectedTimeRange = ProgressViewModel.TimeRange.ALL;
                        break;
                    default:
                        selectedTimeRange = ProgressViewModel.TimeRange.MONTH;
                }
                
                viewModel.setTimeRange(selectedTimeRange);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void observeViewModel() {
        // Observe weight history
        viewModel.getWeightHistory().observe(this, weightProgressList -> {
            if (weightProgressList != null) {
                updateWeightHistory(weightProgressList);
            }
        });
        
        // Observe chart data
        viewModel.getChartData().observe(this, chartEntries -> {
            if (chartEntries != null) {
                updateChart(chartEntries);
            }
        });
        
        // Observe weight trend
        viewModel.getWeightTrend().observe(this, trend -> {
            if (trend != null) {
                trendTextView.setText(trend);
            }
        });
        
        // Observe selected time range
        viewModel.getSelectedTimeRange().observe(this, timeRange -> {
            int position;
            
            switch (timeRange) {
                case WEEK:
                    position = 0;
                    break;
                case MONTH:
                    position = 1;
                    break;
                case THREE_MONTHS:
                    position = 2;
                    break;
                case SIX_MONTHS:
                    position = 3;
                    break;
                case YEAR:
                    position = 4;
                    break;
                case ALL:
                    position = 5;
                    break;
                default:
                    position = 1; // Default to MONTH
            }
            
            timeRangeSpinner.setSelection(position);
        });
        
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(ProgressActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateWeightHistory(List<WeightProgress> weightProgressList) {
        if (weightProgressList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            weightHistoryRecyclerView.setVisibility(View.GONE);
            chartContainer.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            weightHistoryRecyclerView.setVisibility(View.VISIBLE);
            chartContainer.setVisibility(View.VISIBLE);
            
            adapter.updateData(weightProgressList);
        }
    }
    
    private void updateChart(List<ProgressViewModel.ChartEntry> chartEntries) {
        // In a real implementation, you would update a chart library (like MPAndroidChart)
        // For this example, we'll just handle visibility
        if (chartEntries.isEmpty()) {
            chartContainer.setVisibility(View.GONE);
        } else {
            chartContainer.setVisibility(View.VISIBLE);
            
            // Here you would update the chart with the data
            // Example with MPAndroidChart:
            /*
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < chartEntries.size(); i++) {
                ProgressViewModel.ChartEntry entry = chartEntries.get(i);
                entries.add(new Entry(i, (float) entry.getWeight()));
            }
            
            LineDataSet dataSet = new LineDataSet(entries, "Berat Badan");
            dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
            dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
            */
        }
    }
    
    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Berat Badan Baru");
        
        // Inflate the custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_weight, null);
        builder.setView(dialogView);
        
        // Get references to views in the dialog
        EditText weightEditText = dialogView.findViewById(R.id.weightEditText);
        TextView dateTextView = dialogView.findViewById(R.id.dateTextView);
        
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
        dateTextView.setText(dateFormat.format(new java.util.Date()));
        
        // Set positive and negative buttons
        builder.setPositiveButton("Simpan", null); // We'll set the listener later
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Override the positive button to prevent dialog from closing if input is invalid
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String weightStr = weightEditText.getText().toString().trim();
            
            if (weightStr.isEmpty()) {
                weightEditText.setError("Berat badan tidak boleh kosong");
                return;
            }
            
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight < 30 || weight > 300) {
                    weightEditText.setError("Berat badan harus antara 30-300 kg");
                    return;
                }
                
                // Add weight to database
                viewModel.addWeightProgress(weight);
                dialog.dismiss();
                
            } catch (NumberFormatException e) {
                weightEditText.setError("Berat badan harus berupa angka");
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}