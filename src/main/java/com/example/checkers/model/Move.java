package com.example.checkers.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A move is a path across squares; captures are encoded by jumps of 2.
 */
public final class Move {
    private final List<Square> path;

    public Move(List<Square> path) {
        Objects.requireNonNull(path);
        if (path.size() < 2) {
            throw new IllegalArgumentException("Move path must have at least 2 squares");
        }
        this.path = List.copyOf(path);
    }

    public List<Square> getPath() {
        return path;
    }

    public Square from() { return path.get(0); }
    public Square to() { return path.get(path.size() - 1); }

    public boolean isCapture() {
        // If any step is a jump of 2 rows, it's a capture
        for (int i = 1; i < path.size(); i++) {
            if (Math.abs(path.get(i).row() - path.get(i-1).row()) == 2) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) {
                int dr = Math.abs(path.get(i+1).row() - path.get(i).row());
                sb.append(dr == 2 ? " x " : " - ");
            }
        }
        return sb.toString();
    }

    public static Move of(Square... squares) {
        List<Square> list = new ArrayList<>();
        Collections.addAll(list, squares);
        return new Move(list);
    }
}


