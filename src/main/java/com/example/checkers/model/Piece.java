package com.example.checkers.model;

import java.util.Objects;

/**
 * A checkers piece: color and whether it is a king.
 */
public final class Piece {
    private final Color color;
    private final boolean king;

    public Piece(Color color, boolean king) {
        this.color = Objects.requireNonNull(color);
        this.king = king;
    }

    public Color getColor() {
        return color;
    }

    public boolean isKing() {
        return king;
    }

    public Piece makeKing() {
        if (king)
            return this;
        return new Piece(color, true);
    }

    /**
     * Promote the piece to king if it reaches the back rank for its color.
     */
    public Piece promoteIfNeeded(int row) {
        if (!king) {
            if (color == Color.RED && row == 0) {
                return makeKing();
            }
            if (color == Color.BLACK && row == 7) {
                return makeKing();
            }
        }
        return this;
    }

    @Override
    public String toString() {
        if (color == Color.RED) {
            return king ? "👑" : "🔴"; // Red piece: crown for king, circle for regular
        }
        return king ? "👑" : "⚫"; // Black piece: crown for king, circle for regular
    }
}
