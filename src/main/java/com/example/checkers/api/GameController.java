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

    @PostMapping
    public Map<String, Object> create(@RequestParam(defaultValue = "4") int aiDepth) {
        Game g = games.createGame(aiDepth);
        Map<String, Object> res = new HashMap<>();
        res.put("gameId", g.getId());
        res.put("turn", g.getTurn().name());
        res.put("ascii", g.ascii());
        return res;
    }

    @GetMapping(value = "/{id}/board")
    public ResponseEntity<?> board(@PathVariable String id, @RequestParam(defaultValue = "json") String format) {
        Game g = games.get(id).orElse(null);
        if (g == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        if ("ascii".equalsIgnoreCase(format)) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(g.ascii());
        }
        return ResponseEntity.ok(BoardResponse.from(g.getBoard(), g.getTurn()));
    }

    @PostMapping(value = "/{id}/move")
    public ResponseEntity<?> move(@PathVariable String id,
            @RequestParam(defaultValue = "BLACK") String aiColor,
            @Valid @RequestBody MoveRequest req) {
        Game g = games.get(id).orElse(null);
        if (g == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        Color aiC;
        try {
            aiC = Color.valueOf(aiColor.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid aiColor");
        }

        Move playerMove = toMove(req);
        try {
            g.makeMove(playerMove);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        Move aiMove = null;
        try {
            aiMove = g.aiMoveIfTurn(aiC);
        } catch (IllegalStateException ex) {
            // ignore; game ended
        }

        Map<String, Object> res = new HashMap<>();
        res.put("playerMove", playerMove.toString());
        res.put("aiMove", aiMove == null ? null : aiMove.toString());
        res.put("turn", g.getTurn().name());
        res.put("ascii", g.ascii());
        res.put("board", BoardResponse.from(g.getBoard(), g.getTurn()));
        return ResponseEntity.ok(res);
    }

    private Move toMove(MoveRequest req) {
        List<MoveRequest.Point> p = req.getPath();
        if (p.size() < 2)
            throw new IllegalArgumentException("path must have at least 2 points");
        return new Move(p.stream().map(pt -> new Square(pt.getRow(), pt.getCol())).toList());
    }
}
