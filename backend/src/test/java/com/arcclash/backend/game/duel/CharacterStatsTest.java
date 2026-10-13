package com.arcclash.backend.game.duel;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CharacterStatsTest {
    @Test
    public void createsValidStats(){
        CharacterStats character = new CharacterStats(5,6,7);

        assertEquals(5,character.getAttack());
        assertEquals(6,character.getDefense());
        assertEquals(7,character.getSpeed());
        assertThrows(IllegalArgumentException.class,()-> new CharacterStats(-1,6,7) );

    }
}
