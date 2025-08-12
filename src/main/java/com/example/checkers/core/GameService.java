package com.example.checkers.core;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    /**
     * Creates a new checkers game.
     * 
     * @return The newly created game
     */
    public Game createGame() {
        Game g = new Game();
        games.put(g.getId(), g);
        return g;
    }

    /**
     * Gets a game by its ID.
     * 
     * @param id The game ID
     * @return Optional containing the game if found
     */
    public Optional<Game> get(String id) {
        return Optional.ofNullable(games.get(id));
    }
}
