package com.arcclash.backend.game.duel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DuelResolverTest {
    @Test
    public void winner(){
        CharacterStats p1 = new CharacterStats(7,4,9);
        CharacterStats p2 = new CharacterStats(5,9,4);
        DuelResolver resolver = new DuelResolver();
        PlayerSide t1 = resolver.resolve(p1,Stance.ATTACK,p2,Stance.DEFENSE,PlayerSide.PLAYER_ONE);
        assertEquals(PlayerSide.PLAYER_TWO,t1);
    }
}
