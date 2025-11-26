package com.example.android_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private GestureDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        database = new GestureDatabase(this);

        Button writeButton = findViewById(R.id.writeButton);
        Button unlockButton = findViewById(R.id.unlockButton);

        writeButton.setOnClickListener(v -> showNameInputDialog());
        unlockButton.setOnClickListener(v -> showUserSelectionDialog());
    }

    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Gesture Pattern");

        final EditText input = new EditText(this);
        input.setHint("Enter your name");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        // Padding dla EditText w dialogu
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("Start", (dialog, which) -> {
            String name = input.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.length() < 2) {
                Toast.makeText(this, "Name must be at least 2 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (database.userExists(name)) {
                Toast.makeText(this, "User '" + name + "' already exists!", Toast.LENGTH_SHORT).show();
                return;
            }

            startGestureActivity(name, GestureMode.REGISTER);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUserSelectionDialog() {
        // user list from db
        List<String> users = database.getAllUserNames();

        if (users.isEmpty()) {
            Toast.makeText(this, "No registered users. Please create a gesture first.", Toast.LENGTH_LONG).show();
            return;
        }

        String[] userArray = users.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select User to Unlock");
        builder.setItems(userArray, (dialog, which) -> {
            String selectedUser = userArray[which];
            startGestureActivity(selectedUser, GestureMode.UNLOCK);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void startGestureActivity(String userName, GestureMode mode) {
        Intent intent = new Intent(this, GestureActivity.class);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("MODE", mode.name());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refreshing db when back to menu
        if (database != null) {
            database.close();
            database = new GestureDatabase(this);
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

enum GestureMode {
    REGISTER,  // saving 5 gestures
    UNLOCK     // verification
}
