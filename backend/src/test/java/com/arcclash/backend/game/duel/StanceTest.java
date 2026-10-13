package com.arcclash.backend.game.duel;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StanceTest {
    @Test
    public void stanceCounterRelationships(){
        assertTrue(Stance.ATTACK.counters(Stance.SPEED));
        assertFalse(Stance.DEFENSE.counters(Stance.SPEED));
        assertTrue(Stance.DEFENSE.counters(Stance.ATTACK));
        assertTrue(Stance.SPEED.counters(Stance.DEFENSE));
        assertFalse(Stance.SPEED.counters(Stance.ATTACK));
        assertFalse(Stance.ATTACK.counters(Stance.DEFENSE));
    }

}
