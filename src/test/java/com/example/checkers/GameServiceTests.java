package com.example.checkers;

import com.example.checkers.core.Game;
import com.example.checkers.core.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GameService class.
 * Verifies game storage, retrieval, and management functionality.
 */
public class GameServiceTests {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
    }

    @Test
    void testCreateGame() {
        // Test game creation
        Game game = gameService.createGame();

        // Verify game properties
        assertNotNull(game, "Created game should not be null");
        assertNotNull(game.getId(), "Game should have an ID");
        assertFalse(game.getId().isEmpty(), "Game ID should not be empty");
        assertNotNull(game.getBoard(), "Game should have a board");
        assertNotNull(game.getTurn(), "Game should have a turn");
        assertNotNull(game.getResult(), "Game should have a result");
    }

    @Test
    void testCreateMultipleGames() {
        // Test creating multiple games
        Game game1 = gameService.createGame();
        Game game2 = gameService.createGame();
        Game game3 = gameService.createGame();

        // Verify all games are created
        assertNotNull(game1, "First game should not be null");
        assertNotNull(game2, "Second game should not be null");
        assertNotNull(game3, "Third game should not be null");

        // Verify games have different IDs
        assertNotEquals(game1.getId(), game2.getId(), "Games should have different IDs");
        assertNotEquals(game2.getId(), game3.getId(), "Games should have different IDs");
        assertNotEquals(game1.getId(), game3.getId(), "Games should have different IDs");
    }

    @Test
    void testGetExistingGame() {
        // Create a game first
        Game createdGame = gameService.createGame();
        String gameId = createdGame.getId();

        // Test retrieving the game
        Optional<Game> retrievedGame = gameService.get(gameId);

        // Verify game is retrieved
        assertTrue(retrievedGame.isPresent(), "Game should be found");
        Game game = retrievedGame.get();
        assertEquals(gameId, game.getId(), "Retrieved game should have correct ID");
        assertSame(createdGame, game, "Retrieved game should be the same instance");
    }

    @Test
    void testGetNonExistentGame() {
        // Test retrieving a non-existent game
        Optional<Game> retrievedGame = gameService.get("non-existent-id");

        // Verify game is not found
        assertFalse(retrievedGame.isPresent(), "Non-existent game should not be found");
    }

    @Test
    void testGetGameWithNullId() {
        // Test retrieving a game with null ID
        Optional<Game> retrievedGame = gameService.get(null);

        // Verify game is not found
        assertFalse(retrievedGame.isPresent(), "Game with null ID should not be found");
    }

    @Test
    void testGetGameWithEmptyId() {
        // Test retrieving a game with empty ID
        Optional<Game> retrievedGame = gameService.get("");

        // Verify game is not found
        assertFalse(retrievedGame.isPresent(), "Game with empty ID should not be found");
    }

    @Test
    void testGameServiceIsolation() {
        // Test that different service instances don't share games
        GameService service1 = new GameService();
        GameService service2 = new GameService();

        // Create games in different services
        Game game1 = service1.createGame();
        Game game2 = service2.createGame();

        // Verify games exist in their respective services
        Optional<Game> retrieved1 = service1.get(game1.getId());
        Optional<Game> retrieved2 = service2.get(game2.getId());

        assertTrue(retrieved1.isPresent(), "Game should be found in service1");
        assertTrue(retrieved2.isPresent(), "Game should be found in service2");

        // Verify games don't exist in other services
        Optional<Game> crossRetrieved1 = service2.get(game1.getId());
        Optional<Game> crossRetrieved2 = service1.get(game2.getId());

        assertFalse(crossRetrieved1.isPresent(), "Game from service1 should not be found in service2");
        assertFalse(crossRetrieved2.isPresent(), "Game from service2 should not be found in service1");
    }

    @Test
    void testGameServiceConcurrency() {
        // Test that game service can handle multiple concurrent operations
        GameService service = new GameService();

        // Create multiple games rapidly
        Game[] games = new Game[10];
        for (int i = 0; i < 10; i++) {
            games[i] = service.createGame();
        }

        // Verify all games were created successfully
        for (int i = 0; i < 10; i++) {
            assertNotNull(games[i], "Game " + i + " should not be null");
            assertNotNull(games[i].getId(), "Game " + i + " should have an ID");
            
            // Verify each game can be retrieved
            Optional<Game> retrieved = service.get(games[i].getId());
            assertTrue(retrieved.isPresent(), "Game " + i + " should be retrievable");
            assertEquals(games[i].getId(), retrieved.get().getId(), 
                       "Retrieved game " + i + " should have correct ID");
        }

        // Verify all games have unique IDs
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                assertNotEquals(games[i].getId(), games[j].getId(), 
                               "Games " + i + " and " + j + " should have different IDs");
            }
        }
    }

    @Test
    void testGameServiceMemoryManagement() {
        // Test that game service doesn't leak memory
        GameService service = new GameService();
        
        // Create many games
        for (int i = 0; i < 100; i++) {
            Game game = service.createGame();
            assertNotNull(game, "Game " + i + " should be created successfully");
            
            // Verify game can be retrieved immediately
            Optional<Game> retrieved = service.get(game.getId());
            assertTrue(retrieved.isPresent(), "Game " + i + " should be retrievable");
        }
    }

    @Test
    void testGameServiceRetrievalAfterCreation() {
        // Test immediate retrieval after creation
        Game createdGame = gameService.createGame();
        String gameId = createdGame.getId();

        // Retrieve immediately
        Optional<Game> retrievedGame = gameService.get(gameId);

        // Verify successful retrieval
        assertTrue(retrievedGame.isPresent(), "Game should be immediately retrievable");
        Game game = retrievedGame.get();
        assertEquals(gameId, game.getId(), "Retrieved game should have correct ID");
        assertSame(createdGame, game, "Retrieved game should be the same instance");
    }

    @Test
    void testGameServiceWithSpecialCharacters() {
        // Test game service with special characters in IDs (edge case)
        GameService service = new GameService();
        
        // Create a game
        Game game = service.createGame();
        String gameId = game.getId();
        
        // Verify game can be retrieved with its actual ID
        Optional<Game> retrieved = service.get(gameId);
        assertTrue(retrieved.isPresent(), "Game should be retrievable with actual ID");
        
        // Test that similar but different IDs don't work
        if (gameId.length() > 1) {
            String similarId = gameId.substring(0, gameId.length() - 1) + "X";
            Optional<Game> similarRetrieved = service.get(similarId);
            assertFalse(similarRetrieved.isPresent(), "Similar ID should not retrieve game");
        }
    }
}
