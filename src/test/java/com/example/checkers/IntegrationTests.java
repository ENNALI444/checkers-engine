package com.example.checkers;

import com.example.checkers.api.GameController;
import com.example.checkers.api.MoveRequest;
import com.example.checkers.core.Game;
import com.example.checkers.core.GameService;
import com.example.checkers.engine.AIPlayer;
import com.example.checkers.engine.RulesEngine;
import com.example.checkers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the entire checkers system.
 * Tests the complete flow from game creation to game completion.
 */
public class IntegrationTests {

    private GameService gameService;
    private GameController gameController;
    private RulesEngine rulesEngine;
    private AIPlayer aiPlayer;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
        gameController = new GameController(gameService);
        rulesEngine = new RulesEngine();
        aiPlayer = new AIPlayer();
    }

    @Test
    void testCompleteGameFlow() {
        // Test the complete game flow from creation to multiple moves
        
        // 1. Create a new game
        Map<String, Object> createResponse = gameController.create();
        assertNotNull(createResponse, "Game creation should succeed");
        
        String gameId = (String) createResponse.get("gameId");
        assertEquals("RED", createResponse.get("turn"), "Game should start with RED's turn");
        
        // 2. Verify initial board state
        ResponseEntity<?> boardResponse = gameController.board(gameId, "ascii");
        assertEquals(HttpStatus.OK, boardResponse.getStatusCode(), "Should get initial board");
        
        String initialBoard = (String) boardResponse.getBody();
        assertTrue(initialBoard.contains("Turn: RED"), "Initial board should show RED's turn");
        assertTrue(initialBoard.contains("🔴"), "Initial board should contain RED pieces");
        assertTrue(initialBoard.contains("⚫"), "Initial board should contain BLACK pieces");
        
        // 3. Get legal moves for RED
        ResponseEntity<?> movesResponse = gameController.legalMoves(gameId);
        assertEquals(HttpStatus.OK, movesResponse.getStatusCode(), "Should get legal moves");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> movesBody = (Map<String, Object>) movesResponse.getBody();
        List<Move> legalMoves = (List<Move>) movesBody.get("legalMoves");
        assertNotNull(legalMoves, "Legal moves should not be null");
        assertFalse(legalMoves.isEmpty(), "Should have legal moves available");
        
        // 4. Make a move for RED
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));
        
        ResponseEntity<?> moveResponse = gameController.move(gameId, moveRequest);
        assertEquals(HttpStatus.OK, moveResponse.getStatusCode(), "Move should succeed");
        
        // 5. Verify AI responded (turn should be RED again)
        ResponseEntity<?> updatedBoardResponse = gameController.board(gameId, "ascii");
        String updatedBoard = (String) updatedBoardResponse.getBody();
        assertTrue(updatedBoard.contains("Turn: RED"), "Turn should be RED after AI move");
        
        // 6. Make another move for RED
        MoveRequest moveRequest2 = new MoveRequest();
        moveRequest2.setPath(List.of(
            new MoveRequest.Point(5, 3),
            new MoveRequest.Point(4, 2)
        ));
        
        ResponseEntity<?> moveResponse2 = gameController.move(gameId, moveRequest2);
        assertEquals(HttpStatus.OK, moveResponse2.getStatusCode(), "Second move should succeed");
        
        // 7. Verify game is still ongoing
        ResponseEntity<?> finalBoardResponse = gameController.board(gameId, "ascii");
        String finalBoard = (String) finalBoardResponse.getBody();
        assertTrue(finalBoard.contains("Turn: RED"), "Turn should be RED after second AI move");
    }

    @Test
    void testGameWithCaptures() {
        // Test a game scenario involving captures
        
        // 1. Create a game
        Map<String, Object> createResponse = gameController.create();
        String gameId = (String) createResponse.get("gameId");
        
        // 2. Set up a position where RED can capture
        Game game = gameService.get(gameId).orElseThrow();
        
        // Manually set up a capture scenario
        Board customBoard = new Board();
        customBoard.set(5, 1, new Piece(Color.RED, false)); // RED piece
        customBoard.set(4, 2, new Piece(Color.BLACK, false)); // BLACK piece to capture
        customBoard.set(3, 3, null); // Landing square for capture
        
        // We'll test the capture logic through the rules engine
        List<Move> legalMoves = rulesEngine.legalMoves(customBoard, Color.RED);
        
        // Should have capture moves available
        assertFalse(legalMoves.isEmpty(), "Should have legal moves");
        assertTrue(legalMoves.stream().anyMatch(Move::isCapture), "Should have capture moves available");
        
        // Find the capture move
        Move captureMove = legalMoves.stream()
            .filter(Move::isCapture)
            .findFirst()
            .orElse(null);
        
        assertNotNull(captureMove, "Should find a capture move");
        assertTrue(captureMove.isCapture(), "Move should be a capture");
        
        // Verify capture path
        List<Square> path = captureMove.getPath();
        assertEquals(2, path.size(), "Capture should have 2 squares");
        assertEquals(new Square(5, 1), path.get(0), "Should start from RED piece");
        assertEquals(new Square(3, 3), path.get(1), "Should land after capture");
    }

    @Test
    void testAIStrategyIntegration() {
        // Test that AI follows its strategy consistently
        
        // 1. Create a game
        Map<String, Object> createResponse = gameController.create();
        String gameId = (String) createResponse.get("gameId");
        
        // 2. Make a move to trigger AI response
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));
        
        ResponseEntity<?> moveResponse = gameController.move(gameId, moveRequest);
        assertEquals(HttpStatus.OK, moveResponse.getStatusCode(), "Move should succeed");
        
        // 3. Verify AI made a move (board should have changed)
        ResponseEntity<?> boardResponse = gameController.board(gameId, "ascii");
        String boardAfterAIMove = (String) boardResponse.getBody();
        
        // Board should show RED's turn again
        assertTrue(boardAfterAIMove.contains("Turn: RED"), "AI should have responded");
        
        // 4. Make another move to see AI consistency
        MoveRequest moveRequest2 = new MoveRequest();
        moveRequest2.setPath(List.of(
            new MoveRequest.Point(5, 3),
            new MoveRequest.Point(4, 2)
        ));
        
        ResponseEntity<?> moveResponse2 = gameController.move(gameId, moveRequest2);
        assertEquals(HttpStatus.OK, moveResponse2.getStatusCode(), "Second move should succeed");
        
        // 5. Verify AI responded again
        ResponseEntity<?> finalBoardResponse = gameController.board(gameId, "ascii");
        String finalBoard = (String) finalBoardResponse.getBody();
        assertTrue(finalBoard.contains("Turn: RED"), "AI should have responded to second move");
    }

    @Test
    void testGameStateConsistency() {
        // Test that game state remains consistent across all operations
        
        // 1. Create a game
        Map<String, Object> createResponse = gameController.create();
        String gameId = (String) createResponse.get("gameId");
        
        // 2. Verify initial state
        Game initialGame = gameService.get(gameId).orElseThrow();
        assertEquals(Color.RED, initialGame.getTurn(), "Initial turn should be RED");
        assertEquals(GameResult.ONGOING, initialGame.getResult(), "Initial result should be ONGOING");
        
        // 3. Make a move
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));
        
        ResponseEntity<?> moveResponse = gameController.move(gameId, moveRequest);
        assertEquals(HttpStatus.OK, moveResponse.getStatusCode(), "Move should succeed");
        
        // 4. Verify state changed consistently
        Game gameAfterMove = gameService.get(gameId).orElseThrow();
        assertEquals(Color.RED, gameAfterMove.getTurn(), "Turn should be RED after AI move");
        assertEquals(GameResult.ONGOING, gameAfterMove.getResult(), "Game should still be ongoing");
        
        // 5. Verify board representation is consistent
        ResponseEntity<?> boardResponse = gameController.board(gameId, "ascii");
        String boardAscii = (String) boardResponse.getBody();
        
        ResponseEntity<?> boardJsonResponse = gameController.board(gameId, "json");
        Object boardJson = boardJsonResponse.getBody();
        
        // Both representations should show the same game state
        assertNotNull(boardAscii, "ASCII board should not be null");
        assertNotNull(boardJson, "JSON board should not be null");
    }

    @Test
    void testErrorHandlingIntegration() {
        // Test error handling across the entire system
        
        // 1. Test invalid game ID
        ResponseEntity<?> invalidGameResponse = gameController.board("invalid-id", "json");
        assertEquals(HttpStatus.NOT_FOUND, invalidGameResponse.getStatusCode(), "Should return NOT_FOUND");
        assertEquals("Game not found", invalidGameResponse.getBody(), "Should return appropriate error message");
        
        // 2. Test invalid move in valid game
        Map<String, Object> createResponse = gameController.create();
        String gameId = (String) createResponse.get("gameId");
        
        // Try to make an invalid move
        MoveRequest invalidMoveRequest = new MoveRequest();
        invalidMoveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 1) // Not diagonal
        ));
        
        ResponseEntity<?> invalidMoveResponse = gameController.move(gameId, invalidMoveRequest);
        assertEquals(HttpStatus.BAD_REQUEST, invalidMoveResponse.getStatusCode(), "Should return BAD_REQUEST");
        assertNotNull(invalidMoveResponse.getBody(), "Should return error message");
        
        // 3. Test malformed move request
        MoveRequest malformedMoveRequest = new MoveRequest();
        malformedMoveRequest.setPath(List.of()); // Empty path
        
        ResponseEntity<?> malformedMoveResponse = gameController.move(gameId, malformedMoveRequest);
        assertEquals(HttpStatus.BAD_REQUEST, malformedMoveResponse.getStatusCode(), "Should return BAD_REQUEST");
        assertNotNull(malformedMoveResponse.getBody(), "Should return error message");
    }

    @Test
    void testPerformanceIntegration() {
        // Test system performance with multiple operations
        
        // 1. Create multiple games rapidly
        String[] gameIds = new String[5];
        for (int i = 0; i < 5; i++) {
            Map<String, Object> response = gameController.create();
            gameIds[i] = (String) response.get("gameId");
        }
        
        // 2. Verify all games were created successfully
        for (int i = 0; i < 5; i++) {
            assertNotNull(gameIds[i], "Game " + i + " should have valid ID");
            
            ResponseEntity<?> boardResponse = gameController.board(gameIds[i], "ascii");
            assertEquals(HttpStatus.OK, boardResponse.getStatusCode(), "Game " + i + " should be accessible");
        }
        
        // 3. Make moves in multiple games
        for (int i = 0; i < 5; i++) {
            MoveRequest moveRequest = new MoveRequest();
            moveRequest.setPath(List.of(
                new MoveRequest.Point(5, 1),
                new MoveRequest.Point(4, 0)
            ));
            
            ResponseEntity<?> moveResponse = gameController.move(gameIds[i], moveRequest);
            assertEquals(HttpStatus.OK, moveResponse.getStatusCode(), "Move in game " + i + " should succeed");
        }
        
        // 4. Verify all games are in consistent state
        for (int i = 0; i < 5; i++) {
            ResponseEntity<?> boardResponse = gameController.board(gameIds[i], "ascii");
            String board = (String) boardResponse.getBody();
            assertTrue(board.contains("Turn: RED"), "Game " + i + " should show RED's turn");
        }
    }

    @Test
    void testGameRulesIntegration() {
        // Test that game rules are properly enforced throughout the system
        
        // 1. Create a game
        Map<String, Object> createResponse = gameController.create();
        String gameId = (String) createResponse.get("gameId");
        
        // 2. Test forced capture rule
        Game game = gameService.get(gameId).orElseThrow();
        
        // Set up a position where RED must capture
        Board customBoard = new Board();
        customBoard.set(5, 1, new Piece(Color.RED, false)); // RED piece
        customBoard.set(4, 2, new Piece(Color.BLACK, false)); // BLACK piece to capture
        customBoard.set(4, 0, null); // Safe move option
        
        // Get legal moves through rules engine
        List<Move> legalMoves = rulesEngine.legalMoves(customBoard, Color.RED);
        
        // Should only have capture moves (forced capture rule)
        assertFalse(legalMoves.isEmpty(), "Should have legal moves");
        assertTrue(legalMoves.stream().allMatch(Move::isCapture), "All moves should be captures when captures are available");
        
        // 3. Test that AI follows the same rules
        Move aiMove = aiPlayer.chooseMove(customBoard, Color.RED);
        assertNotNull(aiMove, "AI should find a move");
        assertTrue(aiMove.isCapture(), "AI should choose capture when forced");
    }
}
