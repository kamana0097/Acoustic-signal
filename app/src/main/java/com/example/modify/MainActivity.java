package com.example.modify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button startButton;
    private Button stopButton;
    private StringBuilder csvData;
    private boolean isRecording = false; // Flag to control recording state
    private Handler handler; // Handler for periodic recording
    private static final long RECORD_INTERVAL = 2000; // Record every 2 seconds
    private boolean isToastShowing = false; // Throttle toast messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        csvData = new StringBuilder();
        csvData.append("Timestamp,Frequency,Amplitude,Duration,SignalType\n");

        // Request permissions
        requestPermissionsIfNeeded();

        handler = new Handler(); // Initialize handler

        startButton.setOnClickListener(v -> startPlayingAndRecording());
        stopButton.setOnClickListener(v -> {
            stopPlayingAndRecording();
            saveDataToCSV();
        });
    }

    private void requestPermissionsIfNeeded() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void startPlayingAndRecording() {
        if (!checkPermissions()) {
            showToast("Permissions not granted!");
            return;
        }

        isRecording = true; // Set recording flag to true
        showToast("Started recording signals.");
        handler.post(recordRunnable); // Start periodic recording
    }

    private void stopPlayingAndRecording() {
        isRecording = false; // Set recording flag to false
        handler.removeCallbacks(recordRunnable); // Stop periodic recording
        showToast("Stopped playing and recording.");
    }

    // Runnable to collect data periodically
    private final Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                String timestamp = getCurrentTimestamp();
                double frequency = 440.0; // Example frequency
                double amplitude = 0.5; // Example amplitude
                double duration = 2.0; // Example duration
                String signalType = "Sine"; // Example signal type

                // Collect data
                csvData.append(timestamp).append(",")
                        .append(frequency).append(",")
                        .append(amplitude).append(",")
                        .append(duration).append(",")
                        .append(signalType).append("\n");

                // Simulated signal generation
                showToast("Recording signal: " + frequency + " Hz");
                handler.postDelayed(this, RECORD_INTERVAL); // Schedule next recording
            }
        }
    };

    private void saveDataToCSV() {
        String fileName = "AcousticSignalData.csv";
        File file = new File(getExternalFilesDir(null), fileName); // Use app-specific directory

        if (csvData.length() > 0) { // Check if there's data to write
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(csvData.toString());
                showToast("Data saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e("MainActivity", "Error writing to CSV file", e);
                showToast("Error saving data: " + e.getMessage());
            }
        } else {
            showToast("No data to save.");
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Audio recording permission granted!");
            } else {
                showToast("Audio recording permission denied!");
            }
        }
    }

    private void showToast(String message) {
        if (!isToastShowing) {
            isToastShowing = true;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(() -> isToastShowing = false, 2000); // Reset after 2 seconds
        }
    }
}
