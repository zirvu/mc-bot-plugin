package com.zirvumcai.project;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class ZirvuBotPlugin extends JavaPlugin implements Listener {

    private ZombieManager zombieManager;  // Manager for Zombie operations

    @Override
    public void onEnable() {
        getLogger().info("ZirvuBotPlugin has been enabled!");
        // Initialize the ZombieManager
        zombieManager = new ZombieManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ZirvuBotPlugin has been disabled!");
    }

    // Event handler for player chat (e.g., "help" or "rest" command)
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        getLogger().info(player.getName() + " sent a chat message: " + message);  // Debugging message

        Bukkit.getScheduler().runTask(this, () -> { // Ensure this is run on the main thread
            // Handle the "help" command
            if (player.getName().equals("zirvu1351") && message.equalsIgnoreCase("help")) {
                getLogger().info("Player zirvu1351 sent 'help' in chat.");
                zombieManager.handleHelpCommand(player);  // Summon or teleport the zombie

            // Handle the "rest" command (only called when explicitly stated)
            } else if (player.getName().equals("zirvu1351") && message.equalsIgnoreCase("rest")) {
                getLogger().info("Player zirvu1351 sent 'rest' in chat.");
                zombieManager.removeZombieIfExists();  // Remove (kill) the zombie
            }
        });
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
            Bukkit.getScheduler().runTask(this, () -> zombieManager.removeZombieIfExists());  // Remove the zombie if the player disconnects
        }
    }
}
