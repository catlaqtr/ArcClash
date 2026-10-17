package com.arcclash.backend.game.duel;

import com.arcclash.backend.game.character.CharacterDefinition;
import com.arcclash.backend.game.character.CharacterRoster;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DuelSetEngineTest {
    @Test
    public void playDuel(){
        DuelSetEngine duel = new DuelSetEngine();
        DuelSet se= new DuelSet();
        CharacterRoster roster = new CharacterRoster();
        CharacterRoster roster2 = new CharacterRoster();
        CharacterStats stats1 = new CharacterStats(1,2,3);
        CharacterStats stats2 = new CharacterStats(1,5,2);
        CharacterDefinition char1 = new CharacterDefinition("1","Mete",stats1, Set.of("Pirate"));
        DuelChoice choice1 = new DuelChoice("1",Stance.ATTACK);
        DuelChoice choice2 = new DuelChoice("2",Stance.DEFENSE);
        CharacterDefinition char2 = new CharacterDefinition("2","Ne",stats2, Set.of("Warrior"));
        roster.addCharacter(char1);
        roster2.addCharacter(char2);
        CharacterDefinition char3 = new CharacterDefinition("3","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char4 = new CharacterDefinition("4","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char5 = new CharacterDefinition("5","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char6 = new CharacterDefinition("6","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char8 = new CharacterDefinition("7","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char9 = new CharacterDefinition("8","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char10 = new CharacterDefinition("9","Ne",stats2, Set.of("Warrior"));
        CharacterDefinition char7 = new CharacterDefinition("10","Ne",stats2, Set.of("Warrior"));
        roster.addCharacter(char3);
        roster.addCharacter(char4);
        roster.addCharacter(char5);
        roster.addCharacter(char10);
        roster2.addCharacter(char6);
        roster2.addCharacter(char7);
        roster2.addCharacter(char8);
        roster2.addCharacter(char9);


        PlayerSide theduel = duel.playDuel(se,roster,choice1,roster2,choice2,PlayerSide.PLAYER_ONE);
        assertEquals(PlayerSide.PLAYER_TWO,theduel);
        assertTrue(roster.getPlayedCharacterIds().contains("1"));
        assertTrue(roster2.getPlayedCharacterIds().contains("2"));
        assertEquals(0, se.getPlayerOneWins());
        assertEquals(1, se.getPlayerTwoWins());
    }
}
