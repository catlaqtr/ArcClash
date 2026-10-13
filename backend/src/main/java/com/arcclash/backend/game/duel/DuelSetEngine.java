package com.arcclash.backend.game.duel;

public class DuelSetEngine {
    DuelResolver resolver = new DuelResolver();
    public PlayerSide playDuel(DuelSet set, CharacterStats firstStats, Stance firstStance, CharacterStats secondStats, Stance secondStance, PlayerSide tiePriority){
    if(set.isFinished()){
        throw new IllegalStateException("game is over");
    }
    PlayerSide winner = resolver.resolve(firstStats, firstStance, secondStats, secondStance, tiePriority);
    set.recordWin(winner);
    return winner;

    }
}
