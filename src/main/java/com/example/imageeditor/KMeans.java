package com.example.imageeditor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class KMeans extends ImageQuantizer<int[]> {
    private int[][] centroids;
    private int[] uniqueColors;

    protected static final int PREF_LEAVES = 32;
    static private final int EPOCHS = 10;
    private TreeMap<Integer, Double>  silhouetteCoefficientMap;

    public KMeans(BufferedImage im) {
        super(im);
    }

    private int[] getUniqueColors() {
        return new HashSet<Integer>(
                Arrays.stream(this.colors)
                        .boxed()
                        .collect(Collectors.toList())
        ).stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }

    // TODO BUG: handle the k=1 condition!
    @Override
    protected int findBestColorsNo() {
        // for each k possibility [2..MAX_K]
        this.centroids = new int[PREF_LEAVES][];
        this.uniqueColors = this.getUniqueColors();
        this.silhouetteCoefficientMap = new TreeMap<>();

        for (int k = PREF_LEAVES - 1; k > 1; k -= 5) {
            SimplifiedSilhouetteCoefficient silhouetteCoefficient = this.computeForCluster(k, this.tmpIm);
//            System.out.println(k + ", " + silhouetteCoefficient.value);
            this.silhouetteCoefficientMap.put(k, silhouetteCoefficient.value);
        }

        int bestK = this.silhouetteCoefficientMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();

        return bestK;
    }

    static private List<List<Color>> splitClusters(int[] cluster, Color[] colors, int k) {
        var res = new ArrayList<ArrayList<Color>>(k);
        for (int i = 0; i < k; ++i)
            res.add(new ArrayList<Color>());

        for (int i = 0; i < cluster.length; ++i)
            res.get(cluster[i]).add(colors[i]);

        return (List)res;
    }

    static private List<List<Integer>> splitClusters(int[] cluster, int[] colors, int k) {
        var res = new ArrayList<ArrayList<Integer>>(k);
        for (int i = 0; i < k; ++i)
            res.add(new ArrayList<Integer>());

        for (int i = 0; i < cluster.length; ++i)
            res.get(cluster[i]).add(colors[i]);

        return (List)res;
    }

    private SimplifiedSilhouetteCoefficient computeForCluster(int k, BufferedImage im) {
        this.centroids[k] = new int[k];
        int l = this.uniqueColors.length;

        for (int i = 0; i < k; ++i) {
            this.centroids[k][i] =
                    this.uniqueColors[(int) (Math.random() * l)];
        }

        int width = im.getWidth(), height = im.getHeight();
        int n = this.centroids[k].length;

        int[] cluster = new int[colors.length];

        for (int j = 0; j < EPOCHS; ++j) {

            int[] clusterSizes = new int[n];
            int[] redSums = new int[n];
            int[] greenSums = new int[n];
            int[] blueSums = new int[n];

            // Assign pixels to clusters
            int idx = 0;
            for (int color : colors) {
                int clusterIndex = 0;
                double minDistance = Double.MAX_VALUE;
                for (int i = 0; i < n; i++) {

                    var c = new Color((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff);
                    var cent = new Color((this.centroids[k][i] >> 16) & 0xff, (this.centroids[k][i] >> 8) & 0xff, this.centroids[k][i] & 0xff);
                    double distance = distance(c, cent);
//                    double distance = distance(color, this.centroids[k][i]);
                    if (distance < minDistance) {
                        minDistance = distance;
                        clusterIndex = i;
                    }
                }
                cluster[idx] = clusterIndex;
                clusterSizes[clusterIndex]++;
                redSums[clusterIndex] += (color >> 16) & 0xFF;
                greenSums[clusterIndex] += (color >> 8) & 0xFF;
                blueSums[clusterIndex] += color & 0xFF;

                ++idx;
            }

            // Update cluster centroids
            for (int i = 0; i < n; i++) {
                int clusterSize = clusterSizes[i];
                int redMean = clusterSize == 0 ? 0 : redSums[i] / clusterSize;
                int greenMean = clusterSize == 0 ? 0 : greenSums[i] / clusterSize;
                int blueMean = clusterSize == 0 ? 0 : blueSums[i] / clusterSize;
                this.centroids[k][i] = new Color(redMean, greenMean, blueMean).getRGB();
            }
        }

        List<List<Integer>> clusters = splitClusters(cluster, colors, n);

        return new SimplifiedSilhouetteCoefficient(clusters, centroids[k]);
    }

    @Override
    protected List<Color> compute() {
        return Arrays.stream(this.centroids[this.pref_colors])
                .mapToObj(c -> new Color(c >> 16 & 0xff, c >> 8 & 0xff, c & 0xff))
                .toList();
    }

    public static void main(String[] args) throws IOException {
        BufferedImage im = ImageIO.read(new File("/home/adr/Downloads/andrew-tate.png"));
        KMeans q = new KMeans(im);

        ImageIO.write(q.getImage(), "png", new File("newimage.png"));
    }

}
