package com.example.sae41;

import java.util.List;

public class Puzzle {
    private int size;
    private String name;
    private List<Point[]> pairs;
    private boolean valid;
    private String fileName;
    
    public Puzzle(int size, String name, List<Point[]> pairs, boolean valid, String fileName) {
        this.size = size;
        this.name = name;
        this.pairs = pairs;
        this.valid = valid;
        this.fileName = fileName;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Point[]> getPairs() {
        return pairs;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getFileName() {
        return fileName;
    }
}
