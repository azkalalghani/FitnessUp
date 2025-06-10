package com.example.fitnessup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.example.fitnessup.ui.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for setting up a new user's profile.
 * Collects basic information needed for nutrition calculations.
 */
public class ProfileSetupActivity extends AppCompatActivity {
    private static final String TAG = "ProfileSetupActivity";
    
    private AuthViewModel authViewModel;
    
    // UI components
    private EditText nameEditText;
    private EditText ageEditText;
    private RadioGroup genderRadioGroup;
    private EditText heightEditText;
    private Spinner activityLevelSpinner;
    private EditText currentWeightEditText;
    private EditText targetWeightEditText;
    private Button saveButton;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Initialize UI components
        initializeViews();
        setupActivityLevelSpinner();
        
        // Set up listeners
        setupListeners();
        
        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            // Pre-fill email and name if available
            nameEditText.setText(currentUser.getDisplayName() != null ? 
                    currentUser.getDisplayName() : "");
        } else {
            // User not authenticated, go back to login
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }
    
    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        heightEditText = findViewById(R.id.heightEditText);
        activityLevelSpinner = findViewById(R.id.activityLevelSpinner);
        currentWeightEditText = findViewById(R.id.currentWeightEditText);
        targetWeightEditText = findViewById(R.id.targetWeightEditText);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupActivityLevelSpinner() {
        // Create an ArrayAdapter using a string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activity_levels, android.R.layout.simple_spinner_item);
        
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Apply the adapter to the spinner
        activityLevelSpinner.setAdapter(adapter);
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveProfile();
            }
        });
    }
    
    private boolean validateInput() {
        boolean isValid = true;
        
        String name = nameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String heightStr = heightEditText.getText().toString().trim();
        String currentWeightStr = currentWeightEditText.getText().toString().trim();
        String targetWeightStr = targetWeightEditText.getText().toString().trim();
        
        // Validate name
        if (name.isEmpty()) {
            nameEditText.setError("Nama tidak boleh kosong");
            isValid = false;
        } else {
            nameEditText.setError(null);
        }
        
        // Validate age
        if (ageStr.isEmpty()) {
            ageEditText.setError("Usia tidak boleh kosong");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 15 || age > 100) {
                    ageEditText.setError("Usia harus antara 15-100 tahun");
                    isValid = false;
                } else {
                    ageEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                ageEditText.setError("Usia harus berupa angka");
                isValid = false;
            }
        }
        
        // Validate gender
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate height
        if (heightStr.isEmpty()) {
            heightEditText.setError("Tinggi badan tidak boleh kosong");
            isValid = false;
        } else {
            try {
                double height = Double.parseDouble(heightStr);
                if (height < 100 || height > 250) {
                    heightEditText.setError("Tinggi badan harus antara 100-250 cm");
                    isValid = false;
                } else {
                    heightEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                heightEditText.setError("Tinggi badan harus berupa angka");
                isValid = false;
            }
        }
        
        // Validate current weight
        if (currentWeightStr.isEmpty()) {
            currentWeightEditText.setError("Berat badan saat ini tidak boleh kosong");
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(currentWeightStr);
                if (weight < 30 || weight > 300) {
                    currentWeightEditText.setError("Berat badan harus antara 30-300 kg");
                    isValid = false;
                } else {
                    currentWeightEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                currentWeightEditText.setError("Berat badan harus berupa angka");
                isValid = false;
            }
        }
        
        // Validate target weight
        if (targetWeightStr.isEmpty()) {
            targetWeightEditText.setError("Berat badan target tidak boleh kosong");
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(targetWeightStr);
                if (weight < 30 || weight > 300) {
                    targetWeightEditText.setError("Berat badan target harus antara 30-300 kg");
                    isValid = false;
                } else {
                    targetWeightEditText.setError(null);
                }
            } catch (NumberFormatException e) {
                targetWeightEditText.setError("Berat badan target harus berupa angka");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void saveProfile() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        
        String name = nameEditText.getText().toString().trim();
        int age = Integer.parseInt(ageEditText.getText().toString().trim());
        
        // Get selected gender
        RadioButton selectedGender = findViewById(genderRadioGroup.getCheckedRadioButtonId());
        String gender = selectedGender.getText().toString().equals("Pria") ? "PRIA" : "WANITA";
        
        double height = Double.parseDouble(heightEditText.getText().toString().trim());
        String activityLevel = activityLevelSpinner.getSelectedItem().toString();
        double currentWeight = Double.parseDouble(currentWeightEditText.getText().toString().trim());
        double targetWeight = Double.parseDouble(targetWeightEditText.getText().toString().trim());
        
        // Get current user email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String email = currentUser != null ? currentUser.getEmail() : "";
        
        // Save profile data
        authViewModel.createUserProfile(
                name,
                email,
                age,
                gender,
                height,
                activityLevel,
                targetWeight,
                currentWeight
        );
        
        // Navigate to main activity
        navigateToMain();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(ProfileSetupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}