package com.example.checkers.model;

/**
 * Player color.
 */
public enum Color {
    RED,
    BLACK;

    /**
     * Returns the opposing color.
     */
    public Color opponent() {
        return this == RED ? BLACK : RED;
    }
}


