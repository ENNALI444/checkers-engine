package com.example.checkers.engine;

import com.example.checkers.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules engine for American/English checkers.
 * Handles move validation, legal move generation, and move application.
 */
public class RulesEngine {

    /**
     * Generates all legal moves for a given player on the current board.
     * If any captures are available, only captures are returned (forced capture
     * rule).
     * 
     * @param board  The current board state
     * @param player The color of the player whose moves we're generating
     * @return List of all legal moves for the player
     */
    public List<Move> legalMoves(Board board, Color player) {
        List<Move> captures = new ArrayList<>();
        List<Move> simpleMoves = new ArrayList<>();

        // First, find all possible captures
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.get(row, col);
                if (piece != null && piece.getColor() == player) {
                    findCaptures(board, row, col, piece, new ArrayList<>(), captures);
                }
            }
        }

        // If no captures exist, find simple diagonal moves
        if (captures.isEmpty()) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.get(row, col);
                    if (piece != null && piece.getColor() == player) {
                        simpleMoves.addAll(findSimpleMoves(board, row, col, piece));
                    }
                }
            }
        }

        // Return captures if any exist, otherwise return simple moves
        return captures.isEmpty() ? simpleMoves : captures;
    }

    /**
     * Applies a move to the board and returns a new board instance.
     * The original board is never modified (immutable design).
     * 
     * @param board The current board state
     * @param move  The move to apply
     * @return A new board with the move applied
     */
    public Board apply(Board board, Move move) {
        Board newBoard = board.copy();
        List<Square> path = move.getPath();

        // Get the piece that's moving
        Square from = path.get(0);
        Piece movingPiece = newBoard.get(from.row(), from.col());

        // Remove piece from starting position
        newBoard.set(from.row(), from.col(), null);

        // Process each step of the move (for multi-jumps)
        for (int i = 1; i < path.size(); i++) {
            Square current = path.get(i - 1);
            Square next = path.get(i);

            // If this step is a jump (distance > 1), remove captured piece
            int rowDiff = Math.abs(next.row() - current.row());
            if (rowDiff == 2) {
                int capturedRow = current.row() + (next.row() - current.row()) / 2;
                int capturedCol = current.col() + (next.col() - current.col()) / 2;
                newBoard.set(capturedRow, capturedCol, null);
            }
        }

        // Place piece at final position
        Square to = path.get(path.size() - 1);
        Piece finalPiece = movingPiece.promoteIfNeeded(to.row());
        newBoard.set(to.row(), to.col(), finalPiece);

        return newBoard;
    }

    /**
     * Finds all possible capture moves for a piece starting from a given position.
     * Uses recursion to find multi-jump sequences.
     * 
     * @param board       The current board state
     * @param row         Starting row of the piece
     * @param col         Starting column of the piece
     * @param piece       The piece to move
     * @param currentPath The current path being explored
     * @param allCaptures List to collect all found captures
     */
    private void findCaptures(Board board, int row, int col, Piece piece,
            List<Square> currentPath, List<Move> allCaptures) {
        // Add current position to path
        currentPath.add(new Square(row, col));

        boolean foundCapture = false;

        // Check all possible capture directions
        int[][] directions = getCaptureDirections(piece);

        for (int[] direction : directions) {
            int jumpRow = row + 2 * direction[0];
            int jumpCol = col + 2 * direction[1];
            int middleRow = row + direction[0];
            int middleCol = col + direction[1];

            // Check if jump is within board bounds
            if (isValidPosition(jumpRow, jumpCol)) {
                Piece middlePiece = board.get(middleRow, middleCol);
                Piece landingPiece = board.get(jumpRow, jumpCol);

                // Check if we can capture: middle has opponent piece, landing is empty
                if (middlePiece != null && middlePiece.getColor() != piece.getColor()
                        && landingPiece == null) {

                    // Create a copy of the board to simulate this capture
                    Board tempBoard = board.copy();
                    tempBoard.set(row, col, null); // Remove from start
                    tempBoard.set(middleRow, middleCol, null); // Remove captured
                    tempBoard.set(jumpRow, jumpCol, piece); // Place at landing

                    // Recursively look for more captures from new position
                    findCaptures(tempBoard, jumpRow, jumpCol, piece,
                            new ArrayList<>(currentPath), allCaptures);

                    foundCapture = true;
                }
            }
        }

        // If no more captures possible and we have a path, add this move
        if (!foundCapture && currentPath.size() > 1) {
            allCaptures.add(new Move(new ArrayList<>(currentPath)));
        }
    }

    /**
     * Finds all simple diagonal moves (non-captures) for a piece.
     * 
     * @param board The current board state
     * @param row   Row of the piece
     * @param col   Column of the piece
     * @param piece The piece to move
     * @return List of simple moves for this piece
     */
    private List<Move> findSimpleMoves(Board board, int row, int col, Piece piece) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = getMovementDirections(piece);

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            if (isValidPosition(newRow, newCol) && board.get(newRow, newCol) == null) {
                moves.add(new Move(List.of(new Square(row, col), new Square(newRow, newCol))));
            }
        }

        return moves;
    }

    /**
     * Gets the valid movement directions for a piece.
     * Men can only move forward, kings can move in all directions.
     * 
     * @param piece The piece to get directions for
     * @return Array of [rowDelta, colDelta] direction pairs
     */
    private int[][] getMovementDirections(Piece piece) {
        if (piece.isKing()) {
            // Kings can move in all 4 diagonal directions
            return new int[][] { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        } else {
            // Men can only move forward (toward opponent's side)
            if (piece.getColor() == Color.RED) {
                return new int[][] { { -1, -1 }, { -1, 1 } }; // RED moves up (toward row 0)
            } else {
                return new int[][] { { 1, -1 }, { 1, 1 } }; // BLACK moves down (toward row 7)
            }
        }
    }

    /**
     * Gets the valid capture directions for a piece.
     * Same as movement directions since captures are diagonal jumps.
     * 
     * @param piece The piece to get capture directions for
     * @return Array of [rowDelta, colDelta] direction pairs
     */
    private int[][] getCaptureDirections(Piece piece) {
        return getMovementDirections(piece);
    }

    /**
     * Checks if a position is within the board boundaries.
     * 
     * @param row Row to check
     * @param col Column to check
     * @return true if position is valid, false otherwise
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
