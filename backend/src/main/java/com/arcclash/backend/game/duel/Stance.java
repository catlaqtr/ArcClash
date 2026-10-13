package com.arcclash.backend.game.duel;

public enum Stance {
    ATTACK,
    DEFENSE,
    SPEED;

    public boolean counters(Stance opponent ){
    if(this == ATTACK && opponent == SPEED){
        return true;
    }
    else if(this == DEFENSE && opponent == ATTACK){
        return true;
    }
    else if(this == SPEED && opponent == DEFENSE){
        return true;
    }

    return false;


    }
}
