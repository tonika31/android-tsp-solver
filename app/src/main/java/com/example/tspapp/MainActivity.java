package com.example.tspapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

// Main activity for the TSP app
public class MainActivity extends AppCompatActivity {

    // UI components
    private LinearLayout pairsContainer;
    private MaterialButton solveButton;
    private MaterialButton exportButton;
    private TextView resultText;
    private AutoCompleteTextView cityCountDropdown;

    // State variables
    private int cityCount = 4;
    private final List<Pair<Integer, Integer>> pairs = new ArrayList<>();
    private final List<TextInputEditText> inputs = new ArrayList<>();

    private String lastTourStr = null;
    private int lastDistance = 0;
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        pairsContainer = findViewById(R.id.pairsContainer);
        solveButton = findViewById(R.id.solveButton);
        exportButton = findViewById(R.id.exportButton);
        resultText = findViewById(R.id.resultText);
        cityCountDropdown = findViewById(R.id.cityCountDropdown);

        // Setup dropdown for city count selection
        setupCityCountDropdown();
        // Generate input fields for city pairs
        generatePairInputs(cityCount);

        // Set up solve button click listener
        solveButton.setOnClickListener(v -> {
            if (!validateInputs()) {
                resultText.setText("Please fill all distances (Km)");
                return;
            }
            int[][] matrix = buildSymmetricMatrix();
            if (matrix != null) {
                new SolveTspTask().execute(matrix);
            }
        });

        // Disable export button until a solution is found
        exportButton.setEnabled(false);
        exportButton.setOnClickListener(v -> {
            if (lastTourStr != null) {
                exportResults(lastTourStr, lastDistance);
            } else {
                Toast.makeText(this, "No results to export", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sets up the dropdown for selecting the number of cities
    private void setupCityCountDropdown() {
        String[] counts = {"4", "5", "6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                counts
        );

        cityCountDropdown.setAdapter(adapter);
        cityCountDropdown.setText(String.valueOf(cityCount), false);
        cityCountDropdown.setKeyListener(null);

        // When a new city count is selected, regenerate input fields
        cityCountDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            cityCount = Integer.parseInt(counts[pos]);
            pairsContainer.removeAllViews();
            pairs.clear();
            inputs.clear();
            generatePairInputs(cityCount);
        });
    }

    // Dynamically generates input fields for each city pair
    private void generatePairInputs(int n) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                pairs.add(Pair.create(i, j));
                View row = inflater.inflate(R.layout.item_distance_input, pairsContainer, false);

                TextView label = row.findViewById(R.id.labelText);
                label.setText(String.format("Distance City (km): %d ↔ %d", i, j));

                TextInputEditText input = row.findViewById(R.id.distanceInput);
                pairsContainer.addView(row);
                inputs.add(input);
            }
        }
    }

    // Validates that all input fields are filled
    private boolean validateInputs() {
        for (TextInputEditText et : inputs) {
            if (et.getText() == null || et.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Builds a symmetric distance matrix from the input fields
    private int[][] buildSymmetricMatrix() {
        int n = cityCount;
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) matrix[i][i] = 0;

        for (int idx = 0; idx < pairs.size(); idx++) {
            Pair<Integer, Integer> p = pairs.get(idx);
            try {
                int d = Integer.parseInt(inputs.get(idx).getText().toString());
                matrix[p.first][p.second] = d;
                matrix[p.second][p.first] = d;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input at pair " + p.first + " ↔ " + p.second, Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return matrix;
    }

    // AsyncTask to solve the TSP problem in the background
    private class SolveTspTask extends AsyncTask<int[][], Void, Pair<List<Integer>, Integer>> {

        @Override
        protected void onPreExecute() {
            resultText.setText("Solving...");
        }

        @Override
        protected Pair<List<Integer>, Integer> doInBackground(int[][]... params) {
            // Call the TSP solver
            return TspBacktrackingSolver.solve(params[0]);
        }

        @Override
        protected void onPostExecute(Pair<List<Integer>, Integer> result) {
            if (result == null) {
                resultText.setText("No solution found");
            } else {
                // Build tour string for display
                StringBuilder tourStr = new StringBuilder();
                for (int i = 0; i < result.first.size(); i++) {
                    tourStr.append("City ").append(result.first.get(i) + 1);
                    if (i < result.first.size() - 1) tourStr.append(" → ");
                }

                String message = "Tour:\n" + tourStr + "\n\nTotal Distance: " + result.second + " km";

                // Show solution in a dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("TSP Solution")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();

                resultText.setText("");
                lastTourStr = tourStr.toString();
                lastDistance = result.second;
                exportButton.setEnabled(true);
            }
        }
    }

    // Handles the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Exports the TSP results to a file
    private void exportResults(String tour, int distance) {
        String filename = "tsp_result.txt";
        String content = "Tour:\n" + tour + "\n\nTotal Distance: " + distance + " km";
        try {
            File file = new File(getExternalFilesDir(null), filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Results exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}