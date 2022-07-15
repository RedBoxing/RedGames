package fr.redboxing.redgames.game.games;

import fr.redboxing.redgames.RedGames;
import fr.redboxing.redgames.configuration.IntegerOption;
import fr.redboxing.redgames.game.Game;
import fr.redboxing.redgames.game.games.manhunt.CompassTask;
import fr.redboxing.redgames.game.games.manhunt.SpeedRunner;
import fr.redboxing.redgames.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ManHuntGame extends Game {
    @Getter
    @Setter
    private int speedrunnerCount = 1;
    @Getter
    private final List<SpeedRunner> speedrunners = new ArrayList<>();
    @Getter
    private final List<UUID> hunters = new ArrayList<>();
    private BukkitTask compassTask;

    private int i = 0;
    private int index = 0;

    public ManHuntGame(RedGames plugin, World world, String title, UUID ownerUUID) throws NoSuchMethodException {
        super(plugin, world, title, ownerUUID);

        this.getSettings().add(new IntegerOption(ItemUtils.createItemStack(Material.ENDER_PEARL, "Speedrunners Count"), 1, 1, 5, 1, this, ManHuntGame.class.getDeclaredMethod("setSpeedrunnerCount", int.class)));
    }

    @Override
    public void start() {
        super.start();

        this.hunters.addAll(this.getWorld().getPlayers().stream().map(Entity::getUniqueId).toList());

        for(int i = 0; i < this.getSpeedrunnerCount(); i++) {
            int retry = 0;
            Player player = this.getWorld().getPlayers().get(new Random().nextInt(this.getWorld().getPlayers().size()));
            while(this.isSpeedRunner(player) && retry <= 5) {
                player = this.getWorld().getPlayers().get(new Random().nextInt(this.getWorld().getPlayers().size()));
                retry++;
            }

            if(retry == 5 && this.isSpeedRunner(player)) {
                continue;
            }

            this.speedrunners.add(new SpeedRunner(player));
            this.hunters.remove(player.getUniqueId());
        }

        this.getWorld().getPlayers().forEach(player -> {
            player.teleport(this.getWorld().getSpawnLocation());

            if(this.isHunter(player)) {
                player.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        });

        this.compassTask = new CompassTask(this).runTaskTimer(this.getPlugin(), 10, 10);
        this.broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "RedGames" + ChatColor.AQUA + "] " + ChatColor.GREEN + "The game has started!");
        this.setStatus(Status.PLAYING);
    }

    @Override
    public void stop() {
        this.broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "RedGames" + ChatColor.AQUA + "] " + ChatColor.RED + "The game has ended!");
        this.setStatus(Status.ENDED);
    }

    public boolean isHunter(Player player) {
        for(UUID uui : this.hunters) {
            if(uui == player.getUniqueId())
                return true;
        }

        return false;
    }

    public boolean isSpeedRunner(Player player) {
        for(SpeedRunner speedRunner : speedrunners) {
            if(speedRunner.getPlayer() == player)
                return true;
        }

        return false;
    }

    public SpeedRunner getSpeedRunner(Player player) {
        for(SpeedRunner speedRunner : speedrunners) {
            if(speedRunner.getPlayer() == player)
                return speedRunner;
        }

        return null;
    }

    public SpeedRunner getRandomSpeedRunner() {
        SpeedRunner speedRunner = this.speedrunners.get(this.index);

        if(this.speedrunners.size() > 1) {
            i++;
            if(i == 100) {
                ++this.index;
                this.i = 0;
            }

            if (this.index > this.speedrunners.size())
                this.index = 0;
        }

        return speedRunner;
    }

    @EventHandler
    private void onDamagedByEntity(EntityDamageByEntityEvent event) {
        if(!this.shouldProcessEvent(event.getEntity().getWorld())) return;
        if(!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if(event.getFinalDamage() > player.getHealth()) {
            handlePlayerDeath(player, event.getDamager(), event);
        }
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if(!this.shouldProcessEvent(event.getEntity().getWorld())) return;
        if(!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if(event.getFinalDamage() > player.getHealth()) {
            handlePlayerDeath(player, null, event);
        }
    }

    private void handlePlayerDeath(Player player, Entity killer, Cancellable event) {
        if(!this.shouldProcessEvent(player.getWorld())) return;

        if(this.isSpeedRunner(player)) {
            event.setCancelled(true);

            if(this.speedrunners.size() == 1) {
                for(UUID uuid : this.hunters) {
                    Player player1 = Bukkit.getPlayer(uuid);
                    if(player1 == null) continue;

                    player1.setGameMode(GameMode.SPECTATOR);
                    player1.sendTitle(ChatColor.GREEN + "VICTORY", ChatColor.GREEN + player.getName() + " have died");
                }

                for(SpeedRunner speedRunner : this.speedrunners) {
                    speedRunner.getPlayer().setGameMode(GameMode.SPECTATOR);
                    speedRunner.getPlayer().sendTitle(ChatColor.RED + "LOSE", ChatColor.RED + "You have died");
                }

                Arrays.stream(player.getInventory().getContents()).spliterator().forEachRemaining(itemStack -> {
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                });

                this.compassTask.cancel();
                Bukkit.broadcastMessage("[" + ChatColor.AQUA + "ManHunt" + ChatColor.WHITE + "] " + ChatColor.GOLD + "ManHunt Stopped !");
            } else if(this.speedrunners.size() > 1) {
                for(Player player1 : Bukkit.getOnlinePlayers()) {
                    player1.sendTitle(ChatColor.RED + player.getName() + " DIED", killer != null ? ChatColor.RED + "Killed by " + killer.getName() : "");
                }

                player.setGameMode(GameMode.SPECTATOR);
                this.speedrunners.remove(this.getSpeedRunner(player));

                Arrays.stream(player.getInventory().getContents()).spliterator().forEachRemaining(itemStack -> {
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                });
            }
        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        if(!this.shouldProcessEvent(event.getPlayer().getWorld())) return;

        Player player = event.getPlayer();
        if(this.hunters.contains(player.getUniqueId())) {
            player.getInventory().addItem(new ItemStack(Material.COMPASS));
        }
    }

    @EventHandler
    private void onPlayerEnterPortal(PlayerPortalEvent event) {
        if(!this.shouldProcessEvent(event.getPlayer().getWorld())) return;

        Player player = event.getPlayer();
        if(this.isSpeedRunner(player) && this.speedrunners.size() > 0) {
            SpeedRunner speedRunner = this.getSpeedRunner(player);
            if( speedRunner== null) return;
            speedRunner.setLastPortalUsed(event.getFrom());
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        if(!this.shouldProcessEvent(event.getPlayer().getWorld())) return;

        Player player = event.getPlayer();
        if(this.isSpeedRunner(player)) {

        } else if(this.isHunter(player)) {

        }
    }
}
