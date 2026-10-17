package com.arcclash.backend.game.magic;


import java.util.*;

public class MagicDraftSystem {
    private final List<MagicCardDefinition> magicDeck;
    private final List<MagicCardDefinition> offer;
    private final Random random = new Random();
    private MagicHand hand;
    private final List<MagicCardDefinition> picked;

    public MagicDraftSystem(List<MagicCardDefinition> magicDeck, MagicHand hand) {
        if (magicDeck == null || hand == null) {
            throw new IllegalArgumentException("magicDeck and hand cannot be null");
        }
        if(magicDeck.size() != 9){
            throw new IllegalArgumentException("magicDeck size is wrong");
        }
        Set<String> ids = new HashSet<>();
        for (MagicCardDefinition magic : magicDeck) {
            if (magic == null || !ids.add(magic.getId())) {
                throw new IllegalArgumentException("Invalid or duplicate magic");
            }
        }
        this.magicDeck = List.copyOf(magicDeck);
        offer = new ArrayList<>();
        this.hand = hand;
        picked = new ArrayList<>();
    }
    public void startNewSet(MagicHand newHand){
        if(!offer.isEmpty()){
            throw new IllegalStateException("offer is not empty");
        }
        if(!hand.isComplete()){
            throw new IllegalStateException("hand is full");
        }
        if (newHand == null || !newHand.getMagicCards().isEmpty()) {
            throw new IllegalArgumentException("New hand must be empty and not null");
        }
        hand = newHand;
    }


    public void choice(String characterId){
        for(int i=0;i< offer.size();i++){
            if(offer.get(i).getId().equals(characterId)){
                hand.addMagic(offer.get(i));
                picked.add(offer.get(i));
                offer.clear();
                return;
            }
        }
        throw new IllegalArgumentException("choice is not in the offer");
    }

    public List<MagicCardDefinition> draft(){

        if(!offer.isEmpty()){
            throw new IllegalStateException("offer is not empty");
        }
        if(hand.isComplete()){
            throw new IllegalStateException("hand is full");
        }
        List<MagicCardDefinition> temp = new ArrayList<>(magicDeck);
        for (MagicCardDefinition selected : picked) {
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

        return List.copyOf(offer);





    }

}
