package chess.core.board;

import java.util.Set;

import chess.core.pieces.*;
import chess.core.pieces.Piece.Type;
import chess.core.player.Player.Alliance;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Board {
    public static final int HEIGHT = 8;
    public static final int WIDTH = 8;
    private Alliance turn;
    private boolean whiteKingExist;
    private boolean blackKingExist;
    private Piece[][] board;
    private Status status;
    private int round;

    private void resetStatus(){
        whiteKingExist = true;
        blackKingExist = true;
        turn = Alliance.WHITE;
        round = 1;
        status = new Status();
    }
    private void resetBoard(){
        char[][] charBoard = new char[Board.WIDTH][Board.HEIGHT];
        try {
            Scanner sc = new Scanner(new File("data/ini.dat"));
            
            for(int y = 0; y < Board.HEIGHT; y++){
                String s = sc.nextLine();
                for(int x = 0; x < Board.WIDTH; x++){
                    charBoard[x][y] = s.charAt(x);
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.board = new Piece[Board.WIDTH][Board.HEIGHT];
        puts(charBoard);
    }

    public void saveGame(String path, int leftTime){
        this.status.saveStatus(path, leftTime);
    }

    public boolean loadGame(String path){
        this.reset();
        return this.status.loadStatus(path, this);
    }

    private void put(Piece p){
        // put p into a position 
        // if there is a piece already, replace it
        Position pos = p.getPosition();
        int x = pos.getX();
        int y = pos.getY();
        board[x][y] = p;
    }

    private void put(char type, int x, int y){
        Piece p = Piece.pieceGenerator(type, x , y);
        if(p != null){
            put(p);
        }
    }

    private void puts(char[][] type){
        for(int y = 0; y < Board.HEIGHT; y++){
            for(int x = 0; x < Board.WIDTH; x++){
                put(type[x][y], x, y);
            }
        }
    }

    public Board(){
        reset();
    }
    
    public void reset(){
        resetStatus();
        resetBoard();
    }

    public char[][] getCharBoard() {
        // BKNPQR and bknpqr are abbreviations. Notice: N indicates Knight and K indicated King
        // the uppercase means WHITE and the lowercase means BLACK
        char[][] charBoard = new char[Board.HEIGHT][Board.WIDTH];
        for(int y = 0; y < Board.HEIGHT; y++){
            for(int x = 0; x < Board.WIDTH; x++){
                if(board[x][y] != null)
                    charBoard[x][y] = board[x][y].toChar();
                else
                    charBoard[x][y] = 'x';
            }
        }
        return charBoard;
    }

    public Piece[][] getBoard(){
        return this.board;
    }

    public Status getStatus(){
        return this.status;
    }

    public int[][] getAvailablePositions(int x, int y){
        // return n-by-2 matrix or null
        if(Board.isOutOfBound(x, y)){
            System.out.printf("Your input (%d, %d) is out of boundary when incoking getAvailablePositions\n", x, y);
            return null;
        }
        if(board[x][y] == null) {
            System.out.printf("no piece at (%d, %d)\n", x, y);
            return null;
        }
        Set<Position> ps = board[x][y].availablePosition(this);
        if(ps.size()==0) return null;
        int[][] psInt = new int[ps.size()][2];
        int i = 0;
        for(Position p : ps){
            psInt[i][0] = p.getX();
            psInt[i][1] = p.getY();
            i++;
        }
        return psInt;
    }

    public boolean isAvailable(int x1, int y1, int x2, int y2){
        Position p1 = new Position(x1, y1);
        Position p2 = new Position(x2, y2);
        boolean valid = true;
        if(board[x1][y1] == null){
            System.out.println("there is no piece at " + p1 + "\n");
            valid = false;
        }else if(board[x1][y1].getAlliance() != this.turn){
            System.out.println("it is not your turn");
            valid = false;
        } else{
            Boolean contain = false;
            Set<Position> ps = board[x1][y1].availablePosition(this);
            for(Position p : ps){
                if(p.equals(p2)) contain = true;
            }
            if(!contain){
                System.out.println("the piece cannot go to " + p2);
                valid = false;
            }
        }
        return valid;
    }

    public boolean movePiece(int x1, int y1, int x2, int y2){
        // alliance try to move the piece in (x1, y1) to (x2, y2)
        // if the operation is valid, return true; otherwise return false
        if(!isAvailable(x1, y1, x2, y2)) return false;
        // we suppose that isAvailable goes well
        // then (x2, y2) is either an enermy or null
        if(board[x2][y2] != null && board[x2][y2].getType() == Type.KING){
            if(board[x2][y2].getAlliance() == Alliance.BLACK){
                this.blackKingExist = false;
            }else{
                this.whiteKingExist = false;
            }
        }
        board[x1][y1].move(x2, y2, this);
        status.add(new Move(x1, y1, x2, y2, board[x2][y2].getType()));
        nextTurn();
        return true;
    }

    public boolean movePiece(Move m){
        Position p1 = m.getP1();
        Position p2 = m.getP2();
        int x1 = p1.getX();
        int y1 = p1.getY();
        int x2 = p2.getX();
        int y2 = p2.getY();
        return movePiece(x1, y1, x2, y2);
    }

    public static boolean strongNear(int x1, int y1, int x2, int y2){
        if(x1-x2==0 && (y1-y2==1 || y1-y2==-1)) return true;
        if(y1-y2==0 && (x1-x2==1 || x1-x2==-1)) return true;
        return false;
    }
    
    public static boolean weakNear(int x1, int y1, int x2, int y2){
        if(x1==x2 && y1==y2) return false;
        if((x1-x2 >= -1 || x1-x2 <= 1) && (y1-y2 >= -1 || y1-y2 <= 1)) return true;
        return true;
    }

    public static boolean isDiagonal(int x1, int y1, int x2, int y2){
        int dx = x1-x2;
        int dy = y1-y2;
        if((dx == 1 || dx == -1) && (dy == 1 || dy == -1)) return true;
        return false;
    }

    public void nextTurn(){
        if(turn == Alliance.WHITE) turn = Alliance.BLACK;
        else{
            turn = Alliance.WHITE;
            round++;
        }
        for(int x = 0; x < Board.WIDTH; x++){
            for(int y = 0; y < Board.HEIGHT; y++){
                if(board[x][y]!=null)
                    board[x][y].nextTurn();
            }
        }
    }

    public int getRound(){
        return this.round;
    }

    public char ifWin(){
        // output "W" or "B" to represent the winner
        // output "D" to represent the drawn game
        // output "N" to represent that nothing happens
        if(turn == Alliance.BLACK){
            if(!blackKingExist) return 'W';
        }else{
            if(!whiteKingExist) return 'B';
        }
        // TODO draw
        return 'N';
    }

    public static boolean isOutOfBound(int x, int y){
        if(x >= 0 && x < Board.WIDTH && y >= 0 && y < Board.HEIGHT)
            return false;
        else return true;
    }
    public static boolean isOutOfBound(Position p){
        return isOutOfBound(p.getX(), p.getY());
    }
    public Alliance getTurn(){
        if(turn == null) System.out.println("Turn is not set");
        return this.turn;
    }
    public char getTurnChar(){
        if(this.turn == Alliance.BLACK) return 'B';
        if(this.turn == Alliance.WHITE) return 'W';
        System.out.println("Turn is not set");
        return 'N';
    }
    public void setPiece(int x, int y, char type) {
        board[x][y] = Piece.pieceGenerator(type, x, y);
    }

}
