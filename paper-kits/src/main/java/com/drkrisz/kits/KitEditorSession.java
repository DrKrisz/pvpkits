package com.drkrisz.kits;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitEditorSession {
    public enum State { NAMING, EDITING }

    private final Player player;
    private String kitName; // internal name key
    private String displayName; // pretty title
    private List<ItemStack> items = new ArrayList<>();
    private State state = State.EDITING;

    public KitEditorSession(Player player) { this.player = player; }

    public Player getPlayer() { return player; }

    public String getKitName() { return kitName; }

    public void setKitName(String kitName) { this.kitName = kitName; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<ItemStack> getItems() { return items; }

    public void setItems(List<ItemStack> items) { this.items = items; }

    public State getState() { return state; }

    public void setState(State state) { this.state = state; }
}
