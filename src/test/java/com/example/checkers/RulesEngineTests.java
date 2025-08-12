package com.example.checkers;

import com.example.checkers.engine.AIPlayer;
import com.example.checkers.engine.RulesEngine;
import com.example.checkers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the RulesEngine class.
 * Tests move generation, validation, and application.
 */
public class RulesEngineTests {

    private RulesEngine rules;
    private Board board;

    @BeforeEach
    void setUp() {
        rules = new RulesEngine();
        board = new Board();
    }

    @Test
    void testInitialBoardSetup() {
        // Test that initial board has correct piece counts
        Board initialBoard = Board.initial();
        assertEquals(12, initialBoard.count(Color.RED), "RED should have 12 pieces");
        assertEquals(12, initialBoard.count(Color.BLACK), "BLACK should have 12 pieces");

        // Test that pieces are on correct squares (dark squares only)
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 0) {
                    // Light squares should be empty
                    assertNull(initialBoard.get(row, col),
                            "Light square at (" + row + "," + col + ") should be empty");
                } else {
                    // Dark squares should have pieces
                    assertNotNull(initialBoard.get(row, col),
                            "Dark square at (" + row + "," + col + ") should have a piece");
                }
            }
        }
    }

    @Test
    void testSimpleMoveGeneration() {
        // Set up a simple position: RED piece at (5,1) with empty space at (4,0)
        board.set(5, 1, new Piece(Color.RED, false));

        List<Move> moves = rules.legalMoves(board, Color.RED);

        // Should have 2 legal moves: (5,1) -> (4,0) and (5,1) -> (4,2)
        assertEquals(2, moves.size(), "RED should have 2 legal moves");

        // Check that both moves are valid
        boolean hasMoveTo40 = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(4, 0)));
        boolean hasMoveTo42 = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(4, 2)));

        assertTrue(hasMoveTo40, "Should be able to move to (4,0)");
        assertTrue(hasMoveTo42, "Should be able to move to (4,2)");
    }

    @Test
    void testForcedCaptureRule() {
        // Set up a position where RED must capture
        board.set(5, 2, new Piece(Color.RED, false)); // RED piece
        board.set(4, 3, new Piece(Color.BLACK, false)); // BLACK piece to capture

        List<Move> moves = rules.legalMoves(board, Color.RED);

        // Should only have capture moves, no simple moves
        assertFalse(moves.isEmpty(), "Should have legal moves");
        assertTrue(moves.stream().allMatch(Move::isCapture),
                "All moves should be captures when captures are available");

        // Should be able to capture to (3,4)
        boolean canCapture = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(3, 4)));
        assertTrue(canCapture, "Should be able to capture to (3,4)");
    }

    @Test
    void testMultiJumpCapture() {
        // Set up a position where RED can make a multi-jump
        board.set(5, 2, new Piece(Color.RED, false)); // RED piece
        board.set(4, 3, new Piece(Color.BLACK, false)); // First BLACK piece
        board.set(2, 5, new Piece(Color.BLACK, false)); // Second BLACK piece

        List<Move> moves = rules.legalMoves(board, Color.RED);

        // Should have at least one multi-jump move
        boolean hasMultiJump = moves.stream()
                .anyMatch(m -> m.getPath().size() >= 3);
        assertTrue(hasMultiJump, "Should have multi-jump moves available");

        // Check that the multi-jump path is valid: (5,2) -> (3,4) -> (1,6)
        boolean hasCorrectPath = moves.stream()
                .anyMatch(m -> m.getPath().size() == 3 &&
                        m.getPath().get(0).equals(new Square(5, 2)) &&
                        m.getPath().get(1).equals(new Square(3, 4)) &&
                        m.getPath().get(2).equals(new Square(1, 6)));
        assertTrue(hasCorrectPath, "Should have correct multi-jump path");
    }

    @Test
    void testKingPromotion() {
        // Set up a RED piece that can move to the back rank
        board.set(1, 2, new Piece(Color.RED, false));

        // Move to back rank (0,1)
        Move promotionMove = new Move(List.of(new Square(1, 2), new Square(0, 1)));
        Board newBoard = rules.apply(board, promotionMove);

        // Check that the piece became a king
        Piece promotedPiece = newBoard.get(0, 1);
        assertNotNull(promotedPiece, "Piece should be at new position");
        assertTrue(promotedPiece.isKing(), "Piece should be promoted to king");
        assertEquals(Color.RED, promotedPiece.getColor(), "Piece should remain RED");
    }

    @Test
    void testKingMovement() {
        // Set up a RED king in the middle of the board
        board.set(4, 4, new Piece(Color.RED, true));

        List<Move> moves = rules.legalMoves(board, Color.RED);

        // King should be able to move in all 4 diagonal directions
        assertEquals(4, moves.size(), "King should have 4 possible moves");

        // Check all directions: up-left, up-right, down-left, down-right
        boolean canMoveUpLeft = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(3, 3)));
        boolean canMoveUpRight = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(3, 5)));
        boolean canMoveDownLeft = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(5, 3)));
        boolean canMoveDownRight = moves.stream()
                .anyMatch(m -> m.getPath().get(1).equals(new Square(5, 5)));

        assertTrue(canMoveUpLeft, "King should move up-left");
        assertTrue(canMoveUpRight, "King should move up-right");
        assertTrue(canMoveDownLeft, "King should move down-left");
        assertTrue(canMoveDownRight, "King should move down-right");
    }

    @Test
    void testMoveApplication() {
        // Set up a simple capture position
        board.set(5, 2, new Piece(Color.RED, false));
        board.set(4, 3, new Piece(Color.BLACK, false));

        // Apply a capture move
        Move captureMove = new Move(List.of(new Square(5, 2), new Square(3, 4)));
        Board newBoard = rules.apply(board, captureMove);

        // Check that pieces are in correct positions
        assertNull(newBoard.get(5, 2), "Starting position should be empty");
        assertNull(newBoard.get(4, 3), "Captured piece should be removed");
        assertNotNull(newBoard.get(3, 4), "Piece should be at landing position");

        // Check that the piece is still RED and not a king
        Piece movedPiece = newBoard.get(3, 4);
        assertEquals(Color.RED, movedPiece.getColor());
        assertFalse(movedPiece.isKing(), "Piece should not be king yet");
    }

    @Test
    void testNoLegalMovesGameEnd() {
        // Set up a position where RED has no legal moves
        board.set(0, 1, new Piece(Color.RED, false)); // RED piece at edge
        board.set(1, 0, new Piece(Color.BLACK, false)); // BLACK piece blocking

        List<Move> moves = rules.legalMoves(board, Color.RED);

        // RED should have no legal moves
        assertTrue(moves.isEmpty(), "RED should have no legal moves");
    }

    @Test
    void testAIPlayerChoosesLegalMove() {
        // Test that AI always chooses a legal move
        Board initialBoard = Board.initial();
        AIPlayer ai = new AIPlayer(3);

        Move aiMove = ai.chooseMove(initialBoard, Color.RED);

        // AI should find a move
        assertNotNull(aiMove, "AI should find a move");

        // The move should be legal
        List<Move> legalMoves = rules.legalMoves(initialBoard, Color.RED);
        boolean isLegal = legalMoves.stream()
                .anyMatch(m -> m.getPath().equals(aiMove.getPath()));

        assertTrue(isLegal, "AI should choose a legal move");
    }

    @Test
    void testBoardCopying() {
        // Test that board.copy() creates a deep copy
        board.set(5, 1, new Piece(Color.RED, false));
        Board copy = board.copy();

        // Modify the copy
        copy.set(5, 1, new Piece(Color.BLACK, false));

        // Original should be unchanged
        Piece originalPiece = board.get(5, 1);
        Piece copyPiece = copy.get(5, 1);

        assertEquals(Color.RED, originalPiece.getColor(), "Original should be unchanged");
        assertEquals(Color.BLACK, copyPiece.getColor(), "Copy should be modified");
    }

    @Test
    void testInvalidMoveRejection() {
        // Set up a simple position
        board.set(5, 1, new Piece(Color.RED, false));

        // Try to make an invalid move (not diagonal)
        Move invalidMove = new Move(List.of(new Square(5, 1), new Square(4, 1)));

        // This should throw an exception or be rejected
        assertThrows(IllegalArgumentException.class, () -> {
            rules.apply(board, invalidMove);
        }, "Invalid moves should be rejected");
    }
}
