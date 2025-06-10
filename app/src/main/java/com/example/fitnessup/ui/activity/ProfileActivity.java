package com.example.fitnessup.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.fitnessup.data.model.User;
import com.example.fitnessup.ui.viewmodel.AuthViewModel;
import com.example.fitnessup.ui.viewmodel.DashboardViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for displaying and editing user profile information.
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    
    private DashboardViewModel dashboardViewModel;
    private AuthViewModel authViewModel;
    
    // UI components
    private TextView emailTextView;
    private EditText nameEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private Spinner activityLevelSpinner;
    private EditText targetWeightEditText;
    private Button saveButton;
    private Button logoutButton;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profil Pengguna");
        }
        
        // Initialize ViewModels
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Initialize UI components
        initializeViews();
        setupActivityLevelSpinner();
        setupListeners();
        setupBottomNavigation();
        
        // Load user data
        observeViewModel();
        
        // Load current user data
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dashboardViewModel.loadUserData(currentUser.getUid());
        } else {
            // User not logged in, finish activity
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeViews() {
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        heightEditText = findViewById(R.id.heightEditText);
        activityLevelSpinner = findViewById(R.id.activityLevelSpinner);
        targetWeightEditText = findViewById(R.id.targetWeightEditText);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }
    
    private void setupActivityLevelSpinner() {
        // In a full implementation, you would set up the spinner with activity levels
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent homeIntent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.navigation_progress) {
                Intent progressIntent = new Intent(ProfileActivity.this, ProgressActivity.class);
                startActivity(progressIntent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Already on profile, do nothing
                return true;
            }
            return false;
        });
    }
    
    private void setupListeners() {
        // Save button
        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                updateUserProfile();
            }
        });
        
        // Logout button
        logoutButton.setOnClickListener(v -> {
            authViewModel.signOut();
            finish();
        });
    }
    
    private void observeViewModel() {
        // Observe user profile
        dashboardViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                updateUIWithUserData(user);
            }
        });
        
        // Observe loading state
        dashboardViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            saveButton.setEnabled(!isLoading);
        });
        
        // Observe errors
        dashboardViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateUIWithUserData(User user) {
        // Update UI with user data
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            emailTextView.setText(firebaseUser.getEmail());
        }
        
        nameEditText.setText(user.getName());
        ageEditText.setText(String.valueOf(user.getAge()));
        heightEditText.setText(String.valueOf(user.getHeight()));
        targetWeightEditText.setText(String.valueOf(user.getTargetWeight()));
        
        // Set activity level spinner selection
        // In a full implementation, you would set the spinner selection based on user.getActivityLevel()
    }
    
    private boolean validateInput() {
        boolean isValid = true;
        
        // Validate name
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Nama tidak boleh kosong");
            isValid = false;
        }
        
        // Validate age
        String ageStr = ageEditText.getText().toString().trim();
        if (ageStr.isEmpty()) {
            ageEditText.setError("Usia tidak boleh kosong");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 15 || age > 100) {
                    ageEditText.setError("Usia harus antara 15-100 tahun");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                ageEditText.setError("Usia harus berupa angka");
                isValid = false;
            }
        }
        
        // Validate height
        String heightStr = heightEditText.getText().toString().trim();
        if (heightStr.isEmpty()) {
            heightEditText.setError("Tinggi badan tidak boleh kosong");
            isValid = false;
        } else {
            try {
                double height = Double.parseDouble(heightStr);
                if (height < 100 || height > 250) {
                    heightEditText.setError("Tinggi badan harus antara 100-250 cm");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                heightEditText.setError("Tinggi badan harus berupa angka");
                isValid = false;
            }
        }
        
        // Validate target weight
        String targetWeightStr = targetWeightEditText.getText().toString().trim();
        if (targetWeightStr.isEmpty()) {
            targetWeightEditText.setError("Berat badan target tidak boleh kosong");
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(targetWeightStr);
                if (weight < 30 || weight > 300) {
                    targetWeightEditText.setError("Berat badan target harus antara 30-300 kg");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                targetWeightEditText.setError("Berat badan target harus berupa angka");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void updateUserProfile() {
        User currentUser = dashboardViewModel.getUserProfile().getValue();
        if (currentUser == null) {
            Toast.makeText(this, "Tidak dapat memperbarui profil: Data pengguna tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update user object with new values
        currentUser.setName(nameEditText.getText().toString().trim());
        currentUser.setAge(Integer.parseInt(ageEditText.getText().toString().trim()));
        currentUser.setHeight(Double.parseDouble(heightEditText.getText().toString().trim()));
        currentUser.setTargetWeight(Double.parseDouble(targetWeightEditText.getText().toString().trim()));
        currentUser.setActivityLevel(activityLevelSpinner.getSelectedItem().toString());
        
        // Save updated user profile
        dashboardViewModel.updateUserProfile(currentUser);
        Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
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