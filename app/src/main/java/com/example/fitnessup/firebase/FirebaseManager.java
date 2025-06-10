package com.example.fitnessup.firebase;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Centralized manager for Firebase services initialization and configuration.
 * Follows the singleton pattern to ensure only one instance exists throughout the app.
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    
    private static FirebaseManager instance;
    
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    
    // Private constructor to enforce singleton pattern
    private FirebaseManager(Context context) {
        // Initialize Firebase
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
            Log.d(TAG, "Firebase initialized successfully");
        }
        
        // Get Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
        // Configure Firestore settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Set cache size
                .build();
        firestore.setFirestoreSettings(settings);
    }
    
    /**
     * Initialize the FirebaseManager singleton with application context.
     * Should be called from Application class onCreate().
     *
     * @param application Application instance
     * @return FirebaseManager singleton instance
     */
    public static FirebaseManager initialize(@NonNull Application application) {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager(application.getApplicationContext());
                }
            }
        }
        return instance;
    }
    
    /**
     * Get the FirebaseManager singleton instance.
     * Must be initialized first with initialize().
     *
     * @return FirebaseManager singleton instance
     * @throws IllegalStateException if not initialized
     */
    public static FirebaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FirebaseManager not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Get the FirebaseAuth instance.
     *
     * @return FirebaseAuth instance
     */
    public FirebaseAuth getAuth() {
        return firebaseAuth;
    }
    
    /**
     * Get the FirebaseFirestore instance.
     *
     * @return FirebaseFirestore instance
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
    
    /**
     * Get the current Firebase user.
     *
     * @return Current FirebaseUser or null if not signed in
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Check if a user is currently signed in.
     *
     * @return True if a user is signed in, false otherwise
     */
    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Sign out the current user.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }
}