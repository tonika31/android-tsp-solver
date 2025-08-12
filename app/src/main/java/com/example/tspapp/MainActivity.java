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

public class MainActivity extends AppCompatActivity {
    private LinearLayout pairsContainer;
    private MaterialButton solveButton;
    private TextView resultText;
    private AutoCompleteTextView cityCountDropdown;

    private int cityCount = 4;  // default
    private final List<Pair<Integer,Integer>> pairs = new ArrayList<>();
    private final List<TextInputEditText> inputs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pairsContainer      = findViewById(R.id.pairsContainer);
        solveButton         = findViewById(R.id.solveButton);
        resultText          = findViewById(R.id.resultText);
        cityCountDropdown   = findViewById(R.id.cityCountDropdown);

        setupCityCountDropdown();
        generatePairInputs(cityCount);

        solveButton.setOnClickListener(v -> {
            if (!validateInputs()) {
                resultText.setText("Please fill all distances");
                return;
            }
            int[][] matrix = buildSymmetricMatrix();
            new SolveTspTask().execute(matrix);
        });
    }

    private void setupCityCountDropdown() {
        // Only allow 4, 5, or 6 cities
        String[] counts = { "4", "5", "6" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                counts
        );

        cityCountDropdown.setAdapter(adapter);

        // Set default to 4 (or whatever you prefer)
        cityCount = 4;
        cityCountDropdown.setText(String.valueOf(cityCount), false);

        // Prevent manual typing—only selection
        cityCountDropdown.setKeyListener(null);

        cityCountDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            cityCount = Integer.parseInt(counts[pos]);
            pairsContainer.removeAllViews();
            pairs.clear();
            inputs.clear();
            generatePairInputs(cityCount);
        });
    }

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
                label.setText(String.format("Distance City: %d ↔ %d", i, j));

                TextInputEditText input =
                        row.findViewById(R.id.distanceInput);

                pairsContainer.addView(row);
                inputs.add(input);
            }
        }
    }

    private boolean validateInputs() {
        for (TextInputEditText et : inputs) {
            if (et.getText() == null ||
                    et.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int[][] buildSymmetricMatrix() {
        int n = cityCount;
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) matrix[i][i] = 0;

        for (int idx = 0; idx < pairs.size(); idx++) {
            Pair<Integer,Integer> p = pairs.get(idx);
            int d = Integer.parseInt(inputs.get(idx)
                    .getText().toString());
            matrix[p.first][p.second] = d;
            matrix[p.second][p.first] = d;
        }
        return matrix;
    }

    private class SolveTspTask
            extends AsyncTask<int[][],Void,Pair<List<Integer>,Integer>> {

        @Override
        protected void onPreExecute() {
            resultText.setText("Solving...");
        }

        @Override
        protected Pair<List<Integer>,Integer> doInBackground(
                int[][]... params) {
            return TspBacktrackingSolver.solve(params[0]);
        }

        @Override
        protected void onPostExecute(
                Pair<List<Integer>,Integer> result) {
            if (result == null) {
                resultText.setText("No solution found");
            } else {
                resultText.setText(
                        "Tour: " + result.first +
                                "\nDistance: " + result.second);
            }
        }
    }
}