package com.example.checkers.engine;

import com.example.checkers.model.*;

import java.util.List;

/**
 * Simple AI player that prioritizes captures and makes safe moves.
 * Perfect for interviews - demonstrates game logic and AI decision-making.
 */
public class AIPlayer {

    /** Rules engine for generating legal moves */
    private final RulesEngine rules = new RulesEngine();

    /**
     * Creates a new AI player with simple strategy.
     * No complex search depth needed for this approach.
     */
    public AIPlayer() {
        // Simple AI doesn't need search depth
    }

    /**
     * Chooses the best move for the AI player using simple strategy.
     * Priority 1: Capture moves (highest priority)
     * Priority 2: Safe moves that don't lead to immediate capture
     * Priority 3: Any other legal move
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

        // If only one move available, no need to evaluate
        if (legalMoves.size() == 1) {
            return legalMoves.get(0);
        }

        // Priority 1: Look for capture moves (highest priority)
        List<Move> captureMoves = legalMoves.stream()
                .filter(Move::isCapture)
                .toList();

        if (!captureMoves.isEmpty()) {
            // Choose the capture move that captures the most pieces
            return chooseBestCapture(captureMoves);
        }

        // Priority 2: Look for safe moves (avoid moves that lead to capture)
        List<Move> safeMoves = findSafeMoves(board, legalMoves, myColor);

        if (!safeMoves.isEmpty()) {
            // Choose the safest move (prefer moves that protect pieces)
            return chooseSafestMove(safeMoves, board, myColor);
        }

        // Priority 3: If no safe moves, choose any legal move
        // Prefer moves that don't expose pieces to immediate capture
        return chooseLeastRiskyMove(legalMoves, board, myColor);
    }

    /**
     * Chooses the best capture move by prioritizing moves that capture more pieces.
     * 
     * @param captureMoves List of available capture moves
     * @return The best capture move (captures most pieces)
     */
    private Move chooseBestCapture(List<Move> captureMoves) {
        Move bestCapture = captureMoves.get(0);
        int maxCaptures = countCaptures(bestCapture);

        for (Move move : captureMoves) {
            int captures = countCaptures(move);
            if (captures > maxCaptures) {
                maxCaptures = captures;
                bestCapture = move;
            }
        }

        return bestCapture;
    }

    /**
     * Counts how many pieces are captured in a move.
     * 
     * @param move The move to analyze
     * @return Number of pieces captured
     */
    private int countCaptures(Move move) {
        return move.getPath().size() - 1; // Each step in path captures one piece
    }

    /**
     * Finds moves that don't immediately lead to the AI being captured.
     * 
     * @param board      Current board state
     * @param legalMoves All legal moves available
     * @param myColor    AI player's color
     * @return List of safe moves
     */
    private List<Move> findSafeMoves(Board board, List<Move> legalMoves, Color myColor) {
        return legalMoves.stream()
                .filter(move -> !leadsToCapture(board, move, myColor))
                .toList();
    }

    /**
     * Checks if a move leads to the AI being captured on the next turn.
     * 
     * @param board   Current board state
     * @param move    Move to check
     * @param myColor AI player's color
     * @return True if the move leads to capture
     */
    private boolean leadsToCapture(Board board, Move move, Color myColor) {
        // Apply the move to get the resulting board
        Board resultingBoard = rules.apply(board, move);

        // Check if opponent can capture any of our pieces
        List<Move> opponentMoves = rules.legalMoves(resultingBoard, myColor.opponent());

        // Look for any capture moves by opponent
        return opponentMoves.stream().anyMatch(Move::isCapture);
    }

    /**
     * Chooses the safest move from a list of safe moves.
     * Prefers moves that protect pieces and maintain good position.
     * 
     * @param safeMoves List of safe moves
     * @param board     Current board state
     * @param myColor   AI player's color
     * @return The safest move
     */
    private Move chooseSafestMove(List<Move> safeMoves, Board board, Color myColor) {
        Move safestMove = safeMoves.get(0);
        double bestScore = evaluateMoveSafety(safestMove, board, myColor);

        for (Move move : safeMoves) {
            double score = evaluateMoveSafety(move, board, myColor);
            if (score > bestScore) {
                bestScore = score;
                safestMove = move;
            }
        }

        return safestMove;
    }

    /**
     * Evaluates how safe a move is based on piece protection and position.
     * 
     * @param move    Move to evaluate
     * @param board   Current board state
     * @param myColor AI player's color
     * @return Safety score (higher is safer)
     */
    private double evaluateMoveSafety(Move move, Board board, Color myColor) {
        // Apply the move
        Board resultingBoard = rules.apply(board, move);

        // Get the destination square
        Square destination = move.getPath().get(move.getPath().size() - 1);

        // Prefer moves that:
        // 1. Keep pieces near the back row (safer)
        // 2. Don't expose pieces to multiple attack directions
        // 3. Maintain piece connectivity

        double safetyScore = 0.0;

        // Bonus for staying near back row (safer position)
        if (myColor == Color.BLACK) {
            safetyScore += (7 - destination.row()) * 0.1; // Prefer top rows
        } else {
            safetyScore += destination.row() * 0.1; // Prefer bottom rows
        }

        // Bonus for center position (more mobility)
        if (destination.col() >= 2 && destination.col() <= 5) {
            safetyScore += 0.05;
        }

        return safetyScore;
    }

    /**
     * Chooses the least risky move when no safe moves are available.
     * Tries to minimize exposure to capture.
     * 
     * @param legalMoves All legal moves
     * @param board      Current board state
     * @param myColor    AI player's color
     * @return The least risky move
     */
    private Move chooseLeastRiskyMove(List<Move> legalMoves, Board board, Color myColor) {
        Move leastRisky = legalMoves.get(0);
        double lowestRisk = evaluateMoveRisk(leastRisky, board, myColor);

        for (Move move : legalMoves) {
            double risk = evaluateMoveRisk(move, board, myColor);
            if (risk < lowestRisk) {
                lowestRisk = risk;
                leastRisky = move;
            }
        }

        return leastRisky;
    }

    /**
     * Evaluates the risk level of a move.
     * 
     * @param move    Move to evaluate
     * @param board   Current board state
     * @param myColor AI player's color
     * @return Risk score (lower is less risky)
     */
    private double evaluateMoveRisk(Move move, Board board, Color myColor) {
        // Apply the move
        Board resultingBoard = rules.apply(board, move);

        // Count how many of our pieces could be captured next turn
        List<Move> opponentMoves = rules.legalMoves(resultingBoard, myColor.opponent());
        int captureThreats = 0;

        for (Move opponentMove : opponentMoves) {
            if (opponentMove.isCapture()) {
                captureThreats++;
            }
        }

        return captureThreats;
    }
}
