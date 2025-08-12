package com.example.checkers.core;

import com.example.checkers.engine.RulesEngine;
import com.example.checkers.engine.AIPlayer;
import com.example.checkers.model.*;

import java.util.List;
import java.util.UUID;

/**
 * Represents a single checkers game.
 * Manages the game state and turns without AI opponent.
 */
public class Game {

    /** Unique identifier for this game */
    private final String id;

    /** Rules engine for validating moves and generating legal moves */
    private final RulesEngine rules;

    /** AI player for computer opponent */
    private final AIPlayer ai;

    /** Current board state */
    private Board board;

    /** Whose turn it is to move */
    private Color turn;

    /** Current game result */
    private GameResult result;

    /**
     * Creates a new game with AI opponent.
     */
    public Game() {
        this.id = UUID.randomUUID().toString();
        this.rules = new RulesEngine();
        this.ai = new AIPlayer();
        this.board = Board.initial();
        this.turn = Color.RED; // RED always goes first in checkers
        this.result = GameResult.ONGOING;
    }

    /**
     * Gets the unique identifier for this game.
     * 
     * @return Game ID string
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current board state.
     * 
     * @return Current board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Gets whose turn it is to move.
     * 
     * @return Color of the player whose turn it is
     */
    public Color getTurn() {
        return turn;
    }

    /**
     * Gets the current game result.
     * 
     * @return Current game status
     */
    public GameResult getResult() {
        return result;
    }

    /**
     * Applies a player move to the board.
     * Validates the move and updates the game state.
     * 
     * @param move The move to make
     * @throws IllegalArgumentException if the move is illegal
     * @throws IllegalStateException    if the game is already finished
     */
    public void makeMove(Move move) {
        // Check if game is still ongoing
        if (result != GameResult.ONGOING) {
            throw new IllegalStateException("Cannot make move: game is already finished");
        }

        // Get all legal moves for the current player
        List<Move> legalMoves = rules.legalMoves(board, turn);

        // Check if the requested move is legal
        boolean isLegal = legalMoves.stream()
                .anyMatch(legalMove -> legalMove.getPath().equals(move.getPath()));

        if (!isLegal) {
            throw new IllegalArgumentException("Illegal move. Must be one of the legal moves.");
        }

        // Apply the move to the board
        board = rules.apply(board, move);

        // Switch turns
        turn = turn.opponent();

        // Check if the game has ended
        checkForGameEnd();

        // If it's now AI's turn and game is still ongoing, make AI move
        if (result == GameResult.ONGOING && turn == Color.BLACK) {
            makeAIMove();
        }
    }

    /**
     * Gets all legal moves for the current player.
     * 
     * @return List of legal moves
     */
    public List<Move> legalMoves() {
        return rules.legalMoves(board, turn);
    }

    /**
     * Gets a text representation of the current board state.
     * 
     * @return String showing the board and current turn
     */
    public String ascii() {
        return "Turn: " + turn + "\n" + board.toString();
    }

    /**
     * Makes an AI move when it's the computer's turn.
     * AI automatically plays for BLACK pieces.
     */
    private void makeAIMove() {
        if (turn != Color.BLACK || result != GameResult.ONGOING) {
            return; // Not AI's turn or game is over
        }

        // Get AI's move choice
        Move aiMove = ai.chooseMove(board, Color.BLACK);

        if (aiMove != null) {
            // Apply the AI move
            board = rules.apply(board, aiMove);

            // Switch turns back to human player
            turn = Color.RED;

            // Check if game ended after AI move
            checkForGameEnd();
        }
    }

    /**
     * Checks if the game has ended and updates the result.
     * Game ends when a player has no legal moves.
     */
    private void checkForGameEnd() {
        List<Move> legalMoves = rules.legalMoves(board, turn);

        if (legalMoves.isEmpty()) {
            // Current player has no moves, so they lose
            if (turn == Color.RED) {
                result = GameResult.BLACK_WIN;
            } else {
                result = GameResult.RED_WIN;
            }
        }
    }
}
