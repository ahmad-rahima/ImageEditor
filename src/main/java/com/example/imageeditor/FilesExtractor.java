package com.example.imageeditor;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FilesExtractor {

    final private List<File> files = new ArrayList<>();
    final private List<String> suffixes;


    static public void main(String[] args) {
//        var formats = ImageIO.getReaderFormatNames();
//
//        var extractor = new FilesExtractor(new File("/home/adr/Pictures/"),
//                Arrays.stream(formats).toList());
//
//        for (File f : extractor.getFiles()) {
//            System.out.println(f.getName());
//        }
    }

    public List<File> getFiles() {
        return files;
    }

    public FilesExtractor(List<File> dir, List<String> suffixes) {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(suffixes);

        this.suffixes = suffixes;

        for (var d : dir)
            this.extract(d);
    }

    private void extract(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                this.extract(f);
                continue;
            }
            var segments = f.getName().split("\\.");
            if (this.suffixes.contains(segments[segments.length - 1])) {
                this.files.add(f);
            }
        }
    }
}
