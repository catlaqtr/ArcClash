package com.arcclash.backend.game.duel;

public class DuelSet {
    private int playerOneWins;
    private int playerTwoWins;

    public DuelSet() {

    }

    public void recordWin(PlayerSide winner){
        if(isFinished()){
            throw new IllegalStateException("game is over");
        }

        if(winner == PlayerSide.PLAYER_ONE){
            playerOneWins +=1;
        }else if(winner == PlayerSide.PLAYER_TWO){
            playerTwoWins +=1;
        }else{
            throw new IllegalArgumentException("has to be a player");

        }

    }

    public int getPlayerOneWins() {
        return playerOneWins;
    }

    public int getPlayerTwoWins() {
        return playerTwoWins;
    }

    public boolean isFinished(){
        if(playerOneWins == 3 || playerTwoWins ==3){
            return true;
        }else{
            return false;
        }

    }

    public PlayerSide getWinner(){
        if(playerOneWins ==3 ){
            return PlayerSide.PLAYER_ONE;
        }else if(playerTwoWins ==3){
            return PlayerSide.PLAYER_TWO;
        }else {
            return null;
        }
    }
}
