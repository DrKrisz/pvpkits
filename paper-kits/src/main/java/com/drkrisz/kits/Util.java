package com.drkrisz.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Util {
    public static String color(String msg) { return ChatColor.translateAlternateColorCodes('&', msg); }

    public static ItemStack item(Material m, int amount) {
        ItemStack it = new ItemStack(m);
        it.setAmount(Math.max(1, amount));
        return it;
    }

    public static List<String> filterPrefix(List<String> list, String prefix) {
        if (prefix == null || prefix.isEmpty()) return list;
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : list) if (s.toLowerCase(Locale.ROOT).startsWith(p)) out.add(s);
        return out;
    }
}
