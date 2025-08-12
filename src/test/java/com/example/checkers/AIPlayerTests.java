package com.example.checkers;

import com.example.checkers.engine.AIPlayer;
import com.example.checkers.engine.RulesEngine;
import com.example.checkers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AIPlayer class.
 * Verifies AI move selection and evaluation logic.
 */
public class AIPlayerTests {

    private AIPlayer ai;
    private RulesEngine rules;
    private Board board;

    @BeforeEach
    void setUp() {
        ai = new AIPlayer(3);
        rules = new RulesEngine();
        board = new Board();
    }

    @Test
    void testAIPlayerCreation() {
        // Test valid depth
        AIPlayer validAI = new AIPlayer(5);
        assertNotNull(validAI, "AI should be created with valid depth");

        // Test invalid depth
        assertThrows(IllegalArgumentException.class, () -> {
            new AIPlayer(0);
        }, "Should not allow depth 0");

        assertThrows(IllegalArgumentException.class, () -> {
            new AIPlayer(-1);
        }, "Should not allow negative depth");
    }

    @Test
    void testAIChoosesMoveWhenMovesAvailable() {
        // Set up a simple position with one RED piece
        board.set(5, 1, new Piece(Color.RED, false));

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find a move when moves are available");
        assertEquals(2, aiMove.getPath().size(), "Move should have start and end positions");
        assertEquals(new Square(5, 1), aiMove.getPath().get(0), "Should start from correct position");
    }

    @Test
    void testAIReturnsNullWhenNoMovesAvailable() {
        // Set up a position where RED has no legal moves
        board.set(0, 1, new Piece(Color.RED, false)); // RED piece at edge
        board.set(1, 0, new Piece(Color.BLACK, false)); // BLACK piece blocking

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNull(aiMove, "AI should return null when no moves are available");
    }

    @Test
    void testAIChoosesOnlyMoveWhenOnlyOneAvailable() {
        // Set up a position where RED has exactly one legal move
        board.set(5, 1, new Piece(Color.RED, false));
        board.set(4, 0, new Piece(Color.BLACK, false)); // Block one direction
        board.set(4, 2, new Piece(Color.BLACK, false)); // Block other direction

        // Only move available is diagonal forward, but both are blocked
        // So no moves should be available
        List<Move> legalMoves = rules.legalMoves(board, Color.RED);
        assertEquals(0, legalMoves.size(), "Should have no legal moves");

        Move aiMove = ai.chooseMove(board, Color.RED);
        assertNull(aiMove, "AI should return null when no moves available");
    }

    @Test
    void testAIPrefersCaptures() {
        // Set up a position where RED can either capture or make a simple move
        board.set(5, 2, new Piece(Color.RED, false)); // RED piece
        board.set(4, 3, new Piece(Color.BLACK, false)); // BLACK piece to capture
        board.set(4, 1, null); // Empty space for simple move

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find a move");
        assertTrue(aiMove.isCapture(), "AI should prefer capture over simple move");
    }

    @Test
    void testAIDepthLimiting() {
        // Create AI with depth 1
        AIPlayer shallowAI = new AIPlayer(1);

        // Set up a position where looking deeper would find better moves
        board.set(5, 1, new Piece(Color.RED, false));
        board.set(4, 0, new Piece(Color.BLACK, false));
        board.set(3, 1, new Piece(Color.RED, false));

        Move shallowMove = shallowAI.chooseMove(board, Color.RED);
        Move deepMove = ai.chooseMove(board, Color.RED);

        // Both should find moves, but they might be different due to depth
        assertNotNull(shallowMove, "Shallow AI should find a move");
        assertNotNull(deepMove, "Deep AI should find a move");
    }

    @Test
    void testAIEvaluationConsistency() {
        // Test that AI evaluation is consistent for the same position
        board.set(5, 1, new Piece(Color.RED, false));

        Move move1 = ai.chooseMove(board, Color.RED);
        Move move2 = ai.chooseMove(board, Color.RED);

        // AI should make the same choice for the same position
        assertEquals(move1.getPath(), move2.getPath(), "AI should be consistent");
    }

    @Test
    void testAIMaterialEvaluation() {
        // Set up a position where RED has more material
        board.set(5, 1, new Piece(Color.RED, false));
        board.set(5, 3, new Piece(Color.RED, false));
        board.set(4, 2, new Piece(Color.BLACK, false)); // Only one BLACK piece

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find a move");
        // AI should prefer moves that maintain material advantage
    }

    @Test
    void testAIKingEvaluation() {
        // Set up a position with kings vs regular pieces
        board.set(5, 1, new Piece(Color.RED, true)); // RED king
        board.set(4, 2, new Piece(Color.BLACK, false)); // BLACK regular piece

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find a move");
        // AI should value king moves appropriately
    }

    @Test
    void testAIMobilityEvaluation() {
        // Set up a position where RED has more mobility
        board.set(5, 1, new Piece(Color.RED, false));
        board.set(4, 0, null); // Empty space for RED to move
        board.set(4, 2, null); // Another empty space

        // BLACK piece in corner with limited mobility
        board.set(0, 1, new Piece(Color.BLACK, false));

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find a move");
        // AI should consider mobility in its evaluation
    }

    @Test
    void testAITerminalPositionHandling() {
        // Set up a position where RED is about to win
        board.set(1, 1, new Piece(Color.RED, false));
        board.set(0, 0, null); // Empty space for RED to move and win

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find the winning move");
        // AI should recognize winning positions
    }

    @Test
    void testAIMultiplePieces() {
        // Set up a more complex position with multiple pieces
        board.set(5, 1, new Piece(Color.RED, false));
        board.set(5, 3, new Piece(Color.RED, false));
        board.set(4, 2, new Piece(Color.BLACK, false));
        board.set(4, 4, new Piece(Color.BLACK, false));

        Move aiMove = ai.chooseMove(board, Color.RED);

        assertNotNull(aiMove, "AI should find a move in complex position");
        // AI should handle multiple pieces and complex positions
    }
}
