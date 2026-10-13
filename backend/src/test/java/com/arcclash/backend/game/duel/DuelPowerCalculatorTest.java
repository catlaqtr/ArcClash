package com.arcclash.backend.game.duel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DuelPowerCalculatorTest {
    @Test
    public void calculatorTest() {
        CharacterStats character = new CharacterStats(5, 6, 7);
        DuelPowerCalculator cal = new DuelPowerCalculator();

        assertEquals(23,cal.calculate(character,Stance.ATTACK,Stance.DEFENSE));
        assertEquals(28,cal.calculate(character,Stance.DEFENSE,Stance.ATTACK));
        assertEquals(25,cal.calculate(character,Stance.SPEED,Stance.ATTACK));
    }
}
