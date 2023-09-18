package com.example.imageeditor;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SilhouetteCoefficient {

    public final double value;

    SilhouetteCoefficient(List<List<Color>> clusters) {
        // Compute the silhouette coefficient for each data point.
        int N = clusters.size();
        double[] silhouetteCoefficients = new double[N];

        for (int i = 0; i < N; i++) {  // for each cluster
            List<Color> cluster = clusters.get(i);
            double[] clusterValues = new double[cluster.size()];

            for (int j = 0; j < cluster.size(); ++j) {  // for each color point
                double a = computeIntraClusterDistance(cluster.get(j), cluster);
                double b = computeInterClusterDistance(cluster.get(j), cluster, clusters);
                clusterValues[j] = (b - a) / Math.max(a, b);
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


    private static double computeIntraClusterDistance(Color color, List<Color> cluster) {
        double distance = 0.0;
        for (Color otherColor : cluster) {
            distance += computeDistance(color, otherColor);
        }
        return distance / cluster.size();
    }

    private static double computeInterClusterDistance(Color color, List<Color> cluster, List<List<Color>> clusters) {
        double minDistance = Double.MAX_VALUE;
        for (List<Color> otherCluster : clusters) {
            if (cluster == otherCluster)
                continue;

            double distance = computeIntraClusterDistance(color, otherCluster);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    private static double computeDistance(Color point1, Color point2) {
        double distanceSquared = Math.abs(point1.getRed() - point2.getRed())
                + Math.abs(point1.getGreen() - point2.getGreen())
                + Math.abs(point1.getBlue() - point2.getBlue());
        return distanceSquared;
    }

}
