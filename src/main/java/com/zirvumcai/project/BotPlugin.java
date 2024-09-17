package com.zirvumcai.project;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BotPlugin extends JavaPlugin implements Listener {

    private UUID golemUUID;  // Unique identifier for the golem

    @Override
    public void onEnable() {
        getLogger().info("BotPlugin has been enabled!");
        // Register the event listener
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("BotPlugin has been disabled!");
    }

    // Event handler for player join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equals("zirvu1351")) {  // Replace with your player name if needed
            // Try to find the existing golem using the stored UUID
            IronGolem existingGolem = findExistingGolem();
            if (existingGolem == null || !existingGolem.isValid()) {
                // No valid golem found, spawn a new one
                startFollowingPlayer(player);
            } else {
                getLogger().info("The golem is already following the player.");
                // Start following the player with the existing golem
                startFollowingWithExistingGolem(existingGolem, player);
            }
        }
    }

    // Method to find the existing golem in the world using the stored UUID
    private IronGolem findExistingGolem() {
        if (golemUUID != null) {
            for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                if (entity instanceof IronGolem && entity.getUniqueId().equals(golemUUID)) {
                    return (IronGolem) entity;  // Return the existing golem
                }
            }
        }
        return null;  // No golem found
    }

    // Method to spawn a new golem and make it follow the player
    public void startFollowingPlayer(Player player) {
        // Spawn a new Iron Golem
        IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
        golemUUID = golem.getUniqueId();  // Store the golem's unique ID

        // Make the golem invincible
        golem.setInvulnerable(true);
        golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048);  // Large health value
        golem.setHealth(2048);  // Set current health to the max

        // Start following the player smoothly
        startFollowingWithExistingGolem(golem, player);
    }

    // Method to make the existing or new golem follow the player smoothly
    public void startFollowingWithExistingGolem(IronGolem golem, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player != null && golem != null && !golem.isDead() && player.isOnline()) {
                    Location playerLocation = player.getLocation();
                    Location golemLocation = golem.getLocation();

                    // Calculate the distance between the golem and the player
                    double distance = golemLocation.distance(playerLocation);

                    // Move the golem toward the player if it's more than 2 blocks away
                    if (distance > 2.0) {
                        // Calculate the direction vector towards the player
                        Vector direction = playerLocation.toVector().subtract(golemLocation.toVector()).normalize();

                        // Multiply the vector by a small value to move the golem smoothly toward the player
                        golem.setVelocity(direction.multiply(0.3));  // Adjust this value to control movement speed
                    } else {
                        // Stop the golem when close to the player
                        golem.setVelocity(new Vector(0, 0, 0));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L); // Run every tick (20 times per second) for smooth movement
    }
}
