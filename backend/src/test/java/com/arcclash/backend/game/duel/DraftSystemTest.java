package com.arcclash.backend.game.duel;

import com.arcclash.backend.game.character.CharacterDefinition;
import com.arcclash.backend.game.character.CharacterRoster;
import com.arcclash.backend.game.character.DraftSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DraftSystemTest {

    @Test
    public void testDraftSystem() {
        List<CharacterDefinition> deck = new ArrayList<>();
        CharacterRoster roster = new CharacterRoster();

        for (int i = 1; i <= 21; i++) {
            CharacterStats stats = new CharacterStats(5, 5, 5);

            CharacterDefinition character = new CharacterDefinition(
                    "char" + i,
                    "Character " + i,
                    stats,
                    Set.of("Warrior")
            );

            deck.add(character);
        }

        DraftSystem sys = new DraftSystem(deck, roster);

        List<CharacterDefinition> offer = sys.draft();
        sys.choice(offer.get(0).getId());
        List<CharacterDefinition> offer1 = sys.draft();
        sys.choice(offer1.get(0).getId());
        List<CharacterDefinition> offer2 = sys.draft();
        sys.choice(offer2.get(0).getId());
        List<CharacterDefinition> offer3 = sys.draft();
        sys.choice(offer3.get(0).getId());
        List<CharacterDefinition> offer4 = sys.draft();
        sys.choice(offer4.get(0).getId());
        assertTrue(roster.isComplete());
        assertEquals(5,roster.getCharacters().size());
        assertThrows(IllegalStateException.class, () -> sys.draft());
    }

}
