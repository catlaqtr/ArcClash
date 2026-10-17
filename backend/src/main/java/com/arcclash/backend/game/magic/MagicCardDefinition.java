package com.arcclash.backend.game.magic;


public class MagicCardDefinition {

    private final String id;
    private final String name;

    public MagicCardDefinition(String id, String name) {
        if(id == null || id.isBlank()){
            throw new IllegalArgumentException("id cant be null or blank");
        }
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("name cant be null or blank");
        }
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
