package com.arcclash.backend.game.character;

import java.util.*;

public class DraftSystem {
    private final List<CharacterDefinition> deck;
    private final List<CharacterDefinition> offer;
    private final Random random = new Random();
    private CharacterRoster roster;
    private final List<CharacterDefinition> picked;

    public DraftSystem(List<CharacterDefinition> deck, CharacterRoster roster) {
        if (deck == null || roster == null) {
            throw new IllegalArgumentException("Deck and roster cannot be null");
        }
        if(deck.size() != 21){
            throw new IllegalArgumentException("deck size is wrong");
        }
        Set<String> ids = new HashSet<>();
        for (CharacterDefinition character : deck) {
            if (character == null || !ids.add(character.getId())) {
                throw new IllegalArgumentException("Invalid or duplicate character");
            }
        }
        this.deck = List.copyOf(deck);
        offer = new ArrayList<>();
        picked = new ArrayList<>();
        this.roster = roster;
    }
    public void startNewSet(CharacterRoster newRoster){

        if(!offer.isEmpty()){
            throw new IllegalStateException("offer is not empty");
        }
        if(!roster.isComplete()){
            throw new IllegalStateException("roster is full");
        }
        if (newRoster == null || !newRoster.getCharacters().isEmpty()) {
            throw new IllegalArgumentException("New roster must be empty and not null");
        }
        roster = newRoster;
    }
    public void choice(String characterId){
        for(int i=0;i< offer.size();i++){
            if(offer.get(i).getId().equals(characterId)){
                roster.addCharacter(offer.get(i));
                picked.add(offer.get(i));
                offer.clear();
                return;
            }
        }
        throw new IllegalArgumentException("choice is not in the offer");
    }

    public List<CharacterDefinition> draft(){

        if(!offer.isEmpty()){
            throw new IllegalStateException("offer is not empty");
        }
        if(roster.isComplete()){
            throw new IllegalStateException("roster is full");
        }
        List<CharacterDefinition> temp = new ArrayList<>(deck);
        for (CharacterDefinition selected : picked) {
            for(int i=0;i< temp.size();i++){
                if(temp.get(i).getId().equals(selected.getId())){
                    temp.remove(i);
                    break;
                }
            }
        }

            int offer1 = random.nextInt(temp.size()) ;
            offer.add(temp.get(offer1));
            temp.remove(offer1);
            int offer2 = random.nextInt(temp.size()) ;
        offer.add(temp.get(offer2));
        temp.remove(offer2);
            int offer3 = random.nextInt(temp.size()) ;
        offer.add(temp.get(offer3));
        temp.remove(offer3);

        return List.copyOf(offer);





    }
}
