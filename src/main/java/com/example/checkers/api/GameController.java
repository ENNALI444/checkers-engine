package com.example.checkers.api;

import com.example.checkers.core.Game;
import com.example.checkers.core.GameService;
import com.example.checkers.model.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService games;

    public GameController(GameService games) {
        this.games = games;
    }

    /**
     * Creates a new checkers game.
     * 
     * @return Game information including ID, current turn, and board
     */
    @PostMapping
    public Map<String, Object> create() {
        Game g = games.createGame();
        Map<String, Object> res = new HashMap<>();
        res.put("gameId", g.getId());
        res.put("turn", g.getTurn().name());
        res.put("ascii", g.ascii());
        res.put("board", BoardResponse.from(g.getBoard(), g.getTurn()));
        return res;
    }

    /**
     * Gets the current board state.
     * 
     * @param id     Game ID
     * @param format Response format (json or ascii)
     * @return Board state in requested format
     */
    @GetMapping(value = "/{id}/board")
    public ResponseEntity<?> board(@PathVariable String id, @RequestParam(defaultValue = "json") String format) {
        Game g = games.get(id).orElse(null);
        if (g == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        if ("ascii".equalsIgnoreCase(format)) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(g.ascii());
        }

        return ResponseEntity.ok(BoardResponse.from(g.getBoard(), g.getTurn()));
    }

    /**
     * Gets all legal moves for the current player.
     * 
     * @param id Game ID
     * @return List of legal moves
     */
    @GetMapping(value = "/{id}/moves")
    public ResponseEntity<?> legalMoves(@PathVariable String id) {
        Game g = games.get(id).orElse(null);
        if (g == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        List<Move> moves = g.legalMoves();
        Map<String, Object> response = new HashMap<>();
        response.put("legalMoves", moves);
        response.put("turn", g.getTurn().name());
        response.put("gameResult", g.getResult().name());

        return ResponseEntity.ok(response);
    }

    /**
     * Makes a move in the game.
     * 
     * @param id  Game ID
     * @param req Move request containing the path
     * @return Board state as plain text after the move
     */
    @PostMapping(value = "/{id}/move")
    public ResponseEntity<?> move(@PathVariable String id, @Valid @RequestBody MoveRequest req) {
        Game g = games.get(id).orElse(null);
        if (g == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        Move playerMove = toMove(req);
        try {
            g.makeMove(playerMove);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

        // Return the board as plain text (like the ASCII endpoint)
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(g.ascii());
    }

    /**
     * Converts MoveRequest to Move object.
     * 
     * @param req The move request
     * @return Move object
     */
    private Move toMove(MoveRequest req) {
        List<MoveRequest.Point> p = req.getPath();
        if (p.size() < 2) {
            throw new IllegalArgumentException("path must have at least 2 points");
        }
        return new Move(p.stream().map(pt -> new Square(pt.getRow(), pt.getCol())).toList());
    }
}
