package fr.redboxing.redgames.game.games.manhunt;

import fr.redboxing.redgames.game.games.ManHuntGame;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@RequiredArgsConstructor
public class CompassTask extends BukkitRunnable {
    private final ManHuntGame game;

    @Override
    public void run() {
        for(UUID uuid : game.getHunters()) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) continue;

            boolean isMainHand = player.getInventory().getItemInMainHand().getType() == Material.COMPASS;
            boolean isOffHand = player.getInventory().getItemInOffHand().getType() == Material.COMPASS;

            if (!isMainHand && !isOffHand) {
                continue;
            }

            SpeedRunner speedRunner = game.getRandomSpeedRunner();

            if (speedRunner.getPlayer().getWorld().getEnvironment() == player.getWorld().getEnvironment()) {
                player.setCompassTarget(speedRunner.getPlayer().getLocation());

                ItemStack itemStack = isMainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                if(itemStack == null) continue;
                if(itemStack.getType() != Material.COMPASS) continue;

                CompassMeta meta = (CompassMeta) itemStack.getItemMeta();
                meta.setLodestone(speedRunner.getPlayer().getLocation());
                meta.setLodestoneTracked(false);
                itemStack.setItemMeta(meta);
            } else {
                player.setCompassTarget(speedRunner.getLastPortalUsed());

                ItemStack itemStack = isMainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                if(itemStack == null) continue;
                if(itemStack.getType() != Material.COMPASS) continue;

                CompassMeta meta = (CompassMeta) itemStack.getItemMeta();
                meta.setLodestone(speedRunner.getLastPortalUsed());
                meta.setLodestoneTracked(false);
                itemStack.setItemMeta(meta);
            }
        }
    }
}