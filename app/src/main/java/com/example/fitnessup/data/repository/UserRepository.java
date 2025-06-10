package com.example.fitnessup.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessup.data.model.User;
import com.example.fitnessup.data.model.WeightProgress;
import com.example.fitnessup.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class that acts as a mediator between the app and Firebase for user-related operations.
 * Implements the Repository pattern to abstract the data sources from the rest of the application.
 * Uses FirebaseManager to access Firebase services.
 */
public class UserRepository {
    private static final String USERS_COLLECTION = "users";
    private static final String PROGRESS_COLLECTION = "progress";
    
    private final FirebaseFirestore firestore;
    private final FirebaseManager firebaseManager;
    
    private static UserRepository instance;
    
    // Singleton pattern to ensure only one instance of UserRepository exists
    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    
    private UserRepository() {
        firebaseManager = FirebaseManager.getInstance();
        firestore = firebaseManager.getFirestore();
    }
    
    /**
     * Gets the currently logged-in user
     * @return FirebaseUser object or null if no user is logged in
     */
    public FirebaseUser getCurrentUser() {
        return firebaseManager.getCurrentUser();
    }
    
    /**
     * Creates a new user profile in Firestore
     * @param user User object containing profile data
     * @return Task that can be used to track operation completion
     */
    public Task<Void> createUserProfile(User user) {
        return firestore.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user.toMap());
    }
    
    /**
     * Updates an existing user profile in Firestore
     * @param user User object with updated data
     * @return Task that can be used to track operation completion
     */
    public Task<Void> updateUserProfile(User user) {
        return firestore.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .update(user.toMap());
    }
    
    /**
     * Retrieves a user profile from Firestore as LiveData
     * @param userId ID of the user to retrieve
     * @return LiveData containing the User object
     */
    public LiveData<User> getUserProfile(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User user = task.getResult().toObject(User.class);
                        userLiveData.setValue(user);
                    } else {
                        userLiveData.setValue(null);
                    }
                });
        
        return userLiveData;
    }
    
    /**
     * Adds a new weight progress entry to Firestore
     * @param weightProgress WeightProgress object to be saved
     * @return Task that can be used to track operation completion
     */
    public Task<DocumentReference> addWeightProgress(WeightProgress weightProgress) {
        return firestore.collection(PROGRESS_COLLECTION)
                .add(weightProgress.toMap());
    }
    
    /**
     * Retrieves weight progress history for a user as LiveData
     * @param userId ID of the user whose progress to retrieve
     * @return LiveData containing a list of WeightProgress objects
     */
    public LiveData<List<WeightProgress>> getWeightProgressHistory(String userId) {
        MutableLiveData<List<WeightProgress>> progressLiveData = new MutableLiveData<>();
        
        firestore.collection(PROGRESS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<WeightProgress> progressList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            WeightProgress progress = document.toObject(WeightProgress.class);
                            if (progress != null) {
                                progress.setProgressId(document.getId());
                                progressList.add(progress);
                            }
                        }
                        progressLiveData.setValue(progressList);
                    } else {
                        progressLiveData.setValue(new ArrayList<>());
                    }
                });
        
        return progressLiveData;
    }
    
    /**
     * Gets the latest weight entry for a user
     * @param userId ID of the user
     * @return LiveData containing the most recent WeightProgress object
     */
    public LiveData<WeightProgress> getLatestWeight(String userId) {
        MutableLiveData<WeightProgress> latestWeightLiveData = new MutableLiveData<>();
        
        firestore.collection(PROGRESS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        WeightProgress latestProgress = task.getResult().getDocuments().get(0).toObject(WeightProgress.class);
                        latestWeightLiveData.setValue(latestProgress);
                    } else {
                        latestWeightLiveData.setValue(null);
                    }
                });
        
        return latestWeightLiveData;
    }
    
    /**
     * Signs out the current user
     */
    public void signOut() {
        firebaseManager.signOut();
    }
}