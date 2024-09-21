package com.zirvumcai.project;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;

public class ZirvuBotPlugin extends JavaPlugin implements Listener {

    private ZombieManager zombieManager;

    @Override
    public void onEnable() {
        getLogger().info("ZirvuBotPlugin has been enabled!");

        zombieManager = new ZombieManager(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ZirvuBotPlugin has been disabled!");
    }

    // Handle player chat events to trigger commands
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        // if (player.getName().equals("zirvu1351") && message.equals("help")) {
        //     zombieManager.handleHelpCommand(player);
        // } else if (player.getName().equals("zirvu1351") && message.equals("rest")) {
        //     zombieManager.removeZombieIfExists();
        // } else if (player.getName().equals("zirvu1351") && message.equals("hold")) {
        //     zombieManager.handleHoldCommand(player);
        // } else if (player.getName().equals("zirvu1351") && message.equals("release")) {
        //     zombieManager.handleReleaseCommand(player);
        // }

        if (message.equals("help")) {
            zombieManager.handleHelpCommand(player);
        } else if (message.equals("rest")) {
            zombieManager.removeZombieIfExists();
        } else if (message.equals("hold")) {
            zombieManager.handleHoldCommand(player);
        } else if (message.equals("release")) {
            zombieManager.handleReleaseCommand(player);
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
        // if (player.getName().equals("zirvu1351")) {
            // Bukkit.getScheduler().runTask(this, () -> zombieManager.removeZombieIfExists());  // Remove the zombie if the player disconnects
        // }
        zombieManager.handleHoldCommand(player);
    }

}
