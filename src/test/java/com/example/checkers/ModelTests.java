package com.example.checkers;

import com.example.checkers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the model classes.
 * Verifies Board, Piece, Move, Square, and Color functionality.
 */
public class ModelTests {

    private Board board;
    private Piece redPiece;
    private Piece blackPiece;
    private Piece redKing;
    private Piece blackKing;

    @BeforeEach
    void setUp() {
        board = new Board();
        redPiece = new Piece(Color.RED, false);
        blackPiece = new Piece(Color.BLACK, false);
        redKing = new Piece(Color.RED, true);
        blackKing = new Piece(Color.BLACK, true);
    }

    // ========== Color Tests ==========

    @Test
    void testColorOpponent() {
        assertEquals(Color.BLACK, Color.RED.opponent(), "RED's opponent should be BLACK");
        assertEquals(Color.RED, Color.BLACK.opponent(), "BLACK's opponent should be RED");
    }

    @Test
    void testColorValues() {
        assertEquals("RED", Color.RED.name(), "RED should have correct name");
        assertEquals("BLACK", Color.BLACK.name(), "BLACK should have correct name");
    }

    // ========== Square Tests ==========

    @Test
    void testSquareCreation() {
        Square square = new Square(3, 4);
        assertEquals(3, square.row(), "Row should be 3");
        assertEquals(4, square.col(), "Column should be 4");
    }

    @Test
    void testSquareIsOnBoard() {
        Square validSquare = new Square(3, 4);
        Square invalidSquare1 = new Square(-1, 4);
        Square invalidSquare2 = new Square(3, 8);
        Square invalidSquare3 = new Square(8, 4);

        assertTrue(validSquare.isOnBoard(), "Valid square should be on board");
        assertFalse(invalidSquare1.isOnBoard(), "Negative row should be invalid");
        assertFalse(invalidSquare2.isOnBoard(), "Column >= 8 should be invalid");
        assertFalse(invalidSquare3.isOnBoard(), "Row >= 8 should be invalid");
    }

    @Test
    void testSquareToString() {
        Square square = new Square(3, 4);
        assertEquals("(3,4)", square.toString(), "Square should have correct string representation");
    }

    // ========== Piece Tests ==========

    @Test
    void testPieceCreation() {
        assertEquals(Color.RED, redPiece.getColor(), "RED piece should have RED color");
        assertEquals(Color.BLACK, blackPiece.getColor(), "BLACK piece should have BLACK color");
        assertFalse(redPiece.isKing(), "New piece should not be king");
        assertFalse(blackPiece.isKing(), "New piece should not be king");
        assertTrue(redKing.isKing(), "King piece should be king");
        assertTrue(blackKing.isKing(), "King piece should be king");
    }

    @Test
    void testPieceMakeKing() {
        Piece promotedRed = redPiece.makeKing();
        Piece promotedBlack = blackPiece.makeKing();

        assertTrue(promotedRed.isKing(), "Promoted RED piece should be king");
        assertTrue(promotedBlack.isKing(), "Promoted BLACK piece should be king");
        assertEquals(Color.RED, promotedRed.getColor(), "Promoted piece should keep color");
        assertEquals(Color.BLACK, promotedBlack.getColor(), "Promoted piece should keep color");

        // Original pieces should be unchanged
        assertFalse(redPiece.isKing(), "Original piece should not be modified");
        assertFalse(blackPiece.isKing(), "Original piece should not be modified");
    }

    @Test
    void testPiecePromotionIfNeeded() {
        // RED piece moving to row 0 (back rank) should be promoted
        Piece promotedRed = redPiece.promoteIfNeeded(0);
        assertTrue(promotedRed.isKing(), "RED piece should be promoted at row 0");

        // BLACK piece moving to row 7 (back rank) should be promoted
        Piece promotedBlack = blackPiece.promoteIfNeeded(7);
        assertTrue(promotedBlack.isKing(), "BLACK piece should be promoted at row 7");

        // Pieces moving to other rows should not be promoted
        Piece notPromotedRed = redPiece.promoteIfNeeded(3);
        assertFalse(notPromotedRed.isKing(), "RED piece should not be promoted at row 3");

        Piece notPromotedBlack = blackPiece.promoteIfNeeded(4);
        assertFalse(notPromotedBlack.isKing(), "BLACK piece should not be promoted at row 4");

        // Kings should not be promoted again
        Piece kingPromoted = redKing.promoteIfNeeded(0);
        assertTrue(kingPromoted.isKing(), "King should remain king");
    }

    @Test
    void testPieceToString() {
        assertEquals("r", redPiece.toString(), "RED piece should show as 'r'");
        assertEquals("b", blackPiece.toString(), "BLACK piece should show as 'b'");
        assertEquals("rk", redKing.toString(), "RED king should show as 'rk'");
        assertEquals("bk", blackKing.toString(), "BLACK king should show as 'bk'");
    }

    // ========== Move Tests ==========

    @Test
    void testMoveCreation() {
        List<Square> path = List.of(new Square(5, 1), new Square(4, 0));
        Move move = new Move(path);

        assertEquals(2, move.getPath().size(), "Move should have 2 positions");
        assertEquals(new Square(5, 1), move.from(), "From should be first position");
        assertEquals(new Square(4, 0), move.to(), "To should be last position");
    }

    @Test
    void testMoveCreationWithInvalidPath() {
        // Empty path should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            new Move(List.of());
        }, "Move with empty path should throw exception");

        // Single position should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            new Move(List.of(new Square(5, 1)));
        }, "Move with single position should throw exception");
    }

    @Test
    void testMoveIsCapture() {
        // Simple move (distance 1)
        Move simpleMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        assertFalse(simpleMove.isCapture(), "Simple move should not be capture");

        // Capture move (distance 2)
        Move captureMove = new Move(List.of(new Square(5, 2), new Square(3, 4)));
        assertTrue(captureMove.isCapture(), "Jump move should be capture");

        // Multi-jump move
        Move multiJump = new Move(List.of(new Square(5, 2), new Square(3, 4), new Square(1, 6)));
        assertTrue(multiJump.isCapture(), "Multi-jump should be capture");
    }

    @Test
    void testMoveToString() {
        // Simple move
        Move simpleMove = new Move(List.of(new Square(5, 1), new Square(4, 0)));
        assertEquals("(5,1) - (4,0)", simpleMove.toString(), "Simple move should show with dash");

        // Capture move
        Move captureMove = new Move(List.of(new Square(5, 2), new Square(3, 4)));
        assertEquals("(5,2) x (3,4)", captureMove.toString(), "Capture move should show with x");

        // Multi-jump
        Move multiJump = new Move(List.of(new Square(5, 2), new Square(3, 4), new Square(1, 6)));
        assertEquals("(5,2) x (3,4) x (1,6)", multiJump.toString(), "Multi-jump should show with x's");
    }

    @Test
    void testMoveOf() {
        Move move = Move.of(new Square(5, 1), new Square(4, 0));
        assertEquals(2, move.getPath().size(), "Move.of should create move with correct path");
        assertEquals(new Square(5, 1), move.from(), "Move.of should set correct from");
        assertEquals(new Square(4, 0), move.to(), "Move.of should set correct to");
    }

    // ========== Board Tests ==========

    @Test
    void testBoardInitialSetup() {
        Board initialBoard = Board.initial();

        // Check piece counts
        assertEquals(12, initialBoard.count(Color.RED), "Initial board should have 12 RED pieces");
        assertEquals(12, initialBoard.count(Color.BLACK), "Initial board should have 12 BLACK pieces");

        // Check piece placement (BLACK at top, RED at bottom)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    Piece piece = initialBoard.get(row, col);
                    assertNotNull(piece, "Dark square at (" + row + "," + col + ") should have piece");
                    assertEquals(Color.BLACK, piece.getColor(), "Top rows should have BLACK pieces");
                }
            }
        }

        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    Piece piece = initialBoard.get(row, col);
                    assertNotNull(piece, "Dark square at (" + row + "," + col + ") should have piece");
                    assertEquals(Color.RED, piece.getColor(), "Bottom rows should have RED pieces");
                }
            }
        }
    }

    @Test
    void testBoardGetSet() {
        board.set(3, 4, redPiece);
        Piece retrieved = board.get(3, 4);

        assertEquals(redPiece, retrieved, "Retrieved piece should match set piece");
        assertNull(board.get(3, 5), "Unset position should be null");
    }

    @Test
    void testBoardCopy() {
        // Set up a board with pieces
        board.set(3, 4, redPiece);
        board.set(4, 5, blackPiece);

        Board copy = board.copy();

        // Check that copy has same pieces
        assertEquals(redPiece.getColor(), copy.get(3, 4).getColor(), "Copy should have same pieces");
        assertEquals(blackPiece.getColor(), copy.get(4, 5).getColor(), "Copy should have same pieces");

        // Modify copy
        copy.set(3, 4, blackPiece);

        // Original should be unchanged
        assertEquals(Color.RED, board.get(3, 4).getColor(), "Original should be unchanged");
        assertEquals(Color.BLACK, copy.get(3, 4).getColor(), "Copy should be modified");
    }

    @Test
    void testBoardCount() {
        // Empty board should have 0 pieces
        assertEquals(0, board.count(Color.RED), "Empty board should have 0 RED pieces");
        assertEquals(0, board.count(Color.BLACK), "Empty board should have 0 BLACK pieces");

        // Add some pieces
        board.set(3, 4, redPiece);
        board.set(4, 5, redPiece);
        board.set(5, 6, blackPiece);

        assertEquals(2, board.count(Color.RED), "Board should have 2 RED pieces");
        assertEquals(1, board.count(Color.BLACK), "Board should have 1 BLACK piece");
    }

    @Test
    void testBoardToString() {
        // Set up a simple board
        board.set(3, 4, redPiece);
        board.set(4, 5, blackPiece);

        String boardStr = board.toString();

        assertNotNull(boardStr, "Board string should not be null");
        assertTrue(boardStr.contains("."), "Board should show empty squares as dots");
        assertTrue(boardStr.contains("r"), "Board should show RED pieces");
        assertTrue(boardStr.contains("b"), "Board should show BLACK pieces");
        assertTrue(boardStr.contains("\n"), "Board should have line breaks between rows");
    }

    @Test
    void testBoardToArray() {
        board.set(3, 4, redPiece);
        Piece[][] array = board.toArray();

        assertEquals(8, array.length, "Array should have 8 rows");
        assertEquals(8, array[0].length, "Array should have 8 columns");
        assertEquals(redPiece, array[3][4], "Array should contain set piece");
        assertNull(array[3][5], "Array should have null for unset positions");
    }
}
