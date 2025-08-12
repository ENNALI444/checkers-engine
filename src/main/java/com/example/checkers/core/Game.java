package com.example.checkers.core;

import com.example.checkers.engine.AIPlayer;
import com.example.checkers.engine.RulesEngine;
import com.example.checkers.model.*;

import java.util.List;
import java.util.UUID;

/**
 * Represents a single checkers game.
 * Manages the game state, turns, and coordinates between players and AI.
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
     * Creates a new game with the specified AI difficulty.
     * 
     * @param aiDepth How many moves ahead the AI should look (3-5 is typical)
     */
    public Game(int aiDepth) {
        this.id = UUID.randomUUID().toString();
        this.rules = new RulesEngine();
        this.ai = new AIPlayer(aiDepth);
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
    }

    /**
     * Makes an AI move if it's the AI's turn.
     * 
     * @param aiColor Which color the AI is playing
     * @return The move the AI made, or null if it's not the AI's turn
     */
    public Move aiMoveIfTurn(Color aiColor) {
        // Check if game is still ongoing
        if (result != GameResult.ONGOING) {
            return null;
        }

        // Check if it's the AI's turn
        if (turn != aiColor) {
            return null;
        }

        // Get the AI's best move
        Move aiMove = ai.chooseMove(board, aiColor);

        if (aiMove != null) {
            // Apply the AI's move
            board = rules.apply(board, aiMove);

            // Switch turns
            turn = turn.opponent();

            // Check if the game has ended
            checkForGameEnd();
        }

        return aiMove;
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
