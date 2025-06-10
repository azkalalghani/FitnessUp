package com.example.fitnessup.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.example.fitnessup.ui.viewmodel.DashboardViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for adding a new weight entry.
 */
public class AddWeightActivity extends AppCompatActivity {
    private static final String TAG = "AddWeightActivity";
    
    private DashboardViewModel dashboardViewModel;
    
    // UI components
    private EditText weightEditText;
    private TextView dateTextView;
    private Button saveButton;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tambah Berat Badan");
        }
        
        // Initialize ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        // Initialize UI components
        initializeViews();
        
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
        dateTextView.setText(dateFormat.format(new Date()));
        
        // Set up listeners
        setupListeners();
        
        // Observe ViewModel
        observeViewModel();
    }
    
    private void initializeViews() {
        weightEditText = findViewById(R.id.weightEditText);
        dateTextView = findViewById(R.id.dateTextView);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveWeight();
            }
        });
    }
    
    private void observeViewModel() {
        // Observe loading state
        dashboardViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            saveButton.setEnabled(!isLoading);
        });
        
        // Observe errors
        dashboardViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(AddWeightActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private boolean validateInput() {
        String weightStr = weightEditText.getText().toString().trim();
        
        if (weightStr.isEmpty()) {
            weightEditText.setError("Berat badan tidak boleh kosong");
            return false;
        }
        
        try {
            double weight = Double.parseDouble(weightStr);
            if (weight < 30 || weight > 300) {
                weightEditText.setError("Berat badan harus antara 30-300 kg");
                return false;
            }
        } catch (NumberFormatException e) {
            weightEditText.setError("Berat badan harus berupa angka");
            return false;
        }
        
        return true;
    }
    
    private void saveWeight() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        double weight = Double.parseDouble(weightEditText.getText().toString().trim());
        
        // Add weight progress
        dashboardViewModel.addWeightProgress(weight);
        
        // Finish activity after successful save
        Toast.makeText(this, "Berat badan berhasil disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}