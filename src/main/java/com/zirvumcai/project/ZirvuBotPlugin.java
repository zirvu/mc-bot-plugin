package com.zirvumcai.project;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ZirvuBotPlugin extends JavaPlugin implements Listener {

    private VillagerManager villagerManager;  // Manager for Villager operations

    @Override
    public void onEnable() {
        getLogger().info("ZirvuBotPlugin has been enabled!");
        // Initialize the VillagerManager
        villagerManager = new VillagerManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ZirvuBotPlugin has been disabled!");
    }

    // Event handler for player chat (e.g., "help" message triggers the NPC summon)
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        getLogger().info(player.getName() + " sent a chat message: " + message);  // Debugging message

        if (player.getName().equals("zirvu1351") && message.equalsIgnoreCase("help")) {
            getLogger().info("Player zirvu1351 sent 'help' in chat.");

            // Use the villagerManager to handle the NPC logic
            villagerManager.handleHelpCommand(player);
        }
    }

    // Handle when a player quits or gets kicked
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handlePlayerDisconnect(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        handlePlayerDisconnect(event.getPlayer());
    }

    // Handle player disconnection and remove the NPC if needed
    private void handlePlayerDisconnect(Player player) {
        if (player.getName().equals("zirvu1351")) {
            villagerManager.removeVillagerIfExists();
        }
    }
}
