package com.example.fitnessup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.example.fitnessup.ui.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * Activity for handling user login.
 * Provides email/password login and Google Sign-In options.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private AuthViewModel authViewModel;
    
    // UI components
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private MaterialButton googleSignInButton;
    private TextView registerTextView;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    
    // Activity result launcher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = 
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    authViewModel.handleGoogleSignInResult(result.getData());
                }
            });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Configure Google Sign-In
        authViewModel.configureGoogleSignIn(getString(R.string.firebase_web_client_id));
        
        // Initialize UI components
        initializeViews();
        setupListeners();
        observeViewModel();
    }
    
    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        registerTextView = findViewById(R.id.registerTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        // Email/Password login
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            
            if (validateInput(email, password)) {
                authViewModel.signInWithEmailAndPassword(email, password);
            }
        });
        
        // Google Sign-In
        googleSignInButton.setOnClickListener(v -> {
            authViewModel.signInWithGoogle(this);
        });
        
        // Register new account
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Forgot password
        forgotPasswordTextView.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }
    
    private void observeViewModel() {
        // Observe authentication state
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // User is signed in, navigate to main activity or profile setup
                navigateToMainActivity();
            }
        });
        
        // Observe loading state
        authViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!isLoading);
            googleSignInButton.setEnabled(!isLoading);
        });
        
        // Observe errors
        authViewModel.getErrors().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        
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
        
        return isValid;
    }
    
    private void showForgotPasswordDialog() {
        // In a real implementation, show a dialog to get the user's email
        // For simplicity, we'll just use the email that's already entered
        String email = emailEditText.getText().toString().trim();
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Masukkan email yang valid terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        authViewModel.resetPassword(email);
        Toast.makeText(this, "Email reset password telah dikirim ke " + email, Toast.LENGTH_LONG).show();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}