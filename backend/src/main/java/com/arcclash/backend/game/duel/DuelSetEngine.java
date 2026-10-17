package com.arcclash.backend.game.duel;

import com.arcclash.backend.game.character.CharacterDefinition;
import com.arcclash.backend.game.character.CharacterRoster;

public class DuelSetEngine {
    private final DuelResolver resolver = new DuelResolver();

    public PlayerSide playDuel(    DuelSet set,
                                   CharacterRoster firstRoster,
                                   DuelChoice firstChoice,
                                   CharacterRoster secondRoster,
                                   DuelChoice secondChoice,
                                   PlayerSide tiePriority){
        if(firstRoster == secondRoster){
            throw new IllegalArgumentException("cant be same");
        }
    if(set.isFinished()){
        throw new IllegalStateException("game is over");
    }
    CharacterDefinition firstCharacter = firstRoster.getAvailableCharacter(firstChoice.getCharacterId());
    CharacterDefinition secondCharacter = secondRoster.getAvailableCharacter(secondChoice.getCharacterId());
    PlayerSide winner = resolver.resolve(firstCharacter.getStats(), firstChoice.getStance(), secondCharacter.getStats(),secondChoice.getStance(), tiePriority);
    set.recordWin(winner);
        firstRoster.playCharacter(firstChoice.getCharacterId());
        secondRoster.playCharacter(secondChoice.getCharacterId());
    return winner;

    }
}
