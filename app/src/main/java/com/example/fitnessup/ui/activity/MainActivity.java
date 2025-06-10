package com.example.fitnessup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessup.R;
import com.example.fitnessup.data.model.NutritionCalculation;
import com.example.fitnessup.data.model.User;
import com.example.fitnessup.data.model.WeightProgress;
import com.example.fitnessup.ui.viewmodel.AuthViewModel;
import com.example.fitnessup.ui.viewmodel.DashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Main activity that serves as the dashboard of the application.
 * Displays nutrition calculations and recommendations for the user.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private DashboardViewModel dashboardViewModel;
    private AuthViewModel authViewModel;
    
    // UI components
    private TextView userNameTextView;
    private TextView currentWeightTextView;
    private TextView targetWeightTextView;
    private TextView lastWeightUpdateTextView;
    private TextView calorieTargetTextView;
    private TextView bmrTextView;
    private TextView tdeeTextView;
    private TextView goalTextView;
    private TextView proteinTextView;
    private TextView carbsTextView;
    private TextView fatsTextView;
    private TextView recommendationTextView;
    private Button addWeightButton;
    private FloatingActionButton profileFab;
    private CircularProgressIndicator loadingIndicator;
    private CardView nutritionCard;
    private CardView recommendationCard;
    private BottomNavigationView bottomNavigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Initialize ViewModels
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Initialize UI components
        initializeViews();
        setupListeners();
        
        // Observe data
        observeViewModel();
        
        // Load user data
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            dashboardViewModel.loadUserData(userId);
        } else {
            // User not logged in, navigate to login
            navigateToLogin();
        }
    }
    
    private void initializeViews() {
        userNameTextView = findViewById(R.id.userNameTextView);
        currentWeightTextView = findViewById(R.id.currentWeightTextView);
        targetWeightTextView = findViewById(R.id.targetWeightTextView);
        lastWeightUpdateTextView = findViewById(R.id.lastWeightUpdateTextView);
        calorieTargetTextView = findViewById(R.id.calorieTargetTextView);
        bmrTextView = findViewById(R.id.bmrTextView);
        tdeeTextView = findViewById(R.id.tdeeTextView);
        goalTextView = findViewById(R.id.goalTextView);
        proteinTextView = findViewById(R.id.proteinTextView);
        carbsTextView = findViewById(R.id.carbsTextView);
        fatsTextView = findViewById(R.id.fatsTextView);
        recommendationTextView = findViewById(R.id.recommendationTextView);
        addWeightButton = findViewById(R.id.addWeightButton);
        profileFab = findViewById(R.id.profileFab);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        nutritionCard = findViewById(R.id.nutritionCard);
        recommendationCard = findViewById(R.id.recommendationCard);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }
    
    private void setupListeners() {
        // Add weight button
        addWeightButton.setOnClickListener(v -> {
            showAddWeightDialog();
        });
        
        // Profile FAB
        profileFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // Bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.navigation_progress) {
                Intent progressIntent = new Intent(MainActivity.this, ProgressActivity.class);
                startActivity(progressIntent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }
            return false;
        });
    }
    
    private void observeViewModel() {
        // Observe user profile
        dashboardViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                updateUserInfo(user);
            }
        });
        
        // Observe latest weight
        dashboardViewModel.getLatestWeight().observe(this, weightProgress -> {
            if (weightProgress != null) {
                updateWeightInfo(weightProgress);
            }
        });
        
        // Observe nutrition calculation
        dashboardViewModel.getNutritionCalculation().observe(this, nutritionCalculation -> {
            if (nutritionCalculation != null) {
                updateNutritionInfo(nutritionCalculation);
                nutritionCard.setVisibility(View.VISIBLE);
            } else {
                nutritionCard.setVisibility(View.GONE);
            }
        });
        
        // Observe recommendation
        dashboardViewModel.getRecommendation().observe(this, recommendation -> {
            if (recommendation != null && !recommendation.isEmpty()) {
                recommendationTextView.setText(recommendation);
                recommendationCard.setVisibility(View.VISIBLE);
            } else {
                recommendationCard.setVisibility(View.GONE);
            }
        });
        
        // Observe loading state
        dashboardViewModel.isLoading().observe(this, isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe errors
        dashboardViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateUserInfo(User user) {
        userNameTextView.setText(getString(R.string.greeting, user.getName()));
        targetWeightTextView.setText(getString(R.string.target_weight_format, user.getTargetWeight()));
    }
    
    private void updateWeightInfo(WeightProgress weightProgress) {
        try {
            DecimalFormat df = new DecimalFormat("#.#");
            currentWeightTextView.setText(getString(R.string.current_weight_format, 
                    df.format(weightProgress.getWeight())));
            
            // Format date
            if (weightProgress.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
                String formattedDate = dateFormat.format(weightProgress.getDate());
                lastWeightUpdateTextView.setText(getString(R.string.last_update_format, formattedDate));
            }
        } catch (Exception e) {
            // Handle any exceptions to prevent crash
            Toast.makeText(this, "Error updating weight info", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateNutritionInfo(NutritionCalculation nutrition) {
        try {
            DecimalFormat df = new DecimalFormat("#");
            
            // Update calorie information
            calorieTargetTextView.setText(df.format(nutrition.getDailyCalorieTarget()));
            bmrTextView.setText(df.format(nutrition.getBmr()));
            tdeeTextView.setText(df.format(nutrition.getTdee()));
            
            // Update goal
            String goalText;
            String goal = nutrition.getGoal();
            if (goal == null) goal = "MAINTENANCE";
            
            switch (goal) {
                case "WEIGHT_LOSS":
                    goalText = "Penurunan Berat Badan";
                    break;
                case "WEIGHT_GAIN":
                    goalText = "Penambahan Berat Badan";
                    break;
                case "MAINTENANCE":
                default:
                    goalText = "Pemeliharaan Berat Badan";
                    break;
            }
            goalTextView.setText(goalText);
            
            // Update macros
            proteinTextView.setText(df.format(nutrition.getProteinGrams()) + "g");
            carbsTextView.setText(df.format(nutrition.getCarbGrams()) + "g");
            fatsTextView.setText(df.format(nutrition.getFatGrams()) + "g");
        } catch (Exception e) {
            // Handle any exceptions to prevent crash
            nutritionCard.setVisibility(View.GONE);
        }
    }
    
    private void showAddWeightDialog() {
        // In a real implementation, you would show a dialog to get the new weight
        // For this example, we'll navigate to a dedicated activity
        try {
            Intent intent = new Intent(MainActivity.this, AddWeightActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Tidak dapat membuka halaman tambah berat badan", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            authViewModel.signOut();
            navigateToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}