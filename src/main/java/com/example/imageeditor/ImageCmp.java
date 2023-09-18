package com.example.imageeditor;

public interface ImageCmp<Target, Other> {
    double compare(Target target, Other other);
}
