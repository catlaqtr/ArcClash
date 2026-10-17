package com.arcclash.backend.game.duel;

import com.arcclash.backend.game.magic.MagicCardDefinition;
import com.arcclash.backend.game.magic.MagicHand;
import com.arcclash.backend.game.magic.MagicDraftSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MagicSystemTest {
    @Test
    public void MagicDraftSystem() {
        List<MagicCardDefinition> deck = new ArrayList<>();
        MagicHand roster = new MagicHand();

        for (int i = 1; i <= 9; i++) {
            CharacterStats stats = new CharacterStats(5, 5, 5);

            MagicCardDefinition magic = new MagicCardDefinition(
                    "magic" + i,
                    "magic " + i
            );

            deck.add(magic);
        }

        MagicDraftSystem sys = new MagicDraftSystem(deck, roster);

        List<MagicCardDefinition> offer = sys.draft();
        sys.choice(offer.get(0).getId());
        List<MagicCardDefinition> offer1 = sys.draft();
        sys.choice(offer1.get(0).getId());
        MagicHand secondHand = new MagicHand();
        assertThrows(IllegalStateException.class, () -> sys.draft());
        sys.startNewSet(secondHand);
        assertTrue(roster.isComplete());
        assertEquals(2,roster.getMagicCards().size());

    }
}
