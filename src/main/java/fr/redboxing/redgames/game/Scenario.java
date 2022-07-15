package fr.redboxing.redgames.game;

import fr.redboxing.redgames.configuration.Option;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class Scenario implements Listener {
    @Getter
    @Setter
    private Game game;

    @Getter
    @Setter
    private List<Option<?>> settings;

    @Getter
    @Setter
    private boolean enabled = false;

    public Scenario(Game game) {
        this.game = game;
        this.settings = new ArrayList<>();

        Bukkit.getPluginManager().registerEvents(this, this.game.getPlugin());
    }

    public void onGameStart() {
    }

    public void onGameEnd() {

    }

    public boolean shouldProcessEvent(World world) {
        return this.game.shouldProcessEvent(world) && this.enabled;
    }
}
