package com.captainbboy.harvesterhoes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHandler {

    private Map<UUID, Double> amountEarned = new HashMap<>();
    private Map<UUID, Double> amountEssenceEarned = new HashMap<>();

    public void addToEarned(UUID playerUUID, Double amountToAdd) {
        if(this.amountEarned.containsKey(playerUUID)) {
            Double newValue = this.amountEarned.get(playerUUID) + amountToAdd;
            this.amountEarned.replace(playerUUID, newValue);
        } else {
            this.amountEarned.put(playerUUID, amountToAdd);
        }
    }

    public void clearAmountEarned() {
        this.amountEarned.clear();
    }

    public Map<UUID, Double> getAmountEarned() {
        return this.amountEarned;
    }

}
