package com.example.tspapp;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class TspBacktrackingSolver {
    private static int n;
    private static int[][] dist;
    private static boolean[] visited;
    private static int bestCost;
    private static List<Integer> bestPath;
    private static List<Integer> currentPath;

    public static Pair<List<Integer>,Integer> solve(int[][] matrix) {
        dist = matrix;
        n = dist.length;
        visited = new boolean[n];
        bestCost = Integer.MAX_VALUE;
        bestPath = new ArrayList<>();
        currentPath = new ArrayList<>();

        // start at city 0
        visited[0] = true;
        currentPath.add(0);
        backtrack(0, 1, 0);

        if (bestCost == Integer.MAX_VALUE) return null;
        return Pair.create(bestPath, bestCost);
    }

    private static void backtrack(int lastCity, int count, int costSoFar) {
        // if all cities visited, add return edge to 0
        if (count == n) {
            int totalCost = costSoFar + dist[lastCity][0];
            if (totalCost < bestCost) {
                bestCost = totalCost;
                bestPath = new ArrayList<>(currentPath);
                bestPath.add(0);
            }
            return;
        }

        // try each unvisited city
        for (int next = 1; next < n; next++) {
            if (!visited[next]) {
                int newCost = costSoFar + dist[lastCity][next];
                if (newCost >= bestCost) continue;  // prune

                visited[next] = true;
                currentPath.add(next);

                backtrack(next, count + 1, newCost);

                visited[next] = false;
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }
}