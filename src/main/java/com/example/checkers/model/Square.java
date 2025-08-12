package com.example.checkers.model;

/**
 * Immutable board square coordinate (0-indexed).
 */
public record Square(int row, int col) {
    public boolean isOnBoard() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
