package com.zirvumcai.project;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class ZombieManager implements Listener {

    private final JavaPlugin plugin;
    private final DataHandler dataHandler;

    public ZombieManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataHandler = new DataHandler(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);  // Register event listeners
    }

    // Handle the "help" command and summon or teleport the NPC
    public void handleHelpCommand(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID storedUUID = dataHandler.getStoredZombieUUID();
            Zombie existingZombie = findZombieByUUID(storedUUID);

            if (existingZombie == null || !existingZombie.isValid() || existingZombie.isDead()) {
                plugin.getLogger().info("No valid NPC found. Summoning a new one.");
                summonZombie(player);  // Summon the baby zombie on the main thread
            } else {
                plugin.getLogger().info("NPC found. Teleporting to player.");
                existingZombie.teleport(player.getLocation());  // Teleport on the main thread
            }
        });
    }

    // Summon a new Baby Zombie NPC with a full set of enchanted diamond armor
    private void summonZombie(Player player) {
        Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        zombie.setCustomName("Guardian");  // Give it a name
        zombie.setCustomNameVisible(true);
        zombie.setBaby(true);  // Make it a baby zombie
        zombie.setInvulnerable(true);
        zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048);
        zombie.setHealth(2048);
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));  // Prevent burning in sunlight

        // Equip the zombie with a diamond sword and fire aspect
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2);  // Fire Aspect level 2
        sword.addEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 5);    // Sharpness V
        zombie.getEquipment().setItemInMainHand(sword);
        zombie.getEquipment().setItemInMainHandDropChance(0);  // Prevent dropping the sword

        // Equip the zombie with full enchanted diamond armor
        equipZombieWithDiamondArmor(zombie);

        // Save the NPC's UUID to the data file
        dataHandler.saveZombieUUID(zombie.getUniqueId());

        // Start the task to either attack mobs or follow the player
        startTaskToFollowOrAttack(zombie, player);
    }

    // Equip the Baby Zombie with a full set of diamond armor
    private void equipZombieWithDiamondArmor(Zombie zombie) {
        // Diamond helmet
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4);  // Protection IV
        helmet.addEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3);  // Unbreaking III
        helmet.addEnchantment(org.bukkit.enchantments.Enchantment.AQUA_AFFINITY, 1);  // Aqua Affinity
        zombie.getEquipment().setHelmet(helmet);
        zombie.getEquipment().setHelmetDropChance(0);  // Prevent helmet from dropping

        // Diamond chestplate
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4);  // Protection IV
        chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3);  // Unbreaking III
        chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.THORNS, 3);  // Thorns III
        zombie.getEquipment().setChestplate(chestplate);
        zombie.getEquipment().setChestplateDropChance(0);  // Prevent chestplate from dropping

        // Diamond leggings
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4);  // Protection IV
        leggings.addEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3);  // Unbreaking III
        zombie.getEquipment().setLeggings(leggings);
        zombie.getEquipment().setLeggingsDropChance(0);  // Prevent leggings from dropping

        // Diamond boots (without Frost Walker)
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4);  // Protection IV
        boots.addEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3);  // Unbreaking III
        boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3);  // Depth Strider III
        zombie.getEquipment().setBoots(boots);
        zombie.getEquipment().setBootsDropChance(0);  // Prevent boots from dropping
    }

    // Prevent the zombie from attacking the player by listening to EntityTargetEvent
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Zombie) {
            Zombie zombie = (Zombie) event.getEntity();
            if (zombie.getCustomName() != null && zombie.getCustomName().equals("Guardian")) {
                if (event.getTarget() instanceof Player) {
                    event.setCancelled(true);  // Cancel targeting the player
                }
            }
        }
    }

    // Find an existing Zombie by UUID
    private Zombie findZombieByUUID(UUID uuid) {
        if (uuid == null) return null;
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (entity instanceof Zombie && entity.getUniqueId().equals(uuid)) {
                return (Zombie) entity;
            }
        }
        return null;
    }

    // Start the task to either attack nearby mobs or follow the player
    private void startTaskToFollowOrAttack(Zombie npc, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player != null && npc != null && !npc.isDead() && player.isOnline()) {
                    // Check the distance between the protector and the player
                    double distanceToPlayer = npc.getLocation().distance(player.getLocation());

                    // If the protector is too far from the player (e.g., 7 blocks away), teleport to the player
                    if (distanceToPlayer > 7.0) {
                        npc.teleport(player.getLocation());
                        return;
                    }

                    // Scan for nearby hostile mobs based on the player's location
                    List<Entity> nearbyEntities = player.getNearbyEntities(10, 5, 10);  // Scan 10 blocks around the player
                    boolean foundHostileMob = false;

                    for (Entity entity : nearbyEntities) {
                        if (isHostileMob(entity)) {  // Attack only hostile mobs
                            foundHostileMob = true;
                            attackHostileMob(npc, (Mob) entity);  // Attack hostile mob
                            break;
                        }
                    }

                    // If no hostile mobs are found, follow the player
                    if (!foundHostileMob) {
                        followPlayer(npc, player);  // Follow player if no mobs to attack
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    // Check if the entity is a hostile mob (Zombies, Skeletons, Creepers, etc.)
    private boolean isHostileMob(Entity entity) {
        return entity.getType() == EntityType.ZOMBIE || 
               entity.getType() == EntityType.SKELETON ||
               entity.getType() == EntityType.CREEPER ||
               entity.getType() == EntityType.SPIDER ||
               entity.getType() == EntityType.ENDERMAN ||  
               entity.getType() == EntityType.PILLAGER || 
               entity.getType() == EntityType.HUSK || 
               entity.getType() == EntityType.STRAY ||
               entity.getType() == EntityType.WITCH;
    }

    // Make the NPC attack a hostile mob
    private void attackHostileMob(Zombie npc, Mob mob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npc != null && !npc.isDead() && mob != null && !mob.isDead()) {
                    Location npcLocation = npc.getLocation();
                    Location mobLocation = mob.getLocation();

                    double distance = npcLocation.distance(mobLocation);

                    // Move the NPC toward the mob if it's more than 1 block away
                    if (distance > 1.5) {
                        Vector direction = mobLocation.toVector().subtract(npcLocation.toVector()).normalize();
                        npc.setVelocity(direction.multiply(0.3));  // Move toward the mob
                    } else {
                        mob.damage(5, npc);  // Damage the mob with a 5 damage value
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);  // Run every tick for smooth attacking
    }

    // Make the NPC follow the player
    private void followPlayer(Zombie npc, Player player) {
        Location playerLocation = player.getLocation();
        Location npcLocation = npc.getLocation();

        double distance = npcLocation.distance(playerLocation);

        // Move the NPC toward the player if it's more than 3 blocks away
        if (distance > 3.0) {
            Vector direction = playerLocation.toVector().subtract(npcLocation.toVector()).normalize();
            npc.setVelocity(direction.multiply(0.2));  // Adjust movement speed
        } else {
            npc.setVelocity(new Vector(0, 0, 0));  // Stop the NPC when close to the player
        }
    }

    // Remove the Zombie if it exists
    public void removeZombieIfExists() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID storedUUID = dataHandler.getStoredZombieUUID();
            Zombie zombie = findZombieByUUID(storedUUID);

            if (zombie != null && !zombie.isDead()) {
                plugin.getLogger().info("Removing existing NPC.");
                zombie.remove();
                dataHandler.clearZombieUUID();  // Clear the saved UUID after removal
            }
        });
    }

    // Teleport the Zombie to the player
    public void teleportZombieToPlayer(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID storedUUID = dataHandler.getStoredZombieUUID();
            Zombie zombie = findZombieByUUID(storedUUID);

            if (zombie != null && !zombie.isDead()) {
                plugin.getLogger().info("Teleporting Zombie to player.");
                zombie.teleport(player.getLocation());
            } else {
                plugin.getLogger().warning("No Zombie found to teleport.");
            }
        });
    }

}
