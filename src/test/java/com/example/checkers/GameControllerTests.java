package com.example.checkers;

import com.example.checkers.api.GameController;
import com.example.checkers.api.MoveRequest;
import com.example.checkers.core.Game;
import com.example.checkers.core.GameService;
import com.example.checkers.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GameController API endpoints.
 * Verifies REST API functionality and response handling.
 */
public class GameControllerTests {

    private GameController controller;
    private GameService gameService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
        controller = new GameController(gameService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateGame() {
        // Test game creation endpoint
        Map<String, Object> response = controller.create();

        // Verify response structure
        assertNotNull(response, "Response should not be null");
        assertTrue(response.containsKey("gameId"), "Response should contain gameId");
        assertTrue(response.containsKey("turn"), "Response should contain turn");
        assertTrue(response.containsKey("ascii"), "Response should contain ascii");
        assertTrue(response.containsKey("board"), "Response should contain board");

        // Verify response values
        String gameId = (String) response.get("gameId");
        assertNotNull(gameId, "Game ID should not be null");
        assertFalse(gameId.isEmpty(), "Game ID should not be empty");

        String turn = (String) response.get("turn");
        assertEquals("RED", turn, "Game should start with RED's turn");

        String ascii = (String) response.get("ascii");
        assertNotNull(ascii, "ASCII representation should not be null");
        assertTrue(ascii.contains("Turn: RED"), "ASCII should show RED's turn");
    }

    @Test
    void testGetBoardJson() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Test getting board as JSON
        ResponseEntity<?> response = controller.board(gameId, "json");

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    @Test
    void testGetBoardAscii() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Test getting board as ASCII
        ResponseEntity<?> response = controller.board(gameId, "ascii");

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType(), "Should return plain text");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        String ascii = (String) response.getBody();
        assertTrue(ascii.contains("Turn:"), "ASCII should contain turn information");
        assertTrue(ascii.contains("🔴"), "ASCII should contain RED pieces");
        assertTrue(ascii.contains("⚫"), "ASCII should contain BLACK pieces");
    }

    @Test
    void testGetBoardInvalidGame() {
        // Test getting board for non-existent game
        ResponseEntity<?> response = controller.board("invalid-id", "json");

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return NOT_FOUND status");
        assertEquals("Game not found", response.getBody(), "Should return appropriate error message");
    }

    @Test
    void testGetLegalMoves() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Test getting legal moves
        ResponseEntity<?> response = controller.legalMoves(gameId);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");
        assertNotNull(response.getBody(), "Response body should not be null");

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.containsKey("legalMoves"), "Response should contain legalMoves");
        assertTrue(responseBody.containsKey("turn"), "Response should contain turn");
        assertTrue(responseBody.containsKey("gameResult"), "Response should contain gameResult");

        @SuppressWarnings("unchecked")
        List<Move> legalMoves = (List<Move>) responseBody.get("legalMoves");
        assertNotNull(legalMoves, "Legal moves should not be null");
        assertFalse(legalMoves.isEmpty(), "Should have legal moves available");
    }

    @Test
    void testGetLegalMovesInvalidGame() {
        // Test getting legal moves for non-existent game
        ResponseEntity<?> response = controller.legalMoves("invalid-id");

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return NOT_FOUND status");
        assertEquals("Game not found", response.getBody(), "Should return appropriate error message");
    }

    @Test
    void testMakeMove() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Create a valid move request
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));

        // Test making a move
        ResponseEntity<?> response = controller.move(gameId, moveRequest);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType(), "Should return plain text");
        assertNotNull(response.getBody(), "Response body should not be null");

        String ascii = (String) response.getBody();
        assertTrue(ascii.contains("Turn:"), "Response should contain board representation");
        assertTrue(ascii.contains("🔴"), "Response should contain RED pieces");
        assertTrue(ascii.contains("⚫"), "Response should contain BLACK pieces");
    }

    @Test
    void testMakeMoveInvalidGame() {
        // Test making a move in non-existent game
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));

        ResponseEntity<?> response = controller.move("invalid-id", moveRequest);

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return NOT_FOUND status");
        assertEquals("Game not found", response.getBody(), "Should return appropriate error message");
    }

    @Test
    void testMakeMoveInvalidMove() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Create an invalid move request (not diagonal)
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 1) // Not diagonal
        ));

        ResponseEntity<?> response = controller.move(gameId, moveRequest);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return BAD_REQUEST status");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().toString().contains("Illegal"), "Should return illegal move error");
    }

    @Test
    void testMakeMoveEmptyPath() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Create a move request with empty path
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of());

        ResponseEntity<?> response = controller.move(gameId, moveRequest);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return BAD_REQUEST status");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().toString().contains("path must have at least 2 points"), 
                  "Should return appropriate error message");
    }

    @Test
    void testMakeMoveSinglePoint() {
        // Create a game first
        Game game = gameService.createGame();
        String gameId = game.getId();

        // Create a move request with only one point
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(new MoveRequest.Point(5, 1)));

        ResponseEntity<?> response = controller.move(gameId, moveRequest);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return BAD_REQUEST status");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().toString().contains("path must have at least 2 points"), 
                  "Should return appropriate error message");
    }

    @Test
    void testMoveRequestValidation() {
        // Test MoveRequest creation and validation
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));

        assertNotNull(moveRequest.getPath(), "Path should not be null");
        assertEquals(2, moveRequest.getPath().size(), "Path should have 2 points");
        assertEquals(5, moveRequest.getPath().get(0).getRow(), "First point should have correct row");
        assertEquals(1, moveRequest.getPath().get(0).getCol(), "First point should have correct col");
        assertEquals(4, moveRequest.getPath().get(1).getRow(), "Second point should have correct row");
        assertEquals(0, moveRequest.getPath().get(1).getCol(), "Second point should have correct col");
    }

    @Test
    void testPointValidation() {
        // Test Point creation and validation
        MoveRequest.Point point = new MoveRequest.Point(5, 1);
        
        assertEquals(5, point.getRow(), "Point should have correct row");
        assertEquals(1, point.getCol(), "Point should have correct col");
    }

    @Test
    void testGameFlowIntegration() {
        // Test complete game flow through API
        // 1. Create game
        Map<String, Object> createResponse = controller.create();
        String gameId = (String) createResponse.get("gameId");
        assertEquals("RED", createResponse.get("turn"), "Game should start with RED's turn");

        // 2. Get board
        ResponseEntity<?> boardResponse = controller.board(gameId, "ascii");
        assertEquals(HttpStatus.OK, boardResponse.getStatusCode(), "Should get board successfully");

        // 3. Get legal moves
        ResponseEntity<?> movesResponse = controller.legalMoves(gameId);
        assertEquals(HttpStatus.OK, movesResponse.getStatusCode(), "Should get legal moves successfully");

        // 4. Make a move
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));

        ResponseEntity<?> moveResponse = controller.move(gameId, moveRequest);
        assertEquals(HttpStatus.OK, moveResponse.getStatusCode(), "Should make move successfully");

        // 5. Verify game state changed
        ResponseEntity<?> updatedBoardResponse = controller.board(gameId, "ascii");
        assertEquals(HttpStatus.OK, updatedBoardResponse.getStatusCode(), "Should get updated board");
        
        String updatedBoard = (String) updatedBoardResponse.getBody();
        assertNotNull(updatedBoard, "Updated board should not be null");
        assertTrue(updatedBoard.contains("Turn:"), "Updated board should show current turn");
    }

    @Test
    void testMultipleMovesIntegration() {
        // Test multiple moves in sequence
        Map<String, Object> createResponse = controller.create();
        String gameId = (String) createResponse.get("gameId");

        // First move
        MoveRequest move1 = new MoveRequest();
        move1.setPath(List.of(
            new MoveRequest.Point(5, 1),
            new MoveRequest.Point(4, 0)
        ));

        ResponseEntity<?> response1 = controller.move(gameId, move1);
        assertEquals(HttpStatus.OK, response1.getStatusCode(), "First move should succeed");

        // Second move
        MoveRequest move2 = new MoveRequest();
        move2.setPath(List.of(
            new MoveRequest.Point(5, 3),
            new MoveRequest.Point(4, 2)
        ));

        ResponseEntity<?> response2 = controller.move(gameId, move2);
        assertEquals(HttpStatus.OK, response2.getStatusCode(), "Second move should succeed");

        // Verify game is still ongoing
        ResponseEntity<?> boardResponse = controller.board(gameId, "ascii");
        String board = (String) boardResponse.getBody();
        assertTrue(board.contains("Turn:"), "Board should show current turn");
    }
}
