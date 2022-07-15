package fr.redboxing.redgames.game.games;

import fr.redboxing.redgames.RedGames;
import fr.redboxing.redgames.configuration.IntegerOption;
import fr.redboxing.redgames.game.Game;
import fr.redboxing.redgames.game.games.deathswap.DeathSwapTask;
import fr.redboxing.redgames.utils.ItemUtils;
import fr.redboxing.redgames.utils.LocationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Tuple;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class DeathSwapGame extends Game {
    @Getter
    @Setter
    private int maxTimer;

    @Getter
    @Setter
    private int minTimer;

    private BukkitTask task;

    @Getter

    private List<UUID> players = new ArrayList<>();

    public DeathSwapGame(RedGames plugin, World world, String title, UUID ownerUUID) throws NoSuchMethodException {
        super(plugin, world, title, ownerUUID);

        this.getSettings().add(new IntegerOption(ItemUtils.createItemStack(Material.CLOCK, "Max Time"), 300, 1, 1000, 10, this, DeathSwapGame.class.getDeclaredMethod("setMaxTimer", int.class)));
        this.getSettings().add(new IntegerOption(ItemUtils.createItemStack(Material.CLOCK, "Min Time"), 120, 1, 1000, 10, this, DeathSwapGame.class.getDeclaredMethod("setMinTimer", int.class)));
    }

    @Override
    public RedGames getPlugin() {
        return super.getPlugin();
    }

    @Override
    public void start() {
        super.start();

        this.players.addAll(this.getWorld().getPlayers().stream().map(Entity::getUniqueId).toList());

        this.players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            player.spigot().respawn();
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0D);
            player.setFoodLevel(20);
            player.setSaturation(20.0F);
            player.setFireTicks(0);
            player.setLevel(0);
            player.setExp(0.0F);
            player.setFallDistance(0);
            player.getInventory().clear();

            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

            Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();
            while (advancements.hasNext()) {
                AdvancementProgress progress = player.getAdvancementProgress(advancements.next());
                for (String s : progress.getAwardedCriteria())
                    progress.revokeCriteria(s);
            }

            player.teleport(LocationUtils.GetRandomLocation(this.getWorld(), (int) this.getWorld().getWorldBorder().getSize() - 5));
        });

        this.task = new DeathSwapTask(this).runTaskTimer(this.getPlugin(), 20, 20);

        this.broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "RedGames" + ChatColor.AQUA + "] " + ChatColor.GREEN + "The game has started!");
    }

    public List<Tuple<UUID, UUID>> makePair() {
        List<Tuple<UUID, UUID>> pairs = new ArrayList<>();
        List<Player> players = this.players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
        Collections.shuffle(players);

        while (players.size() >= 2) {
            Player p1 = players.get(players.size() - 1);
            Player p2 = players.get(players.size() - 2);

            pairs.add(new Tuple<>(p1.getUniqueId(), p2.getUniqueId()));

            int size = players.size();
            players.remove(size - 1);
            players.remove(size - 2);
        }

        if(players.size() == 1) {
            Player p1 = players.get(0);
            pairs.add(new Tuple<>(p1.getUniqueId(), null));
            players.remove(0);
        }

        return pairs;
    }

    @Override
    public void stop() {
        super.stop();

        this.task.cancel();
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if(!this.shouldProcessEvent(event.getEntity().getWorld())) return;

        Player player = event.getEntity();

        if(this.players.contains(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
            this.getWorld().strikeLightning(player.getLocation());
            player.sendTitle(ChatColor.RED + "You lost", ChatColor.RED + event.getDeathMessage(), 10, 20, 10);
            this.broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "RedGames" + ChatColor.AQUA + "] " + ChatColor.RED + player.getName() + ChatColor.RED + " has been eliminiated");
            this.players.forEach(uuid -> {
                Player player1 = Bukkit.getPlayer(uuid);
                player1.playSound(player1.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0F, 1.0F);
            });

            this.players.remove(player.getUniqueId());
            if(this.players.size() == 1) {
                Player winner = Bukkit.getPlayer(this.players.get(0));
                winner.setGameMode(GameMode.SPECTATOR);
                winner.sendTitle(ChatColor.GREEN + "You won", ChatColor.GREEN + "You won the game", 10, 20, 10);
                this.broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "RedGames" + ChatColor.AQUA + "] " + ChatColor.GREEN + winner.getName() + ChatColor.GREEN + " has won the game");
                this.stop();
            }
        }
    }
}
