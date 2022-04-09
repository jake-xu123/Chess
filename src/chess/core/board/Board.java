package chess.core.board;

import java.util.Set;
import chess.core.pieces.Piece;
import chess.core.player.Player.Alliance;

public class Board {
    public static final int HEIGHT = 8;
    public static final int WIDTH = 8;
    private Alliance turn;
    private boolean whiteKingExist;
    private boolean blackKingExist;
    private Piece[][] board ;
    private Steps steps;

    private void resetSteps(){
        // TODO
    }
    private void resetStatus(){
        whiteKingExist = true;
        blackKingExist = true;
        turn = Alliance.WHITE;
    }
    private void resetBoard(){
        // TODO
    }

    public Board(){
        resetStatus();
        resetBoard();
        resetSteps();
    }

    public char[][] getBoard() {
        char[][] charBoard = new char[Board.HEIGHT][Board.WIDTH];
        for(int y = 0; y < Board.HEIGHT; y++){
            for(int x = 0; x < Board.WIDTH; x++){
                charBoard[x][y] = board[x][y].toString().charAt(0);
            }
        }
        return charBoard;
        
    }

    public Set<Position> getAvailablPositions(int x, int y){
        return board[x][y].availablePosition(this);
    }

    public boolean movePiece(int x1, int y1, int x2, int y2, Alliance alliance){
        // alliance try to move the piece in (x1, y1) to (x2, y2)
        // if the operation is valid, return true; otherwise return false
        Position p2 = new Position(x2, y2);

        if(alliance != turn){
            System.out.println("It's not your turn");
            return false;
        }
        if(board[x1][y1].availablePosition(this).contains(p2)){
            board[x1][y1] = null;
            // TODO
            return true;
        }else{
            System.out.println("the position "+ p2 +" is not available");
            return false;
        }

    }


    public String ifWin(){
        // output "WHITE" or "BLACK" to represent the winner
        // output "DRAW" to represent the drawn game
        // output "NOTHING" to represent that nothing happens
        if(turn == Alliance.BLACK){
            if(!blackKingExist) return "WHITE";
        }else{
            if(!whiteKingExist) return "BLACK";
        }
        // TODO draw
        return "NOTHING";
    }

}
