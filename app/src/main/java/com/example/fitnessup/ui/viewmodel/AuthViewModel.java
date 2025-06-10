package com.example.fitnessup.ui.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessup.auth.AuthManager;
import com.example.fitnessup.data.model.User;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel class for handling authentication-related data and operations.
 * Manages data related to user authentication and registration screens.
 */
public class AuthViewModel extends AndroidViewModel {
    private final AuthManager authManager;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authManager = AuthManager.getInstance();
    }

    /**
     * Configure Google Sign-In with web client ID
     * @param webClientId Web client ID from Firebase console
     */
    public void configureGoogleSignIn(String webClientId) {
        authManager.configureGoogleSignIn(getApplication(), webClientId);
    }

    /**
     * Start Google Sign-In flow
     * @param activity Activity to launch Sign-In intent from
     */
    public void signInWithGoogle(Activity activity) {
        isLoading.setValue(true);
        authManager.signInWithGoogle(activity);
    }

    /**
     * Handle Google Sign-In result from activity result
     * @param data Intent data from onActivityResult
     */
    public void handleGoogleSignInResult(Intent data) {
        authManager.handleGoogleSignInResult(data);
    }

    /**
     * Register a new user with email and password
     * @param email User's email
     * @param password User's password
     */
    public void registerUser(String email, String password) {
        isLoading.setValue(true);
        authManager.registerUser(email, password);
    }

    /**
     * Sign in with email and password
     * @param email User's email
     * @param password User's password
     */
    public void signInWithEmailAndPassword(String email, String password) {
        isLoading.setValue(true);
        authManager.signInWithEmailAndPassword(email, password);
    }

    /**
     * Create user profile in Firestore after successful registration
     * @param name User's name
     * @param email User's email
     * @param age User's age
     * @param gender User's gender ("PRIA" or "WANITA")
     * @param height User's height in cm
     * @param activityLevel User's activity level
     * @param targetWeight User's target weight in kg
     * @param initialWeight User's initial weight in kg
     */
    public void createUserProfile(String name, String email, int age, String gender,
                                 double height, String activityLevel, 
                                 double targetWeight, double initialWeight) {
        FirebaseUser firebaseUser = authManager.getCurrentUser().getValue();
        if (firebaseUser != null) {
            User user = new User(
                    firebaseUser.getUid(),
                    name,
                    email,
                    age,
                    gender,
                    height,
                    activityLevel,
                    targetWeight,
                    initialWeight
            );
            authManager.createUserProfile(user, initialWeight);
        }
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        authManager.signOut();
    }

    /**
     * Send password reset email
     * @param email Email address to send reset link to
     */
    public void resetPassword(String email) {
        isLoading.setValue(true);
        authManager.resetPassword(email);
    }

    /**
     * Get current authenticated user
     * @return LiveData object containing the current FirebaseUser
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        return authManager.getCurrentUser();
    }

    /**
     * Get authentication errors
     * @return LiveData object containing error messages
     */
    public LiveData<String> getErrors() {
        LiveData<String> errors = authManager.getErrors();
        errors.observeForever(error -> {
            if (error != null) {
                isLoading.setValue(false);
            }
        });
        return errors;
    }

    /**
     * Get loading state
     * @return LiveData object indicating if an auth operation is in progress
     */
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
}