package com.arcclash.backend.game.duel;

public class DuelPowerCalculator {

    public int calculate(CharacterStats stats, Stance ownStance, Stance opponentStance){
        int duel = stats.getAttack() + stats.getDefense()+ stats.getSpeed();
        switch(ownStance){
            case Stance.ATTACK:
                duel += stats.getAttack();
                break;
            case Stance.DEFENSE:
                duel += stats.getDefense();
                break;
            case Stance.SPEED:
                duel += stats.getSpeed();
                break;

        }

        if(ownStance.counters(opponentStance)){
            duel=duel +4;
        }
        return duel;
    }
}
