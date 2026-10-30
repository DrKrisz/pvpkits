package com.drkrisz.kits;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KitPermissionsTest {
    @Test
    void packagedDescriptorRestrictsEditingToOperatorsByDefault() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(input);
            PluginDescriptionFile descriptor = new PluginDescriptionFile(input);
            for (String node : new String[]{"kits.create", "kits.admin"}) {
                Permission permission = descriptor.getPermissions().stream()
                        .filter(p -> p.getName().equals(node)).findFirst().orElseThrow();
                assertEquals(PermissionDefault.OP, permission.getDefault());
                assertFalse(permission.getDefault().getValue(false), node + " for ordinary players");
                assertTrue(permission.getDefault().getValue(true), node + " for operators");
            }
            Permission use = descriptor.getPermissions().stream()
                    .filter(p -> p.getName().equals("kits.use")).findFirst().orElseThrow();
            assertTrue(use.getDefault().getValue(false));
        }
    }

    @Test
    void revokedEditorPermissionBlocksSaveAndRemovesSession() throws Exception {
        KitManager manager = mock(KitManager.class);
        KitEditorGUI gui = new KitEditorGUI(null, manager);
        Player player = mock(Player.class);
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPermission("kits.create")).thenReturn(false);
        Map<UUID, KitEditorSession> sessions = sessions(gui);
        sessions.put(id, new KitEditorSession(player));
        InventoryClickEvent event = editorClick(player);

        gui.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(event, never()).setCancelled(false);
        verify(player, never()).closeInventory();
        assertFalse(sessions.containsKey(id));
        verifyNoInteractions(manager);
    }

    @Test
    void matchingTitleWithoutAnEditorSessionCannotSave() throws Exception {
        KitManager manager = mock(KitManager.class);
        KitEditorGUI gui = new KitEditorGUI(null, manager);
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.hasPermission("kits.create")).thenReturn(true);
        InventoryClickEvent event = editorClick(player);

        gui.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(event, never()).setCancelled(false);
        verifyNoInteractions(manager);
    }

    @Test
    void authorizedEditorCanStillInteractWithItemSlots() throws Exception {
        KitManager manager = mock(KitManager.class);
        KitEditorGUI gui = new KitEditorGUI(null, manager);
        Player player = mock(Player.class);
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPermission("kits.create")).thenReturn(true);
        sessions(gui).put(id, new KitEditorSession(player));
        InventoryClickEvent event = editorClick(player);
        when(event.getRawSlot()).thenReturn(0);

        gui.onInventoryClick(event);

        verify(event).setCancelled(false);
        verify(player, never()).closeInventory();
        assertTrue(sessions(gui).containsKey(id));
    }

    private InventoryClickEvent editorClick(Player player) {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        InventoryView view = mock(InventoryView.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getView()).thenReturn(view);
        when(view.getTopInventory()).thenReturn(mock(Inventory.class));
        when(view.getTitle()).thenReturn(ChatColor.DARK_AQUA + "Kit Editor");
        when(event.getRawSlot()).thenReturn(45); // Save button
        return event;
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, KitEditorSession> sessions(KitEditorGUI gui) throws Exception {
        Field field = KitEditorGUI.class.getDeclaredField("sessions");
        field.setAccessible(true);
        return (Map<UUID, KitEditorSession>) field.get(gui);
    }
}
