package com.example.checkers.model;

/**
 * 8x8 board storing pieces only on dark squares (we still index full 8x8).
 */
public final class Board {
    private final Piece[][] squares; // [row][col]

    public Board() {
        this.squares = new Piece[8][8];
    }

    private Board(Piece[][] copy) {
        this.squares = copy;
    }

    public Piece get(int row, int col) {
        return squares[row][col];
    }

    public void set(int row, int col, Piece piece) {
        squares[row][col] = piece;
    }

    public static Board initial() {
        Board b = new Board();
        // BLACK starts at rows 0..2 (top), RED at 5..7 (bottom). RED moves first per
        // spec.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    b.set(row, col, new Piece(Color.BLACK, false));
                }
            }
        }
        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    b.set(row, col, new Piece(Color.RED, false));
                }
            }
        }
        return b;
    }

    /**
     * Deep copy of the board and pieces.
     */
    public Board copy() {
        Piece[][] cp = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = squares[r][c];
                if (p != null)
                    cp[r][c] = new Piece(p.getColor(), p.isKing());
            }
        }
        return new Board(cp);
    }

    public int count(Color color) {
        int cnt = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = squares[r][c];
                if (p != null && p.getColor() == color)
                    cnt++;
            }
        }
        return cnt;
    }

    public Piece[][] toArray() {
        return squares;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = squares[r][c];
                if (p == null) {
                    sb.append(".");
                } else {
                    sb.append(p.toString());
                }
                if (c < 7)
                    sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
