package com.arcclash.backend.game.character;

import com.arcclash.backend.game.duel.CharacterStats;

import java.util.Set;

public class CharacterDefinition {
    private final String id;
    private final String name;
    private final CharacterStats stats;
    private final Set<String> factions;

    public CharacterDefinition(String id, String name, CharacterStats stats, Set<String> factions) {
        if(id == null || id.isBlank()){
            throw new IllegalArgumentException("id cant be null or blank");
        }
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("name cant be null or blank");
        }
        if(stats == null){
            throw new IllegalArgumentException("stats cant be null");
        }
        if(factions == null || factions.isEmpty()){
            throw new IllegalArgumentException("factions cant be null or blank");
        }
        this.id = id;
        this.name = name;
        this.stats = stats;
        this.factions = Set.copyOf(factions);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CharacterStats getStats() {
        return stats;
    }

    public Set<String> getFactions() {
        return factions;
    }
}
