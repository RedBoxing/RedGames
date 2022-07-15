package fr.redboxing.redgames;

import fr.redboxing.redgames.commands.RedGamesCommand;
import fr.redboxing.redgames.game.GameManager;
import lombok.Getter;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RedGames extends JavaPlugin {
    @Getter
    private GameManager gameManager;

    @Override
    public void onEnable() {
        this.gameManager = new GameManager(this);
        this.getServer().getPluginManager().registerEvents(new RedGamesListener(), this);
        this.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);
        this.getCommand("redgames").setExecutor(new RedGamesCommand(this));
    }

    @Override
    public void onDisable() {
        this.gameManager.clear();
    }
}
