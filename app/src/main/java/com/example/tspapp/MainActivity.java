package com.example.tspapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

// Main activity for the TSP app
public class MainActivity extends AppCompatActivity {
    // UI components
    private LinearLayout pairsContainer;
    private MaterialButton solveButton;
    private TextView resultText;
    private AutoCompleteTextView cityCountDropdown;

    // State variables
    private int cityCount = 4;  // default city count
    private final List<Pair<Integer, Integer>> pairs = new ArrayList<>(); // city pairs
    private final List<TextInputEditText> inputs = new ArrayList<>();    // input fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        pairsContainer = findViewById(R.id.pairsContainer);
        solveButton = findViewById(R.id.solveButton);
        resultText = findViewById(R.id.resultText);
        cityCountDropdown = findViewById(R.id.cityCountDropdown);

        setupCityCountDropdown();      // Setup dropdown for city count
        generatePairInputs(cityCount); // Generate input fields for distances

        // Set up button click to solve TSP
        solveButton.setOnClickListener(v -> {
            if (!validateInputs()) {
                resultText.setText("Please fill all distances (Km)");
                return;
            }
            int[][] matrix = buildSymmetricMatrix(); // Build distance matrix
            new SolveTspTask().execute(matrix);      // Solve TSP asynchronously
        });
    }

    // Sets up the dropdown for selecting city count
    private void setupCityCountDropdown() {
        // Only allow 4, 5, or 6 cities
        String[] counts = {"4", "5", "6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                counts
        );

        cityCountDropdown.setAdapter(adapter);

        // Set default to 4
        cityCount = 4;
        cityCountDropdown.setText(String.valueOf(cityCount), false);

        // Prevent manual typing—only selection
        cityCountDropdown.setKeyListener(null);

        // Regenerate input fields when city count changes
        cityCountDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            cityCount = Integer.parseInt(counts[pos]);
            pairsContainer.removeAllViews();
            pairs.clear();
            inputs.clear();
            generatePairInputs(cityCount);
        });
    }

    // Generates input fields for all city pairs
    private void generatePairInputs(int n) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                pairs.add(Pair.create(i, j));
                View row = inflater.inflate(
                        R.layout.item_distance_input,
                        pairsContainer,
                        false);

                TextView label = row.findViewById(R.id.labelText);
                label.setText(String.format("Distance City (km) : %d ↔ %d", i, j));

                TextInputEditText input =
                        row.findViewById(R.id.distanceInput);

                pairsContainer.addView(row);
                inputs.add(input);
            }
        }
    }

    // Validates that all input fields are filled
    private boolean validateInputs() {
        for (TextInputEditText et : inputs) {
            if (et.getText() == null ||
                    et.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Builds a symmetric distance matrix from user inputs
    private int[][] buildSymmetricMatrix() {
        int n = cityCount;
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) matrix[i][i] = 0;

        for (int idx = 0; idx < pairs.size(); idx++) {
            Pair<Integer, Integer> p = pairs.get(idx);
            int d = Integer.parseInt(inputs.get(idx)
                    .getText().toString());
            matrix[p.first][p.second] = d;
            matrix[p.second][p.first] = d;
        }
        return matrix;
    }

    // AsyncTask to solve TSP in the background
    private class SolveTspTask
            extends AsyncTask<int[][], Void, Pair<List<Integer>, Integer>> {

        @Override
        protected void onPreExecute() {
            resultText.setText("Solving...");
        }

        @Override
        protected Pair<List<Integer>, Integer> doInBackground(
                int[][]... params) {
            return TspBacktrackingSolver.solve(params[0]);
        }

        @Override
        protected void onPostExecute(Pair<List<Integer>, Integer> result) {
            // Display the result of the TSP solution
            if (result == null) {
                // No solution found, show error message
                resultText.setText("No solution found");
            } else {
                // Build the tour string for display
                StringBuilder tourStr = new StringBuilder();
                for (int i = 0; i < result.first.size(); i++) {
                    tourStr.append("City ").append(result.first.get(i) + 1);
                    if (i < result.first.size() - 1) tourStr.append(" → ");
                }
                // Prepare the message with the tour and total distance
                String message = "Tour:\n" + tourStr +
                        "\n\nTotal Distance: " + result.second + " km";

                // Show the result in an AlertDialog
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("TSP Solution")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();

                // Optionally clear the old result text
                resultText.setText("");
            }
        }
    }
}