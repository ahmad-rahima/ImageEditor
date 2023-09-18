package com.example.imageeditor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


public class ImageSearcher {

    public static class ImageResult {
        double priority;
        public File file;

        public double getPriority() { return this.priority; }

        public File get() { return this.file; }

        public ImageResult(double priority, File file) {
            this.priority = priority;
            this.file = file;
        }
    }

    private boolean done = false;
    // TODO: we need a thread-safe container!
    final private PriorityQueue<ImageResult> result = new PriorityQueue<>(
            Comparator.comparingDouble(ImageResult::getPriority)
    );

    public boolean isDone() {
        return this.done;
    }

    private float threshold;

    private Runnable doSearch(BufferedImage im, List<File> images, Function<File, Number> priorityFn) {
        return () -> {
            for (var other : images) {
//                System.out.println("Checking for " + other);
                double prt = priorityFn.apply(other).doubleValue();
                System.out.println("Checking for " + other + ", priority: " + prt); // deleting this caused problems!
                if (prt >= threshold)
                    this.result.add(new ImageResult(prt, other));
            }
        };
    }

    public PriorityQueue<ImageResult> getResult() {
        return this.result;
    }
    ImageSearcher(BufferedImage im, List<File> images, Function<File, Number> priorityFn) {
        this(im, images, priorityFn, .5f);
    }

    ImageSearcher(BufferedImage im, List<File> images, Function<File, Number> priorityFn, float threshold) {
        try {
            this.threshold = threshold;
            ExecutorService executor = Executors.newFixedThreadPool(4);
            int execSz = (int)Math.ceil((float)images.size() / 4);
            for (int i = 0; i < 3; ++i) {
                executor.submit(this.doSearch(im, images.subList(i * execSz, (i + 1) * execSz), priorityFn));
            }
            executor.submit(this.doSearch(im, images.subList(3 * execSz, images.size()), priorityFn));

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            this.done = true;
        } catch (InterruptedException e) {
            System.out.println("Interrupting.");
        }
    }

}
