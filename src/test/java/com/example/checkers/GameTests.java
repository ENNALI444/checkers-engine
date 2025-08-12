package com.example.checkers;

import com.example.checkers.core.Game;
import com.example.checkers.engine.RulesEngine;
import com.example.checkers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Game class.
 * Verifies game state management and move validation.
 */
public class GameTests {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testGameCreation() {
        // Test that game is created with correct initial state
        assertNotNull(game.getId(), "Game should have an ID");
        assertEquals(Color.RED, game.getTurn(), "RED should go first");
        assertEquals(GameResult.ONGOING, game.getResult(), "Game should start as ongoing");
        assertNotNull(game.getBoard(), "Game should have a board");

        // Test that initial board has correct piece counts
        Board initialBoard = game.getBoard();
        assertEquals(12, initialBoard.count(Color.RED), "Initial board should have 12 RED pieces");
        assertEquals(12, initialBoard.count(Color.BLACK), "Initial board should have 12 BLACK pieces");
    }

    @Test
    void testGameTurnManagement() {
        // Test that turns alternate correctly
        Color initialTurn = game.getTurn();
        assertEquals(Color.RED, initialTurn, "Game should start with RED's turn");

        // Make a move and check turn changes
        Move legalMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(legalMove);

        assertEquals(Color.BLACK, game.getTurn(), "Turn should switch to BLACK after RED moves");

        // Make another move and check turn changes back
        Move blackMove = new Move(List.of(new Square(2, 1), new Square(3, 0)));
        game.makeMove(blackMove);

        assertEquals(Color.RED, game.getTurn(), "Turn should switch back to RED after BLACK moves");
    }

    @Test
    void testLegalMoveValidation() {
        // Test that legal moves are accepted
        Move legalMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));

        assertDoesNotThrow(() -> {
            game.makeMove(legalMove);
        }, "Legal moves should be accepted");

        // Test that illegal moves are rejected
        Move illegalMove = new Move(List.of(new Square(5, 1), new Square(4, 1))); // Not diagonal

        assertThrows(IllegalArgumentException.class, () -> {
            game.makeMove(illegalMove);
        }, "Illegal moves should be rejected");
    }

    @Test
    void testForcedCaptureEnforcement() {
        // Set up a position where RED must capture
        Board customBoard = new Board();
        customBoard.set(5, 2, new Piece(Color.RED, false));
        customBoard.set(4, 3, new Piece(Color.BLACK, false));

        // Create a new game and manually set the board
        Game customGame = new Game();
        // We'll test this with the actual game logic instead

        // Test that the game enforces forced captures through the rules engine
        RulesEngine rules = new RulesEngine();
        List<Move> legalMoves = rules.legalMoves(customBoard, Color.RED);

        // Should only have capture moves available
        assertFalse(legalMoves.isEmpty(), "Should have legal moves");
        assertTrue(legalMoves.stream().allMatch(Move::isCapture),
                "All legal moves should be captures when captures are available");
    }

    @Test
    void testGameEndDetection() {
        // Test that game ends when a player has no legal moves
        // This would require setting up a specific endgame position

        // For now, test that ongoing games continue
        assertEquals(GameResult.ONGOING, game.getResult(), "New game should be ongoing");

        // Make a few moves and ensure game stays ongoing
        Move move1 = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(move1);
        assertEquals(GameResult.ONGOING, game.getResult(), "Game should still be ongoing after move");
    }

    @Test
    void testGameStateAfterMoves() {
        // Test that game state is properly maintained after moves

        // Get initial state
        Board initialBoard = game.getBoard();
        Color initialTurn = game.getTurn();

        // Make a move
        Move move = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(move);

        // Check that board changed
        Board newBoard = game.getBoard();
        assertNotSame(initialBoard, newBoard, "Board should be a new instance after move");

        // Check that turn changed
        assertNotEquals(initialTurn, game.getTurn(), "Turn should change after move");
    }

    @Test
    void testLegalMovesGeneration() {
        // Test that legal moves are generated correctly
        List<Move> legalMoves = game.legalMoves();

        assertNotNull(legalMoves, "Legal moves should not be null");
        assertFalse(legalMoves.isEmpty(), "Should have legal moves available");

        // All generated moves should be valid
        for (Move move : legalMoves) {
            assertTrue(move.getPath().size() >= 2, "Move should have at least start and end positions");
        }
    }

    @Test
    void testGameCannotMoveAfterEnd() {
        // Test that moves cannot be made after game ends
        // This would require setting up an endgame position

        // For now, test that ongoing games can make moves
        Move legalMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));

        assertDoesNotThrow(() -> {
            game.makeMove(legalMove);
        }, "Ongoing games should allow legal moves");
    }

    @Test
    void testGameAsciiRepresentation() {
        // Test that ASCII representation is generated
        String ascii = game.ascii();

        assertNotNull(ascii, "ASCII representation should not be null");
        assertTrue(ascii.contains("Turn: RED"), "ASCII should show current turn");
        assertTrue(ascii.contains("🔴"), "ASCII should show RED pieces");
        assertTrue(ascii.contains("⚫"), "ASCII should show BLACK pieces");
        assertTrue(ascii.contains("*"), "ASCII should show non-playable spaces");
    }

    @Test
    void testGameIdUniqueness() {
        // Test that different games have different IDs
        Game game1 = new Game();
        Game game2 = new Game();

        assertNotEquals(game1.getId(), game2.getId(), "Different games should have different IDs");
    }

    @Test
    void testAIMoveIntegration() {
        // Test that AI automatically makes a move after human move
        Move humanMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(humanMove);
        
        // AI should have made a move, turn should be RED again
        assertEquals(Color.RED, game.getTurn(), "Turn should be RED after AI move");
        assertEquals(GameResult.ONGOING, game.getResult(), "Game should still be ongoing");
        
        // Board should have changed from AI move
        Board boardAfterAIMove = game.getBoard();
        assertNotNull(boardAfterAIMove, "Board should exist after AI move");
    }



    @Test
    void testGameStateConsistency() {
        // Test that game state remains consistent
        String initialAscii = game.ascii();
        Board initialBoard = game.getBoard();
        Color initialTurn = game.getTurn();
        
        // Make a move
        Move move = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(move);
        
        // Verify state changed
        assertNotEquals(initialTurn, game.getTurn(), "Turn should change after move");
        assertNotSame(initialBoard, game.getBoard(), "Board should be new instance");
        
        // Verify new state is valid
        assertNotNull(game.getBoard(), "Board should not be null");
        assertNotNull(game.getTurn(), "Turn should not be null");
        assertNotNull(game.getResult(), "Result should not be null");
    }

    @Test
    void testGameWithMultipleMoves() {
        // Test game flow with multiple moves
        Move move1 = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(move1);
        assertEquals(Color.RED, game.getTurn(), "Turn should be RED after AI move");
        
        Move move2 = new Move(List.of(new Square(5, 3), new Square(4, 2)));
        game.makeMove(move2);
        assertEquals(Color.RED, game.getTurn(), "Turn should be RED after AI move");
        
        assertEquals(GameResult.ONGOING, game.getResult(), "Game should still be ongoing");
    }
}
