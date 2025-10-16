package com.drkrisz.kits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class KitManager {
    private final KitsPlugin plugin;

    public KitManager(KitsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean kitExists(String name) {
        return plugin.getConfig().isList("kits." + name + ".items");
    }

    public List<String> getKitNames() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("kits");
        if (section == null) return new ArrayList<>();
        return new ArrayList<>(section.getKeys(false));
    }

    public int giveKit(Player p, String name) {
        List<ItemStack> items = getKitItems(name);
        if (items == null || items.isEmpty()) return 0;
        PlayerInventory inv = p.getInventory();
        int given = 0;
        for (ItemStack it : items) {
            if (it == null || it.getType() == Material.AIR) continue;
            HashMap<Integer, ItemStack> left = inv.addItem(it.clone());
            given += it.getAmount();
            // drop leftovers if inventory is full
            if (!left.isEmpty()) {
                for (ItemStack drop : left.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), drop);
                }
            }
        }
        return given;
    }

    @SuppressWarnings("unchecked")
    public List<ItemStack> getKitItems(String name) {
        return (List<ItemStack>) plugin.getConfig().getList("kits." + name + ".items");
    }

    public void saveKit(String name, List<ItemStack> items, String displayName) {
        // strip nulls and air
        List<ItemStack> clean = new ArrayList<>();
        for (ItemStack it : items) {
            if (it == null || it.getType() == Material.AIR) continue;
            clean.add(it);
        }
        FileConfiguration cfg = plugin.getConfig();
        String base = "kits." + name + ".";
        cfg.set(base + "items", clean);
        cfg.set(base + "title", displayName == null ? name : displayName);
        plugin.saveConfig();
    }

    public String getKitTitle(String name) {
        return plugin.getConfig().getString("kits." + name + ".title", name);
    }

    public void createDefaultKits() {
        // starter: leather armor, wooden sword, bread
        List<ItemStack> starter = List.of(
                Util.item(Material.LEATHER_HELMET, 1),
                Util.item(Material.LEATHER_CHESTPLATE, 1),
                Util.item(Material.LEATHER_LEGGINGS, 1),
                Util.item(Material.LEATHER_BOOTS, 1),
                Util.item(Material.WOODEN_SWORD, 1),
                Util.item(Material.SHIELD, 1),
                Util.item(Material.BREAD, 16)
        );
        saveKit("starter", starter, "Starter");

        // mid: iron armor, stone sword, steak
        List<ItemStack> mid = List.of(
                Util.item(Material.IRON_HELMET, 1),
                Util.item(Material.IRON_CHESTPLATE, 1),
                Util.item(Material.IRON_LEGGINGS, 1),
                Util.item(Material.IRON_BOOTS, 1),
                Util.item(Material.STONE_SWORD, 1),
                Util.item(Material.COOKED_BEEF, 16)
        );
        saveKit("mid", mid, "Mid");

        // pro: diamond armor, iron sword, golden apples
        List<ItemStack> pro = List.of(
                Util.item(Material.DIAMOND_HELMET, 1),
                Util.item(Material.DIAMOND_CHESTPLATE, 1),
                Util.item(Material.DIAMOND_LEGGINGS, 1),
                Util.item(Material.DIAMOND_BOOTS, 1),
                Util.item(Material.IRON_SWORD, 1),
                Util.item(Material.GOLDEN_APPLE, 4)
        );
        saveKit("pro", pro, "Pro");
    }
}
