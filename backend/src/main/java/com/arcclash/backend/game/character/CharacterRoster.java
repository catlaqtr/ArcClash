package com.arcclash.backend.game.character;

import java.util.ArrayList;
import java.util.List;

public class CharacterRoster {
    private final List<CharacterDefinition> characters = new ArrayList<>();
    private final List<String> playedCharacterIds = new ArrayList<>();



    public void addCharacter(CharacterDefinition character) {
        if (character == null) {
            throw new IllegalArgumentException("character cant be null");
        }
        for (CharacterDefinition ext : characters) {
            if (ext.getId().equals(character.getId())) {
                throw new IllegalArgumentException("there cant be a duplicate");
            }

        }
        if (characters.size() == 5) {
            throw new IllegalStateException("team is 5");
        }
        characters.add(character);
    }

    public List<CharacterDefinition> getCharacters() {
        return List.copyOf(characters);
    }

    public boolean isComplete() {
        return characters.size() == 5;
    }

    public void playCharacter(String characterId) {
        if (isComplete() == false) {
            throw new IllegalStateException("roster is not 5");
        }
        boolean found = false;
        for (CharacterDefinition ext : characters) {
            if (ext.getId().equals(characterId)) {
                found = true;
            }
        }
            if(found == false){
                throw new IllegalArgumentException("character not in the roster");
            }
            if(playedCharacterIds.contains(characterId)){
                throw new IllegalStateException("already played");
            }
            playedCharacterIds.add(characterId);

    }

        public List<String> getPlayedCharacterIds () {
            return List.copyOf(playedCharacterIds);
        }


    public CharacterDefinition getAvailableCharacter(String characterId){
        if(!isComplete()){
            throw new IllegalStateException("squad is not 5");
        }
        if(playedCharacterIds.contains(characterId)){
            throw new IllegalStateException("it is already played");
        }
        for (CharacterDefinition character : characters) {
            if (character.getId().equals(characterId)) {
                return character;
            }
        }
        throw new IllegalArgumentException("Character not found in roster");
    }


    }
