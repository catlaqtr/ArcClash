package com.arcclash.backend.game.duel;

public class CharacterStats {
    private final int attack;
    private final int defense;
    private final int speed;

    public CharacterStats(int attack, int defense, int speed) {
        if(attack < 0){
            throw new IllegalArgumentException("attack can't be lower than 0");
        }
        if(defense < 0){
            throw new IllegalArgumentException("defense can't be lower than 0");
        }
        if(speed < 0){
            throw new IllegalArgumentException("speed can't be lower than 0");
        }
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }
}
