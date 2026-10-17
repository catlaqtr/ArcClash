package com.arcclash.backend.game.duel;

public class DuelChoice {
    private final String characterId;
    private final Stance stance;

    public DuelChoice(String characterId, Stance stance) {
        if( characterId == null || characterId.isBlank()){
            throw new IllegalArgumentException("id cant be blank or null");
        }
        if(stance == null){
            throw new IllegalArgumentException("stance cant be null");
        }
        this.characterId = characterId;
        this.stance = stance;
    }

    public String getCharacterId() {
        return characterId;
    }

    public Stance getStance() {
        return stance;
    }
}
