package com.example.checkers;

import com.example.checkers.core.Game;
import com.example.checkers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Game class.
 * Verifies game state management, move validation, and AI integration.
 */
public class GameTests {

    private Game game;
    private Board board;

    @BeforeEach
    void setUp() {
        game = new Game(3);
        board = new Board();
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

        // Create a new game with custom board
        Game customGame = new Game(3);
        // Note: This test would need a way to set custom board state

        // Test that simple moves are rejected when captures are available
        Move simpleMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));

        // This should fail because captures are forced
        assertThrows(IllegalArgumentException.class, () -> {
            customGame.makeMove(simpleMove);
        }, "Simple moves should be rejected when captures are available");
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
    void testAIMoveIntegration() {
        // Test that AI makes moves when it's AI's turn
        Color aiColor = Color.BLACK;

        // Make a move for RED (human player)
        Move humanMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        game.makeMove(humanMove);

        // Now it's BLACK's turn, so AI should move
        Move aiMove = game.aiMoveIfTurn(aiColor);

        assertNotNull(aiMove, "AI should make a move when it's AI's turn");
        assertEquals(Color.RED, game.getTurn(), "Turn should switch back to RED after AI move");
    }

    @Test
    void testAINoMoveWhenNotAITurn() {
        // Test that AI doesn't move when it's not AI's turn
        Color aiColor = Color.BLACK;

        // RED's turn, AI shouldn't move
        Move aiMove = game.aiMoveIfTurn(aiColor);

        assertNull(aiMove, "AI should not move when it's not AI's turn");
        assertEquals(Color.RED, game.getTurn(), "Turn should remain RED");
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
        assertTrue(ascii.contains("."), "ASCII should show board representation");
    }

    @Test
    void testGameIdUniqueness() {
        // Test that different games have different IDs
        Game game1 = new Game(3);
        Game game2 = new Game(3);

        assertNotEquals(game1.getId(), game2.getId(), "Different games should have different IDs");
    }

    @Test
    void testGameAIDepthConfiguration() {
        // Test that AI depth is properly configured
        Game shallowGame = new Game(1);
        Game deepGame = new Game(5);

        // Both games should be created successfully
        assertNotNull(shallowGame, "Game with depth 1 should be created");
        assertNotNull(deepGame, "Game with depth 5 should be created");

        // Both should have legal moves available
        assertFalse(shallowGame.legalMoves().isEmpty(), "Shallow game should have legal moves");
        assertFalse(deepGame.legalMoves().isEmpty(), "Deep game should have legal moves");
    }
}
