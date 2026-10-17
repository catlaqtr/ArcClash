package com.arcclash.backend.game.magic;

import java.util.ArrayList;
import java.util.List;

public class MagicHand {
    private final List<MagicCardDefinition> magicCards = new ArrayList<>();
    private final List<String> playedMagicIds = new ArrayList<>();


    public void addMagic(MagicCardDefinition magic) {
        if (magic == null) {
            throw new IllegalArgumentException("magic cant be null");
        }
        for (MagicCardDefinition ext : magicCards) {
            if (ext.getId().equals(magic.getId())) {
                throw new IllegalArgumentException("there cant be a duplicate");
            }

        }
        if (magicCards.size() == 2) {
            throw new IllegalStateException("hand is 2");
        }
        magicCards.add(magic);
    }

    public List<MagicCardDefinition> getMagicCards() {
        return List.copyOf(magicCards);
    }

    public boolean isComplete() {
        return magicCards.size() == 2;
    }

    public void playMagic(String magicId) {
        if (isComplete() == false) {
            throw new IllegalStateException("hand is not 2");
        }
        boolean found = false;
        for (MagicCardDefinition ext : magicCards) {
            if (ext.getId().equals(magicId)) {
                found = true;
            }
        }
        if(found == false){
            throw new IllegalArgumentException("magic not in the hand");
        }
        if(playedMagicIds.contains(magicId)){
            throw new IllegalStateException("already played");
        }
        playedMagicIds.add(magicId);

    }

    public List<String> getPlayedMagicIds() {
        return List.copyOf(playedMagicIds);
    }


    public MagicCardDefinition getAvailableMagic(String magicId){
        if(!isComplete()){
            throw new IllegalStateException("hand is not 2");
        }
        if(playedMagicIds.contains(magicId)){
            throw new IllegalStateException("it is already played");
        }
        for (MagicCardDefinition magic : magicCards) {
            if (magic.getId().equals(magicId)) {
                return magic;
            }
        }
        throw new IllegalArgumentException("magic not found in hand");
    }
}
