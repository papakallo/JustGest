package com.example.android_app;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GestureActivity extends AppCompatActivity {

    private PatternGestureView gestureView;
    private TextView statusText;
    private TextView attemptText;

    private String userName;
    private GestureMode mode;
    private GestureDatabase database;

    private boolean same = true;
    private int current = 0;

    // REGISTER
    private List<String> collectedGestures = new ArrayList<>();
    private static final int REQUIRED_SAMPLES = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        gestureView = findViewById(R.id.patternGestureView);
        statusText = findViewById(R.id.statusText);
        attemptText = findViewById(R.id.attemptText);

        userName = getIntent().getStringExtra("USER_NAME");
        String modeStr = getIntent().getStringExtra("MODE");
        mode = GestureMode.valueOf(modeStr);

        database = new GestureDatabase(this);

        if (mode == GestureMode.REGISTER) {
            setupRegistrationMode();
        } else {
            setupUnlockMode();
        }

        gestureView.setOnPatternCompleteListener(this::onGestureComplete);
    }

    private void setupRegistrationMode() {
        statusText.setText("Registration: " + userName);
        statusText.setTextColor(Color.parseColor("#A494C5"));
        updateAttemptCounter();
    }

    private void setupUnlockMode() {
        statusText.setText("Unlock as: " + userName);
        statusText.setTextColor(Color.parseColor("#4CAF50"));
        attemptText.setText("Draw your pattern");
    }

    private void updateAttemptCounter() {
        if (same){
            current = collectedGestures.size() + 1;
        }
        attemptText.setText("Attempt " + current + " / " + REQUIRED_SAMPLES);
    }

    private void onGestureComplete(int[] pattern) {
        if (mode == GestureMode.REGISTER) {
            handleRegistrationGesture(pattern);
        } else {
            handleUnlockGesture(pattern);
        }
    }

    private void handleRegistrationGesture(int[] pattern) {
        android.util.Log.d("GestureActivity", "handleRegistrationGesture called, pattern length: " + pattern.length);

        // min pattern length
        if (pattern.length < 4) {
            Toast.makeText(this, "Pattern too short! Minimum 4 dots.", Toast.LENGTH_SHORT).show();
            return;
        }

        // pull last saved JSON
        String gestureJson = gestureView.getLastGestureJson();
        android.util.Log.d("GestureActivity", "gestureJson: " + (gestureJson != null ? "OK" : "NULL"));

        if (gestureJson == null) {
            Toast.makeText(this, "Error capturing gesture data", Toast.LENGTH_SHORT).show();
            return;
        }

        // checking logic pattern
        if (!collectedGestures.isEmpty()) {
            String firstPattern = extractPatternFromJson(collectedGestures.get(0));
            String currentPattern = Arrays.toString(pattern);

            if (!firstPattern.equals(currentPattern)) {
                Toast.makeText(this, "Pattern doesn't match! Please draw the same pattern.", Toast.LENGTH_LONG).show();
                same = false;
                return;
            }
            same = true;
        }

        collectedGestures.add(gestureJson);

        Toast.makeText(this, "Gesture " + collectedGestures.size() + " captured!", Toast.LENGTH_SHORT).show();

        if (collectedGestures.size() >= REQUIRED_SAMPLES) {
            saveUserToDatabase();
        } else {
            updateAttemptCounter();
            same = false;
        }
    }

    private void handleUnlockGesture(int[] pattern) {
        if (pattern.length < 4) {
            Toast.makeText(this, "Pattern too short!", Toast.LENGTH_SHORT).show();
            return;
        }

        String gestureJson = gestureView.getLastGestureJson();
        if (gestureJson == null) {
            showUnlockResult(false, "Error capturing gesture");
            return;
        }

        // pull user gestures saved in db
        List<String> userGestures = database.getUserGestures(userName);

        if (userGestures.isEmpty()) {
            showUnlockResult(false, "No gestures found for user");
            return;
        }

        // compare patterns
        String currentPattern = Arrays.toString(pattern);
        String storedPattern = extractPatternFromJson(userGestures.get(0));

        if (!currentPattern.equals(storedPattern)) {
            showUnlockResult(false, "Wrong pattern!");
            return;
        }

        // TODO DTW etc

        showUnlockResult(true, "Pattern matched!");
    }

    private String extractPatternFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("pattern_order");
            int[] pattern = new int[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                pattern[i] = arr.getInt(i);
            }
            return Arrays.toString(pattern);
        } catch (Exception e) {
            return "";
        }
    }

    private void saveUserToDatabase() {
        boolean success = database.saveUser(userName, collectedGestures);

        if (success) {
            statusText.setText("Registration Complete!");
            statusText.setTextColor(Color.parseColor("#4CAF50"));
            attemptText.setText(REQUIRED_SAMPLES + " gestures saved for " + userName);

            Toast.makeText(this, "User registered successfully!", Toast.LENGTH_LONG).show();

            // back to menu in 2s
            gestureView.postDelayed(() -> finish(), 2000);
        } else {
            Toast.makeText(this, "Error saving to database!", Toast.LENGTH_LONG).show();
        }
    }

    private void showUnlockResult(boolean success, String message) {
        if (success) {
            statusText.setText("UNLOCKED");
            statusText.setTextColor(Color.parseColor("#4CAF50"));
            attemptText.setText(message);
            Toast.makeText(this, "Welcome, " + userName + "!", Toast.LENGTH_LONG).show();

            // back to menu in 2s
            gestureView.postDelayed(() -> finish(), 2000);
        } else {
            statusText.setText("DENIED");
            statusText.setTextColor(Color.parseColor("#F44336"));
            attemptText.setText(message);
            Toast.makeText(this, "Access denied!", Toast.LENGTH_SHORT).show();

            // try again in 2s
            gestureView.postDelayed(() -> {
                statusText.setText("Unlock as: " + userName);
                statusText.setTextColor(Color.parseColor("#4CAF50"));
                attemptText.setText("Try again");
            }, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}