package com.drkrisz.kits;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KitsPlugin extends JavaPlugin implements TabExecutor {
    private KitManager kitManager;
    private KitEditorGUI editorGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.kitManager = new KitManager(this);
        this.editorGUI = new KitEditorGUI(this, kitManager);

        // register command
        getCommand("kit").setExecutor(this);
        getCommand("kit").setTabCompleter(this);

        // register events
        Bukkit.getPluginManager().registerEvents(editorGUI, this);

        // create defaults on first run
        if (!getConfig().isConfigurationSection("kits")) {
            getLogger().info("no kits found in config, creating defaults...");
            kitManager.createDefaultKits();
            saveConfig();
        }
        getLogger().info("PaperKits enabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("only players can use this.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(Util.color("&7usage: &f/kit <name>&7, &f/kit list&7, &f/kit create&7, &f/kit edit <name>"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                List<String> names = kitManager.getKitNames();
                if (names.isEmpty()) {
                    p.sendMessage(Util.color("&cno kits yet. use &e/kit create &cto make one."));
                } else {
                    p.sendMessage(Util.color("&aavailable kits: &f" + String.join(", ", names)));
                }
                return true;
            }
            case "create" -> {
                if (!p.hasPermission("kits.create")) {
                    p.sendMessage(Util.color("&cno permission."));
                    return true;
                }
                editorGUI.openNewSession(p);
                return true;
            }
            case "edit" -> {
                if (!p.hasPermission("kits.create")) {
                    p.sendMessage(Util.color("&cno permission."));
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage(Util.color("&7usage: &f/kit edit <name>"));
                    return true;
                }
                String name = args[1].toLowerCase(Locale.ROOT);
                if (!kitManager.kitExists(name)) {
                    p.sendMessage(Util.color("&cno kit named &e" + name));
                    return true;
                }
                editorGUI.openEditSession(p, name);
                return true;
            }
            default -> {
                String kitName = sub;
                if (!kitManager.kitExists(kitName)) {
                    p.sendMessage(Util.color("&cunknown kit: &e" + kitName + "&7. try &f/kit list"));
                    return true;
                }
                if (!p.hasPermission("kits.use")) {
                    p.sendMessage(Util.color("&cno permission."));
                    return true;
                }
                int given = kitManager.giveKit(p, kitName);
                p.sendMessage(Util.color("&areceived &e" + given + " &aitems from kit &b" + kitName));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("list");
            if (sender.hasPermission("kits.create")) {
                out.add("create");
                out.add("edit");
            }
            out.addAll(kitManager.getKitNames());
            return Util.filterPrefix(out, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            out.addAll(kitManager.getKitNames());
            return Util.filterPrefix(out, args[1]);
        }
        return out;
    }
}
