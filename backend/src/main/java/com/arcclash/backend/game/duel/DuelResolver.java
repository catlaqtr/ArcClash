package com.arcclash.backend.game.duel;

public class DuelResolver {
    public PlayerSide resolve(CharacterStats firstStats, Stance firstStance, CharacterStats secondStats, Stance secondStance, PlayerSide tiePriority){
        DuelPowerCalculator duel = new DuelPowerCalculator();
        int p1 =duel.calculate(firstStats,firstStance,secondStance);
        int p2 = duel.calculate(secondStats,secondStance,firstStance);
        if(p1 > p2){
            return PlayerSide.PLAYER_ONE;
        }else if(p2>p1){
            return  PlayerSide.PLAYER_TWO;
        }else{
            if(firstStats.getSpeed() > secondStats.getSpeed()){
                return  PlayerSide.PLAYER_ONE;
            }else if(secondStats.getSpeed() > firstStats.getSpeed()){
                return PlayerSide.PLAYER_TWO;
            }else{
                return tiePriority;
            }
        }

    }
}
