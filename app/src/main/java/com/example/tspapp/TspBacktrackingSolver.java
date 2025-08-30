package com.example.tspapp;

import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

// Solves the Traveling Salesman Problem using backtracking
public class TspBacktrackingSolver {
    // Problem state variables
    private static int n;                    // Number of cities
    private static int[][] dist;             // Distance matrix
    private static boolean[] visited;        // Visited cities tracker
    private static int bestCost;             // Best tour cost found
    private static List<Integer> bestPath;   // Best tour path found
    private static List<Integer> currentPath;// Current path during search

    // Entry point: solves TSP for the given distance matrix
    public static Pair<List<Integer>,Integer> solve(int[][] matrix) {
        dist = matrix;
        n = dist.length;
        visited = new boolean[n];
        bestCost = Integer.MAX_VALUE;
        bestPath = new ArrayList<>();
        currentPath = new ArrayList<>();

        // Start search from city 0
        visited[0] = true;
        currentPath.add(0);
        backtrack(0, 1, 0);

        // Return result if found, else null
        if (bestCost == Integer.MAX_VALUE) return null;
        return Pair.create(bestPath, bestCost);
    }

    // Recursive backtracking to explore all possible tours
    private static void backtrack(int lastCity, int count, int costSoFar) {
        // If all cities visited, complete the tour by returning to start
        if (count == n) {
            int totalCost = costSoFar + dist[lastCity][0];
            if (totalCost < bestCost) {
                bestCost = totalCost;
                bestPath = new ArrayList<>(currentPath);
                bestPath.add(0); // Complete the cycle
            }
            return;
        }

        // Try each unvisited city as the next step
        for (int next = 1; next < n; next++) {
            if (!visited[next]) {
                int newCost = costSoFar + dist[lastCity][next];
                if (newCost >= bestCost) continue;  // Prune paths worse than best

                visited[next] = true;
                currentPath.add(next);

                backtrack(next, count + 1, newCost);

                // Backtrack: unmark city and remove from path
                visited[next] = false;
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }
}