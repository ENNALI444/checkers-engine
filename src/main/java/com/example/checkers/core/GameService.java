package com.example.checkers.core;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public Game createGame(int aiDepth) {
        Game g = new Game(aiDepth);
        games.put(g.getId(), g);
        return g;
    }

    public Optional<Game> get(String id) {
        return Optional.ofNullable(games.get(id));
    }
}
