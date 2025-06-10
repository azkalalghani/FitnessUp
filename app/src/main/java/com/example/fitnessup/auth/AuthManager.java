package com.example.fitnessup.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessup.data.model.User;
import com.example.fitnessup.data.model.WeightProgress;
import com.example.fitnessup.data.repository.UserRepository;
import com.example.fitnessup.firebase.FirebaseManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Manager class for handling Firebase Authentication operations.
 * Provides methods for user registration, login, and Google Sign-In.
 */
public class AuthManager {
    private static final String TAG = "AuthManager";
    private static final int RC_SIGN_IN = 9001;

    private final FirebaseAuth firebaseAuth;
    private final FirebaseManager firebaseManager;
    private final UserRepository userRepository;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    private GoogleSignInClient googleSignInClient;

    private static AuthManager instance;

    // Singleton pattern
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    private AuthManager() {
        firebaseManager = FirebaseManager.getInstance();
        firebaseAuth = firebaseManager.getAuth();
        userRepository = UserRepository.getInstance();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();

        // Update LiveData if auth state changes
        firebaseAuth.addAuthStateListener(auth -> {
            userLiveData.postValue(auth.getCurrentUser());
        });
    }

    /**
     * Configure Google Sign-In
     * @param context Application context
     * @param webClientId Web client ID from Firebase console
     */
    public void configureGoogleSignIn(Context context, String webClientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    /**
     * Start Google Sign-In flow
     * @param activity Activity to launch Sign-In intent from
     */
    public void signInWithGoogle(Activity activity) {
        if (googleSignInClient == null) {
            errorLiveData.setValue("Google Sign-In not configured. Call configureGoogleSignIn first.");
            return;
        }

        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle Google Sign-In result
     * @param data Intent data from onActivityResult
     */
    public void handleGoogleSignInResult(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            errorLiveData.setValue("Google Sign-In failed: " + e.getMessage());
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     * @param idToken ID token from Google Sign-In
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        userLiveData.setValue(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        errorLiveData.setValue("Authentication failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    /**
     * Register a new user with email and password
     * @param email User's email
     * @param password User's password
     */
    public void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        userLiveData.setValue(user);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        errorLiveData.setValue("Registration failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    /**
     * Sign in with email and password
     * @param email User's email
     * @param password User's password
     */
    public void signInWithEmailAndPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        userLiveData.setValue(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        errorLiveData.setValue("Login failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    /**
     * Create user profile in Firestore after registration
     * @param user User object with profile data
     * @param initialWeight User's initial weight
     */
    public void createUserProfile(User user, double initialWeight) {
        // First create the user profile
        userRepository.createUserProfile(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created successfully");
                    
                    // Then add initial weight progress
                    WeightProgress weightProgress = new WeightProgress(user.getUserId(), initialWeight);
                    userRepository.addWeightProgress(weightProgress)
                            .addOnSuccessListener(documentReference -> 
                                    Log.d(TAG, "Initial weight progress added with ID: " + documentReference.getId()))
                            .addOnFailureListener(e -> 
                                    errorLiveData.setValue("Failed to add initial weight progress: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating user profile", e);
                    errorLiveData.setValue("Failed to create user profile: " + e.getMessage());
                });
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        firebaseManager.signOut();
        
        // Also sign out from Google if Google Sign-In was used
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }

    /**
     * Send password reset email
     * @param email Email address to send reset link to
     */
    public void resetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent.");
                    } else {
                        Log.w(TAG, "resetPassword:failure", task.getException());
                        errorLiveData.setValue("Failed to send reset email: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    /**
     * Get current authenticated user
     * @return LiveData object containing the current FirebaseUser
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        return userLiveData;
    }

    /**
     * Get authentication errors
     * @return LiveData object containing error messages
     */
    public LiveData<String> getErrors() {
        return errorLiveData;
    }
}