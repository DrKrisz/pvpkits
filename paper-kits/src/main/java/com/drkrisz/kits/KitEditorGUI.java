package com.drkrisz.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitEditorGUI implements Listener {
    private final KitsPlugin plugin;
    private final KitManager kitManager;
    private final Map<UUID, KitEditorSession> sessions = new HashMap<>();

    private static final String TITLE = ChatColor.DARK_AQUA + "Kit Editor";

    public KitEditorGUI(KitsPlugin plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    public void openNewSession(Player p) {
        KitEditorSession s = new KitEditorSession(p);
        sessions.put(p.getUniqueId(), s);
        openEditorInventory(p, s);
    }

    public void openEditSession(Player p, String kitName) {
        KitEditorSession s = new KitEditorSession(p);
        s.setKitName(kitName);
        s.setDisplayName(kitManager.getKitTitle(kitName));
        List<ItemStack> items = kitManager.getKitItems(kitName);
        if (items != null) s.setItems(new ArrayList<>(items));
        sessions.put(p.getUniqueId(), s);
        openEditorInventory(p, s);
    }

    private void openEditorInventory(Player p, KitEditorSession s) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE);
        // slots 0..44 for items
        List<ItemStack> items = s.getItems();
        for (int i = 0; i < Math.min(45, items.size()); i++) inv.setItem(i, items.get(i));

        // control buttons
        inv.setItem(45, button(Material.LIME_WOOL, "&aSave"));
        inv.setItem(46, button(Material.ANVIL, "&eSet Name"));
        inv.setItem(52, button(Material.PAPER, "&7Current name: &f" + safeName(s.getKitName())));
        inv.setItem(53, button(Material.BARRIER, "&cCancel"));

        p.openInventory(inv);
    }

    private String safeName(String n) { return n == null ? "<unset>" : n; }

    private ItemStack button(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(Util.color(name));
        it.setItemMeta(m);
        return it;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(false); // allow placing items in 0..44
        int slot = e.getRawSlot();
        if (slot >= 54) return; // bottom inventory
        if (slot >= 45) {
            e.setCancelled(true);
            KitEditorSession s = sessions.get(p.getUniqueId());
            if (s == null) return;
            switch (slot) {
                case 45 -> { // save
                    // collect items from 0..44
                    List<ItemStack> list = new ArrayList<>();
                    for (int i = 0; i < 45; i++) {
                        ItemStack it = e.getView().getTopInventory().getItem(i);
                        if (it != null && it.getType() != Material.AIR) list.add(it.clone());
                    }
                    if (s.getKitName() == null || s.getKitName().isBlank()) {
                        p.sendMessage(Util.color("&cset a name first with &eSet Name"));
                        return;
                    }
                    if (list.isEmpty()) {
                        p.sendMessage(Util.color("&cadd at least one item to the kit"));
                        return;
                    }
                    s.setItems(list);
                    kitManager.saveKit(s.getKitName(), s.getItems(), s.getDisplayName());
                    p.closeInventory();
                    p.sendMessage(Util.color("&aSaved kit &b" + s.getKitName() + " &7(" + s.getDisplayName() + ")"));
                    sessions.remove(p.getUniqueId());
                }
                case 46 -> { // name
                    p.closeInventory();
                    KitEditorSession ses = sessions.get(p.getUniqueId());
                    if (ses != null) ses.setState(KitEditorSession.State.NAMING);
                    p.sendMessage(Util.color("&eType the kit name in chat. &7(only letters, digits and - _)")); 
                }
                case 53 -> { // cancel
                    p.closeInventory();
                    p.sendMessage(Util.color("&7canceled kit editing."));
                    sessions.remove(p.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        // keep session, do nothing on close to allow chat naming
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        KitEditorSession s = sessions.get(p.getUniqueId());
        if (s == null) return;
        if (s.getState() != KitEditorSession.State.NAMING) return;

        e.setCancelled(true);
        String raw = e.getMessage().trim();
        String key = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
        if (key.isBlank()) {
            p.sendMessage(Util.color("&cinvalid name. try again."));
            return;
        }
        s.setKitName(key);
        s.setDisplayName(raw);
        s.setState(KitEditorSession.State.EDITING);

        Bukkit.getScheduler().runTask(plugin, () -> {
            p.sendMessage(Util.color("&aSet name: &f" + raw + " &7(key: " + key + ")"));
            openEditorInventory(p, s);
        });
    }
}
