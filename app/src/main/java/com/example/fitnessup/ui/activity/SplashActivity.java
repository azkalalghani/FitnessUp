package com.example.fitnessup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.example.fitnessup.ui.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash screen activity shown when the app is launched.
 * Checks authentication state and directs to appropriate screen.
 */
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 1500; // 1.5 seconds
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Delay navigation to give splash screen time to display
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndNavigate, SPLASH_DELAY);
    }

    /**
     * Check if user is authenticated and navigate to appropriate screen
     */
    private void checkAuthAndNavigate() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // User is signed in, check if profile is complete
                checkUserProfileAndNavigate(user);
            } else {
                // User is not signed in, go to login screen
                navigateToLogin();
            }
        });
    }

    /**
     * Check if user has completed their profile setup
     */
    private void checkUserProfileAndNavigate(FirebaseUser user) {
        // In a real implementation, you would check Firestore to see if the user has a profile
        // For simplicity in this example, we'll just navigate to the main screen
        navigateToMain();

        // In a complete implementation, you would do something like:
        /*
        UserRepository.getInstance().getUserProfile(user.getUid()).observe(this, userProfile -> {
            if (userProfile == null) {
                // Profile doesn't exist, user needs to set up profile
                navigateToProfileSetup();
            } else {
                // Profile exists, go to main screen
                navigateToMain();
            }
        });
        */
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProfileSetup() {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}