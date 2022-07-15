package fr.redboxing.redgames.game;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import fr.redboxing.redgames.RedGames;
import fr.redboxing.redgames.configuration.DoubleOption;
import fr.redboxing.redgames.configuration.IntegerOption;
import fr.redboxing.redgames.configuration.Option;
import fr.redboxing.redgames.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class Game implements Listener {
    @Getter
    private RedGames plugin;

    @Getter
    private World world;

    @Getter
    @Setter
    private UUID ownerUUID;

    @Getter
    @Setter
    private List<UUID> coHostUUID;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private Status status;

    @Getter
    @Setter
    private BukkitTask countdownTask;

    @Getter
    @Setter
    private int countdown = 10;

    @Getter
    @Setter
    private List<Option<?>> settings;

    @Getter
    @Setter
    private List<Option<?>> worldGenerationSettings;

    @Getter
    @Setter
    private int maxPlayers;

    @Getter
    @Setter
    private int minPlayers;

    @Getter
    @Setter
    private Map<Scenarios, Scenario> scenarios;

    @Getter
    @Setter
    private BukkitTask endTask;

    public Game(RedGames plugin, World world, String title, UUID ownerUUID) {
        this.plugin = plugin;
        this.world = world;
        this.title = title;
        this.ownerUUID = ownerUUID;
        this.coHostUUID = new ArrayList<>();
        this.status = Status.WAITING;
        this.settings = new ArrayList<>();
        this.worldGenerationSettings = new ArrayList<>();
        this.scenarios = new HashMap<>();

        for(Scenarios scenario : Scenarios.values()) {
            try {
                this.scenarios.put(scenario, scenario.getClazz().getConstructor(Game.class).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            setupDefaultSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.world.getWorldBorder().setCenter(0, 0);

        File file = new File(this.getPlugin().getDataFolder(), "lobby.schem");
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clipboard.paste(BukkitAdapter.adapt(this.world), BlockVector3.at(0, 150, 0));
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    private void setupDefaultSettings() throws NoSuchMethodException {
        this.settings.add(new IntegerOption(ItemUtils.createItemStack(Material.DIAMOND_SWORD, ChatColor.RED + "Max Players"), 10, 2, 30, 1, this, Game.class.getDeclaredMethod("setMaxPlayers", int.class)));
        this.settings.add(new IntegerOption(ItemUtils.createItemStack(Material.IRON_SWORD, ChatColor.RED + "Min Players"), 2, 2, 30, 1, this, Game.class.getDeclaredMethod("setMinPlayers", int.class)));
        this.settings.add(new DoubleOption(ItemUtils.createItemStack(Material.GLASS, "Border Size"), 500, 100, 5000, 100, this.world.getWorldBorder(), this.world.getWorldBorder().getClass().getDeclaredMethod("setSize", double.class)));
    }

    public void startCountdown() {
        this.status = Status.STARTING;
        this.countdownTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            this.world.getPlayers().forEach(player -> {
                player.sendTitle("", this.countdown + " seconds remaining", 10, 70, 20);
                player.setLevel(this.countdown);
            });

            this.countdown--;

            if(this.countdown == -1) {
                this.status = Status.PLAYING;
                this.countdownTask.cancel();
                this.start();
            }
        }, 20, 20);
    }

    public void cancelCountdown() {
        this.countdownTask.cancel();
        this.countdown = 10;
        this.world.getPlayers().forEach(player -> player.setLevel(0));
        this.status = Status.WAITING;
    }

    public void start() {
        //this.plugin.getGameManager().generateWorld(this);
        this.world.getPlayers().forEach(player -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0D);
            player.setFoodLevel(20);
            player.setSaturation(20.0F);
            player.setFireTicks(0);
            player.setLevel(0);
            player.setExp(0.0F);
            player.setFallDistance(0);
            player.getInventory().clear();
        });

        this.scenarios.values().forEach(scenario -> {
            if(scenario.isEnabled()) {
                scenario.onGameStart();
            }
        });
    }
    public void stop() {
        this.scenarios.values().forEach(scenario -> {
            if(scenario.isEnabled()) {
                scenario.onGameEnd();
            }
        });

        this.endTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.getWorld().getPlayers().forEach(player -> {
                player.teleport(new Location(this.world, 0, 152, 0));
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0D);
                player.setFoodLevel(20);
                player.setSaturation(20.0F);
                player.setFireTicks(0);
                player.setLevel(0);
                player.setExp(0.0F);
                player.setFallDistance(0);
                player.getInventory().clear();
            });
        }, 20 * 5);
    }

    public void broadcastMessage(String message) {
        this.world.getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.world.getPlayers().forEach(player -> player.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }

    public void broadcastActionBar(String message) {
        this.world.getPlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message)));
    }

    public boolean shouldProcessEvent(World world) {
        return this.status == Status.PLAYING && this.world.getName().equals(world.getName());
    }

    public enum Status {
        WAITING,
        STARTING,
        PLAYING,
        ENDED
    }
}
