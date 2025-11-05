package com.example.android_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

//    private PatternView patternView;
//    private GestureView gestureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

//        patternView = findViewById(R.id.patternView);
//        gestureView = findViewById(R.id.gestureView);

        PatternGestureView v = findViewById(R.id.patternGestureView);
        v.setOnPatternCompleteListener(pattern -> {

            // <<here comparision logic and gesture biometrics>>

            runOnUiThread(() -> {
                Toast.makeText(this, "Pattern: " + Arrays.toString(pattern), Toast.LENGTH_SHORT).show();
            });
        });

    }
}
