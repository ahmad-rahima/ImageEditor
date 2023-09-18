package com.example.imageeditor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class SimplifiedSilhouetteCoefficient {

    public final double value;

    SimplifiedSilhouetteCoefficient(List<List<Integer>> clusters, int[] centroids) {
        int N = clusters.size();
        double[] silhouetteCoefficients = new double[N];

        for (int i = 0; i < N; i++) {  // for each cluster
            List<Integer> cluster = clusters.get(i);
            double[] clusterValues = new double[cluster.size()];

            for (int j = 0; j < cluster.size(); ++j) {  // for each color point
                double a = computeDistance(cluster.get(j), centroids[i]);

                double minDistance = Double.MAX_VALUE;
                for (int k = 0; k < centroids.length; ++k) {
                    if (k == i)
                        continue;

                    double distance = computeDistance(cluster.get(j), centroids[k]);

                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }

                double b = minDistance;
                clusterValues[j] = (b - a) / Math.max(a, b);
//                if (Double.isNaN(clusterValues[j])) {
//                    System.out.println("NaN: a: " + a + ", b: " + b);
//                }
            }

            silhouetteCoefficients[i] = Arrays
                    .stream(clusterValues)
                    .average()
                    .orElse(Double.NaN);  // NaN or 0.0?
        }

        this.value = Arrays
                .stream(silhouetteCoefficients)
                .max()
                .orElse(Double.NaN);
    }

    private static double computeDistance(Color point1, Color point2) {
        double distanceSquared = Math.abs(point1.getRed() - point2.getRed())
                + Math.abs(point1.getGreen() - point2.getGreen())
                + Math.abs(point1.getBlue() - point2.getBlue());
        return distanceSquared;
    }

    private static double computeDistance(int color1, int color2) {
        int redDiff = ((color1 >> 16) & 0xFF) - ((color2 >> 16) & 0xFF);
        int greenDiff = ((color1 >> 8) & 0xFF) - ((color2 >> 8) & 0xFF);
        int blueDiff = (color1 & 0xFF) - (color2 & 0xFF);
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }


}
