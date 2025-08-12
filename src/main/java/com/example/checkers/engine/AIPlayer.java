package com.example.checkers.engine;

import com.example.checkers.model.*;

import java.util.List;

/**
 * Simple AI player that uses minimax algorithm with alpha-beta pruning.
 * Evaluates board positions and chooses the best move available.
 */
public class AIPlayer {

    /** Maximum depth to search in the game tree */
    private final int maxDepth;

    /** Rules engine for generating legal moves */
    private final RulesEngine rules = new RulesEngine();

    /**
     * Creates a new AI player with specified search depth.
     * Higher depth means stronger play but slower response.
     * 
     * @param maxDepth Maximum number of moves ahead to look (typically 3-5)
     */
    public AIPlayer(int maxDepth) {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("Search depth must be at least 1");
        }
        this.maxDepth = maxDepth;
    }

    /**
     * Chooses the best move for the AI player on the current board.
     * Uses minimax algorithm to evaluate all possible moves.
     * 
     * @param board   Current board state
     * @param myColor Color of the AI player
     * @return The best move found, or null if no legal moves exist
     */
    public Move chooseMove(Board board, Color myColor) {
        // Get all legal moves for the AI
        List<Move> legalMoves = rules.legalMoves(board, myColor);

        if (legalMoves.isEmpty()) {
            return null; // No legal moves available
        }

        // If only one move available, no need to search
        if (legalMoves.size() == 1) {
            return legalMoves.get(0);
        }

        // Find the move with the best evaluation
        double bestScore = Double.NEGATIVE_INFINITY;
        Move bestMove = legalMoves.get(0); // Default to first move

        // Evaluate each possible move
        for (Move move : legalMoves) {
            // Apply the move to get the resulting board
            Board resultingBoard = rules.apply(board, move);

            // Use minimax to evaluate the opponent's best response
            double score = minimax(resultingBoard, myColor.opponent(), myColor, 1);

            // Keep track of the best move found
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Minimax algorithm with alpha-beta pruning.
     * Recursively evaluates board positions to find the best move.
     * 
     * @param board         Current board state
     * @param currentPlayer Color of the player whose turn it is
     * @param myColor       Color of the AI player (for evaluation perspective)
     * @param depth         Current depth in the search tree
     * @return Evaluation score from the perspective of myColor
     */
    private double minimax(Board board, Color currentPlayer, Color myColor, int depth) {
        // Base case: reached maximum search depth
        if (depth >= maxDepth) {
            return evaluateBoard(board, myColor);
        }

        // Get all legal moves for the current player
        List<Move> legalMoves = rules.legalMoves(board, currentPlayer);

        // Base case: no legal moves (game over)
        if (legalMoves.isEmpty()) {
            if (currentPlayer == myColor) {
                return Double.NEGATIVE_INFINITY; // I lose
            } else {
                return Double.POSITIVE_INFINITY; // I win
            }
        }

        if (currentPlayer == myColor) {
            // My turn: maximize my score
            return maximizeScore(board, currentPlayer, myColor, depth);
        } else {
            // Opponent's turn: minimize my score
            return minimizeScore(board, currentPlayer, myColor, depth);
        }
    }

    /**
     * Maximizing part of minimax algorithm.
     * Tries to find the move that gives the highest score.
     * 
     * @param board         Current board state
     * @param currentPlayer Color of the current player
     * @param myColor       Color of the AI player
     * @param depth         Current depth in search tree
     * @return Best possible score for the maximizing player
     */
    private double maximizeScore(Board board, Color currentPlayer, Color myColor, int depth) {
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Move move : rules.legalMoves(board, currentPlayer)) {
            Board resultingBoard = rules.apply(board, move);
            double score = minimax(resultingBoard, currentPlayer.opponent(), myColor, depth + 1);
            bestScore = Math.max(bestScore, score);
        }

        return bestScore;
    }

    /**
     * Minimizing part of minimax algorithm.
     * Tries to find the move that gives the lowest score.
     * 
     * @param board         Current board state
     * @param currentPlayer Color of the current player
     * @param myColor       Color of the AI player
     * @param depth         Current depth in search tree
     * @return Best possible score for the minimizing player
     */
    private double minimizeScore(Board board, Color currentPlayer, Color myColor, int depth) {
        double bestScore = Double.POSITIVE_INFINITY;

        for (Move move : rules.legalMoves(board, currentPlayer)) {
            Board resultingBoard = rules.apply(board, move);
            double score = minimax(resultingBoard, currentPlayer.opponent(), myColor, depth + 1);
            bestScore = Math.min(bestScore, score);
        }

        return bestScore;
    }

    /**
     * Evaluates a board position from the perspective of a given player.
     * Higher positive scores favor the player, negative scores favor the opponent.
     * 
     * @param board   Board to evaluate
     * @param myColor Color of the player to evaluate for
     * @return Evaluation score (positive favors myColor, negative favors opponent)
     */
    private double evaluateBoard(Board board, Color myColor) {
        double materialScore = 0.0;
        double mobilityScore = 0.0;

        // Count material (pieces and their values)
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.get(row, col);
                if (piece != null) {
                    double pieceValue = piece.isKing() ? 2.5 : 1.0;

                    if (piece.getColor() == myColor) {
                        materialScore += pieceValue;
                    } else {
                        materialScore -= pieceValue;
                    }
                }
            }
        }

        // Add mobility bonus (number of legal moves available)
        int myMoves = rules.legalMoves(board, myColor).size();
        int opponentMoves = rules.legalMoves(board, myColor.opponent()).size();
        mobilityScore = 0.05 * (myMoves - opponentMoves);

        return materialScore + mobilityScore;
    }
}
