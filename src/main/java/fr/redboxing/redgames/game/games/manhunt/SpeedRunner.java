package fr.redboxing.redgames.game.games.manhunt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class SpeedRunner {
    @Getter
    private final UUID uuid;

    @Getter
    @Setter
    private Location lastPortalUsed;

    public SpeedRunner(Player player) {
        this(player.getUniqueId());
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SpeedRunner))
            return false;

        return ((SpeedRunner) obj).uuid == this.uuid;
    }
}
