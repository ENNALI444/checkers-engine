package com.example.checkers.api;

import com.example.checkers.model.Board;
import com.example.checkers.model.Color;
import com.example.checkers.model.Piece;

public class BoardResponse {
    private String[][] board; // null or r/rk/b/bk
    private String turn;

    public static BoardResponse from(Board b, Color turn) {
        BoardResponse res = new BoardResponse();
        String[][] arr = new String[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = b.get(r, c);
                arr[r][c] = p == null ? null : p.toString();
            }
        }
        res.board = arr;
        res.turn = turn.name();
        return res;
    }

    public String[][] getBoard() { return board; }
    public String getTurn() { return turn; }
}


