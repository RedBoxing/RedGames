package fr.redboxing.redgames.game;

import com.gestankbratwurst.fastchunkpregen.FastChunkPregenerator;
import com.gestankbratwurst.fastchunkpregen.generation.GeneratorManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import fr.redboxing.redgames.RedGames;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class GameManager {
    @Getter
    private final Map<UUID, Game> gamesMap = new HashMap<>();
    private final RedGames plugin;
    private final GeneratorManager generatorManager;

    private MVWorldManager worldManager;

    public GameManager(RedGames plugin) {
        this.plugin = plugin;

        this.generatorManager = ((FastChunkPregenerator) Bukkit.getServer().getPluginManager().getPlugin("FastChunkPregenerator")).getGeneratorManager();
        this.worldManager = ((MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core")).getMVWorldManager();
    }

    public void createServer(String title, Player owner, Games gameType) {
        UUID uuid = UUID.randomUUID();

        if(!this.worldManager.addWorld(uuid.toString(), World.Environment.NORMAL, null, WorldType.NORMAL, true, null)) {
            owner.sendMessage(ChatColor.RED + " An error occured while creating the world !");
            return;
        }

        MultiverseWorld world = this.worldManager.getMVWorld(uuid.toString());

        Game game = null;
        try {
            game = gameType.getClazz().getConstructor(RedGames.class, World.class, String.class, UUID.class).newInstance(this.plugin, world.getCBWorld(), title, owner.getUniqueId());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            owner.sendMessage(ChatColor.RED + "A unexpected error occurred while creating world !");
            throw new RuntimeException(e);
        }

        this.gamesMap.put(uuid, game);
        owner.teleport(new Location(game.getWorld(), 0, 152, 0));
    }

    public void generateWorld(Game game) {
        this.generatorManager.start(game.getWorld(), 0, 0, (int) (game.getWorld().getWorldBorder().getSize() / 16));
    }

    public Game getGame(UUID uuid) {
        return this.gamesMap.get(uuid);
    }

    public boolean hasGame(UUID uuid) {
        return this.gamesMap.containsKey(uuid);
    }

    public void clear() {
        this.gamesMap.forEach((key, value) -> this.worldManager.deleteWorld(key.toString()));
        this.gamesMap.clear();
    }
}
