package com.example.imageeditor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class OctreeColorQuantizer extends ImageQuantizer<int[][]> {

    private List<OctreeColorQuantizer.Node>[] levels;
    private static final int MAX_DEPTH = 3; // since we want 255!
    private static final double MAX_LEAVES = Math.floor(Math.pow(8, 3));
    private OctreeColorQuantizer.Node root;

    public static int[] destructColor(int color) {
        return new int[] { (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF };
    }

    public static float compare(OctreeColorQuantizer object, OctreeColorQuantizer another) {
        float similarity = 0;

        var objectNodes = new ArrayList<>(Arrays.asList(object.leafNodesFilled()));
        var anotherNodes = new ArrayList<>(Arrays.asList(another.leafNodesFilled()));

        if (objectNodes.size() != anotherNodes.size())
            System.out.println("This must not happens!!, " + objectNodes.size() + " v " + anotherNodes.size());
        float levelSimilarity = 0;
        for (int j = 0; j < Math.min(objectNodes.size(), anotherNodes.size()); ++j) {
            var dist = distance(scaleColor(objectNodes.get(j).color()), scaleColor(anotherNodes.get(j).color()));
            float colorSimilarity = 1 - (float)(dist / Math.sqrt(3));
            var colorPercentage = Math.min(
                    ((float) objectNodes.get(j).pixelCount / object.root.pixelCount),
                    ((float) anotherNodes.get(j).pixelCount / another.root.pixelCount)
            );

            levelSimilarity += colorPercentage * colorSimilarity;
        }
//        System.out.println(levelSimilarity);
        similarity += levelSimilarity * 1;

        return similarity;
    }

    public static float compare(OctreeColorQuantizer object, List<Color> colors) {
        float similarity = 0;

        for (var node : object.leafNodes()) {
            float dist = Float.MAX_VALUE;
            for (var c : colors) {
                float tmpDist = (float)distance(scaleColor(node.color()), scaleColor(c));
                if (tmpDist < dist) dist = tmpDist;
            }
            float colorSimilarity = 1 - dist / (float) Math.sqrt(3);
            var colorPercentage =
                    ((float) node.pixelCount / object.root.pixelCount);

            similarity += colorPercentage * colorSimilarity;
        }

        return similarity;
    }

    public static float measureSimilarity(OctreeColorQuantizer object, OctreeColorQuantizer another) {
        int depth = Math.max(object.MAX_DEPTH, another.MAX_DEPTH);
        float similarity = 0;

        var objectNodes = new ArrayList<>(object.levels[1]);
        var anotherNodes = new ArrayList<>(another.levels[1]);

        float levelSimilarity = 0;
        balanceListsWithMaxPixelNo(objectNodes, anotherNodes);  // same count

        for (int j = 0; j < objectNodes.size(); ++j) {
            var dist = distance(scaleColor(objectNodes.get(j).color()), scaleColor(anotherNodes.get(j).color()));
            float colorSimilarity = 1 - (float)(dist / 255);  // leaves!!!!
            var colorPercentage = Math.min(
                    ((float) objectNodes.get(j).pixelCount / object.root.pixelCount),
                    ((float) anotherNodes.get(j).pixelCount / another.root.pixelCount)
            );

            levelSimilarity += colorPercentage * colorSimilarity;
        }
        System.out.println(levelSimilarity);
        similarity += levelSimilarity * 1;

        levelSimilarity = 0;
        objectNodes = new ArrayList<>(Arrays.asList(object.leafNodes()));
        anotherNodes = new ArrayList<>(Arrays.asList(another.leafNodes()));
        balanceListsWithMaxPixelNo(objectNodes, anotherNodes);  // same count

        System.out.println(objectNodes.get(0).color());
        System.out.println(anotherNodes.get(0).color());
        if (objectNodes.size() != anotherNodes.size())
            System.out.println("This must not happen!!");
        for (int j = 0; j < objectNodes.size(); ++j) {
            var dist = distance(scaleColor(objectNodes.get(j).color()), scaleColor(anotherNodes.get(j).color()));
            float colorSimilarity = 1 - (float)(dist / 255);  // leaves!!!!
            var colorPercentage = Math.min(
                    ((float) objectNodes.get(j).pixelCount / object.root.pixelCount),
                    ((float) anotherNodes.get(j).pixelCount / another.root.pixelCount)
            );

            levelSimilarity += colorPercentage * colorSimilarity;
        }
        System.out.println(levelSimilarity);
        similarity += levelSimilarity * 1;

//        similarity /= 2;

        return similarity;
    }



    private static float[] scaleColor(Color color) {
        return new float[] {
                (float)color.getRed() / 255,
                (float)color.getGreen() / 255,
                (float)color.getBlue() / 255
        };
    }

    private static void balanceListsWithMaxPixelNo(ArrayList<Node> nodes, ArrayList<Node> other) {
        var diff = Math.abs(nodes.size() - other.size());
        if (diff < 1) return;

        var overflowNodes = nodes.size() - other.size() > 0 ? nodes : other;

        var sortedNodes = overflowNodes.stream()
                .sorted(Comparator.comparingInt(Node::getPixelCount))
                .toList();

        while (diff > 0) {
            for (int i = 0; i < overflowNodes.size() && diff > 0; ++i) {
                if (overflowNodes.get(i) == sortedNodes.get(diff - 1)) {
                    overflowNodes.remove(sortedNodes.get(diff - 1));
                    --diff;
                }
            }
        }

//        if (nodes.size() != other.size()) System.out.println("WTF!?" + nodes.size() + " is not " + other.size() + " and overflow is " + overflowNodes.size() + "and diff is: " + diff);
    }

    static double distance(int[] color0, int[] color1) {
        int redDiff = color0[0] - color1[0];
        int greenDiff = color0[1] - color1[1];
        int blueDiff = color0[2] - color1[2];
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    protected int findBestColorsNo() {
        return PREF_LEAVES;
    }

    public static int getColorIndex(int[] color, int level) {
        int index = 0;
        int mask = 0b10000000 >> level;
        if ((color[0] & mask) != 0) index |= 0b100;
        if ((color[1] & mask) != 0) index |= 0b010;
        if ((color[2] & mask) != 0) index |= 0b001;
        return index;
    }

    public static void addColor(int[] baseColor, int[] color) {
        baseColor[0] += color[0];
        baseColor[1] += color[1];
        baseColor[2] += color[2];
    }

    public static Color normalizeColor(int[] color, int pixelCount) {
        return new Color(
                (int) (color[0] * 1.0f / pixelCount),
                (int) (color[1] * 1.0f / pixelCount),
                (int) (color[2] * 1.0f / pixelCount)
        );
    }

    public OctreeColorQuantizer(BufferedImage im) {
        super(im);
    }

    public OctreeColorQuantizer(BufferedImage im, int pref_colors) {
        super(im, pref_colors);
    }

    @Override
    public int[][] getImageColors() {
        int[] rgbArray = this.tmpIm.getRGB(0, 0, this.tmpIm.getWidth(), this.tmpIm.getHeight(), null, 0, this.tmpIm.getWidth());
        return Arrays.stream(rgbArray)
                .mapToObj(OctreeColorQuantizer::destructColor)
                .toArray(int[][]::new);
    }

    @Override
    protected List<Color> compute() {
        this.levels = IntStream.range(0, MAX_DEPTH)
                .mapToObj(i -> new ArrayList<>())
                .toArray(List[]::new);

        root = new Node(0, this);

        for (var c : this.colors) this.addColor(c);

        var p = this.makePalette(PREF_LEAVES);
        this.root.computeParentColors(0);

        return p;
    }

    public OctreeColorQuantizer.Node[] leafNodes() {
        return root.leafNodes();
    }

    public OctreeColorQuantizer.Node[] leafNodesFilled() {
        return root.leafNodesFilled(0);
    }

    public List<Color> makePalette(int colorCount) {
        List<Color> palette = new ArrayList<>(colorCount);
//        for (int i = 0; i < colorCount; ++i) palette.add(null);

        int paletteIndex = 0;
        int leafCount = this.leafNodes().length;

        // reduce colors
        for (int level = MAX_DEPTH - 1; level > -1; level--) {
            if (levels[level] != null) {
                List<OctreeColorQuantizer.Node> nodes = levels[level];
                for (var node : nodes) {
                    leafCount -= node.removeLeaves();
                    if (leafCount <= colorCount) break;
                }
                if (leafCount <= colorCount) break;
                levels[level] = new ArrayList<>();
            }
        }

        OctreeColorQuantizer.Node[] leafNodes = this.leafNodes();
        for (OctreeColorQuantizer.Node node : leafNodes) {
            if (paletteIndex >= colorCount) break;
            if (node.isLeaf()) palette.add(node.color());
            node.setPaletteIndex(paletteIndex);
            paletteIndex++;
        }
        return palette;
    }

    public void addLevelNode(int level, OctreeColorQuantizer.Node node) {
        levels[level].add(node);
    }

    public void addColor(int[] colors) {
        root.addColor(colors,0, this);
    }


    static class Node {
        private int[] _color;
        private int pixelCount;
        private int paletteIndex;
        private OctreeColorQuantizer.Node[] children;

        public int getPixelCount() {
            return pixelCount;
        }

        public Node(int level, OctreeColorQuantizer parent) {
            _color = new int[] {0, 0, 0};
            pixelCount = 0;
            paletteIndex = 0;
            children = Stream.generate(() -> null)
                    .limit(8)
                    .toList()
                    .toArray(OctreeColorQuantizer.Node[]::new);

            if (level < MAX_DEPTH - 1) parent.addLevelNode(level, this);
        }

        private Node() {
            _color = new int[] {0, 0, 0};
            pixelCount = 0;
        }


        // TODO: change the following method
        public boolean isLeaf() {
            for (var child : children)
                if (child != null)
                    return false;
            return true;
        }

        public OctreeColorQuantizer.Node[] leafNodes() {
            List<OctreeColorQuantizer.Node> leafNodes = new ArrayList<>();
            for (var node : children) {
                if (node == null) continue;
                if (node.isLeaf()) {
                    leafNodes.add(node);
                } else {
                    leafNodes.addAll(List.of(node.leafNodes()));
                }
            }
            return leafNodes.toArray(OctreeColorQuantizer.Node[]::new);
        }

        public OctreeColorQuantizer.Node[] leafNodesFilled(int level) {
            List<OctreeColorQuantizer.Node> leafNodes = new ArrayList<>();
            for (var node : children) {
                if (level == 2) {
                    leafNodes.add(node == null ? new Node() : node);
                } else {
                    if (node == null)
                        for (int i = 0; i < Math.pow(8, 2 - level); ++i)
                            leafNodes.add(new Node());
                    else
                        leafNodes.addAll(List.of(node.leafNodesFilled(level+1)));
                }
            }
            return leafNodes.toArray(OctreeColorQuantizer.Node[]::new);
        }


        public void addColor(int[] color, int level, OctreeColorQuantizer parent) {
            if (level >= MAX_DEPTH) {
                OctreeColorQuantizer.addColor(_color, color);
                pixelCount++;
                return;
            }
            int index = getColorIndex(color, level);
            if (children[index] == null) {
                children[index] = new Node(level, parent);
            }
            pixelCount++;
            children[index].addColor(color, level + 1, parent);
        }

        public int[] computeParentColors(int level) {
            if (level < MAX_DEPTH) {
                for (var child : children) {
                    if (child == null)
                        continue;
                    OctreeColorQuantizer.addColor(_color,
                            child.computeParentColors(level + 1));
                }

                _color[0] /= children.length;
                _color[1] /= children.length;
                _color[2] /= children.length;
            }

            return _color;
        }

        public int removeLeaves() {
            int result = 0;
            for (var node : children) {
                if (node == null) continue;
                OctreeColorQuantizer.addColor(_color, node._color);
                pixelCount += node.pixelCount;
                result++;
            }
            Arrays.fill(children, null);
            return result - 1;
        }

        public void setPaletteIndex(int index) {
            paletteIndex = index;
        }

        public Color color() {
            return normalizeColor(_color, pixelCount);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedImage im = ImageIO.read(new File("/home/adr/Downloads/andrew-tate.png"));
        BufferedImage im1 = ImageIO.read(new File("/home/adr/Downloads/nice-wallpaper.png"));
        OctreeColorQuantizer q = new OctreeColorQuantizer(im);
        OctreeColorQuantizer q1 = new OctreeColorQuantizer(im);

        var v = OctreeColorQuantizer.measureSimilarity(q, q1);
        System.out.printf("Similarity: %s%n", v);
    }

}
