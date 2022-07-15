package fr.redboxing.redgames.game.games.deathswap;

import fr.redboxing.redgames.game.games.DeathSwapGame;
import fr.redboxing.redgames.utils.LocationUtils;
import lombok.AllArgsConstructor;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

public class DeathSwapTask extends BukkitRunnable {
    private final DeathSwapGame game;
    private final Random random = new Random();
    private int untilSwap;
    private int timer;
    private int minutes;
    private int totalSwaps = 0;

    public DeathSwapTask(DeathSwapGame game) {
        this.game = game;
        this.untilSwap = this.random.nextInt(game.getMaxTimer() + 1 - game.getMinTimer()) + game.getMinTimer();
    }

    @Override
    public void run() {
        this.timer++;
        this.untilSwap--;

        if(this.timer == 60) {
            this.minutes++;
            this.timer = 0;
        }

        this.game.broadcastActionBar(ChatColor.YELLOW + "Swap in " + this.untilSwap + " seconds");

        String timerMessage = "";
        if(this.timer <= 9) {
            timerMessage += this.minutes + ":0" + this.timer;
        } else {
            timerMessage += this.minutes + ":" + this.timer;
        }

        timerMessage += ChatColor.GRAY + " Swaps [" + ChatColor.DARK_GREEN + this.totalSwaps + ChatColor.GRAY + "]";

        if(this.untilSwap <= 10) {
            timerMessage += ChatColor.DARK_RED + "Swapping in: " + this.untilSwap + " Seconds !";
        }

        if(this.timer < this.game.getMinTimer()) {
            this.game.broadcastActionBar(ChatColor.GREEN + "[SAFE] Time since last swap : " + timerMessage);
        } else {
            this.game.broadcastActionBar(ChatColor.RED + "[DANGER] Time since last swap : " + timerMessage);
        }

        if(this.untilSwap == 0) {
            this.untilSwap = this.random.nextInt(game.getMaxTimer() + 1 - game.getMinTimer()) + game.getMinTimer();
            if(this.game.getWorld().getPlayers().size() < this.game.getPlayers().size()) {
                this.game.broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "RedGames" + ChatColor.AQUA + "] " + ChatColor.RED + "Skipping due to missing players !");
                return;
            }

            this.totalSwaps++;
            for(Tuple<UUID, UUID> pair : this.game.makePair()) {
                Player player1 = Bukkit.getPlayer(pair.a());
                Player player2 = Bukkit.getPlayer(pair.b());

                Location loc = player1.getLocation();

                player1.teleport(player2 == null ? LocationUtils.GetRandomLocation(player1.getWorld(), (int) (player1.getWorldBorder().getSize() - 5)) : player2.getLocation());
                player1.playSound(player1.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

                if(player2 != null) {
                    player2.teleport(loc);
                    player2.playSound(player2.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }
    }
}
