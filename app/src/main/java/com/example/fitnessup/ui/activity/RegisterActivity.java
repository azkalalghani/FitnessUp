package com.example.fitnessup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.example.fitnessup.ui.viewmodel.AuthViewModel;

/**
 * Activity for user registration.
 * Handles creating new accounts with email and password.
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private AuthViewModel authViewModel;
    
    // UI components
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Initialize UI components
        initializeViews();
        setupListeners();
        observeViewModel();
    }
    
    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        // Register button
        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            
            if (validateInput(name, email, password, confirmPassword)) {
                authViewModel.registerUser(email, password);
            }
        });
        
        // Login text
        loginTextView.setOnClickListener(v -> {
            finish(); // Go back to login screen
        });
    }
    
    private void observeViewModel() {
        // Observe authentication state
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // User is registered, navigate to profile setup
                navigateToProfileSetup();
            }
        });
        
        // Observe loading state
        authViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            registerButton.setEnabled(!isLoading);
        });
        
        // Observe errors
        authViewModel.getErrors().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;
        
        if (name.isEmpty()) {
            nameEditText.setError("Nama tidak boleh kosong");
            isValid = false;
        } else {
            nameEditText.setError(null);
        }
        
        if (email.isEmpty()) {
            emailEditText.setError("Email tidak boleh kosong");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Format email tidak valid");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password tidak boleh kosong");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password minimal 6 karakter");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Konfirmasi password tidak boleh kosong");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError("Password tidak cocok");
            isValid = false;
        } else {
            confirmPasswordEditText.setError(null);
        }
        
        return isValid;
    }
    
    private void navigateToProfileSetup() {
        Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}