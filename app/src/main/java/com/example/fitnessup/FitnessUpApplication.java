package com.example.fitnessup;

import android.app.Application;

import com.example.fitnessup.firebase.FirebaseManager;

/**
 * Application class for the FitnessUp app.
 * Responsible for initializing app-wide components like Firebase.
 */
public class FitnessUpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseManager.initialize(this);
    }
}