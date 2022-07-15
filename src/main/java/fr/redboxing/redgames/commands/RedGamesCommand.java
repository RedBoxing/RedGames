package fr.redboxing.redgames.commands;

import fr.redboxing.redgames.RedGames;
import fr.redboxing.redgames.game.Game;
import fr.redboxing.redgames.game.Games;
import fr.redboxing.redgames.game.Scenario;
import fr.redboxing.redgames.game.Scenarios;
import fr.redboxing.redgames.utils.ItemUtils;
import lombok.AllArgsConstructor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.type.ChestMenu;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@AllArgsConstructor
public class RedGamesCommand implements CommandExecutor {
    private RedGames plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(((Player) sender).getWorld().getName().startsWith("world")) { //TODO: get default world name
                Menu mainMenu = createMainMenu();
                mainMenu.open((Player) sender);
            } else {
                openServerMenu(this.plugin.getGameManager().getGame(UUID.fromString(((Player) sender).getWorld().getName())), (Player) sender);
            }
        } else {
            sender.sendMessage(ChatColor.RED + " Only player can execute this player !");
        }

        return true;
    }

    private void openServerMenu(Game game, Player player) {
        Menu menu = ChestMenu.builder(5).title(game.getTitle()).build();
        Mask mask = BinaryMask.builder(menu).item(ItemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, " "))
                .pattern("110000011")
                .pattern("100000001")
                .pattern("000000000")
                .pattern("100000001")
                .pattern("110000011").build();
        mask.apply(menu);

        menu.getSlot(13).setItem(ItemUtils.createItemStack(Material.OAK_SIGN, game.getTitle(), List.of("Click here to rename your server")));

        menu.getSlot(20).setItem(ItemUtils.createItemStack(Material.GUNPOWDER, "Settings", List.of("Click here to open the settings menu")));
        menu.getSlot(20).setClickHandler((player1, slot) -> {
            openServerSettingsMenu(game, player1, menu);
        });

        menu.getSlot(21).setItem(ItemUtils.createItemStack(Material.GRASS_BLOCK, "World Generation Settings", List.of("Click here to open the world generation settings menu")));
        menu.getSlot(21).setClickHandler((player1, slot) -> {
            openWorldGenerationSettingsMenu(game, player1, menu);
        });

        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setOwningPlayer(Bukkit.getOfflinePlayer(game.getOwnerUUID()));
        itemMeta.setDisplayName(ChatColor.GOLD + Bukkit.getOfflinePlayer(game.getOwnerUUID()).getName() + " is the Host");

        if(player.getUniqueId().equals(game.getOwnerUUID())) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        List<String> lores = new ArrayList<>();
        game.getCoHostUUID().forEach(uuid -> {
            lores.add(ChatColor.LIGHT_PURPLE + Bukkit.getOfflinePlayer(uuid).getName() + " is a Co-Host");
        });

        lores.add(ChatColor.GRAY + "Click here to add co-hosts");

        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);

        menu.getSlot(22).setItem(itemStack);

        menu.getSlot(24).setItem(ItemUtils.createItemStack(Material.COMMAND_BLOCK, "Scenarios", List.of(ChatColor.GRAY + "Click here to open the scenarios menu")));
        menu.getSlot(24).setClickHandler((player1, info) -> openScenariosMenu(game, player1, menu));

        if(game.getStatus() == Game.Status.WAITING || game.getStatus() == Game.Status.ENDED) {
            menu.getSlot(40).setItem(ItemUtils.createItemStack(Material.LIME_BANNER, "Start", List.of(ChatColor.GRAY + "Click here to start the game")));
        } else if(game.getStatus() == Game.Status.STARTING) {
            menu.getSlot(40).setItem(ItemUtils.createItemStack(Material.RED_BANNER, "Cancel", List.of(ChatColor.GRAY + "Click here to stop the game")));
        }

        menu.getSlot(40).setClickHandler((plr, info) -> {
            if(game.getStatus() == Game.Status.WAITING || game.getStatus() == Game.Status.ENDED) {
                game.startCountdown();
                menu.getSlot(40).setItem(ItemUtils.createItemStack(Material.RED_BANNER, "Cancel", List.of(ChatColor.GRAY + "Click here to stop the game")));
            } else if(game.getStatus() == Game.Status.STARTING) {
                game.cancelCountdown();
                menu.getSlot(40).setItem(ItemUtils.createItemStack(Material.LIME_BANNER, "Start", List.of(ChatColor.GRAY + "Click here to start the game")));
            }
        });

        menu.open(player);
    }

    private void openServerSettingsMenu(Game game, Player player, Menu previousMenu) {
        Menu.Builder menu = ChestMenu.builder(3).title("Settings").redraw(true);
        Mask mask = BinaryMask.builder(menu.getDimensions())
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("011111110").build(); // Fourth row

        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(menu)
                .slots(mask)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonEmpty(new ItemStack(Material.AIR))
                .nextButtonSlot(23)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonEmpty(new ItemStack(Material.AIR))
                .previousButtonSlot(21);

        game.getSettings().forEach(option -> {
            SlotSettings.Builder slotSettings = SlotSettings.builder()
                    .item(option.getItem())
                    .clickHandler(((player1, clickInformation) -> {
                        if(clickInformation.getClickType().isRightClick()) {
                            option.previous();
                            clickInformation.getClickedSlot().setItem(option.getItem());
                        } else if(clickInformation.getClickType().isLeftClick()) {
                            option.next();
                            clickInformation.getClickedSlot().setItem(option.getItem());
                        }
                    }));

            builder.addItem(slotSettings.build());
        });

        List<Menu> pages = builder.build();
        pages.forEach(page -> {
            page.getSlot(22).setItem(ItemUtils.createItemStack(Material.BARRIER, "Go back"));
            page.getSlot(22).setClickHandler((plr, info) -> {
                previousMenu.open(plr);
            });
        });

        pages.get(0).open(player);
    }

    private void openWorldGenerationSettingsMenu(Game game, Player player, Menu previousMenu) {
        Menu.Builder menu = ChestMenu.builder(3).title("World Generation Settings").redraw(true);
        Mask mask = BinaryMask.builder(menu.getDimensions())
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("011111110").build();

        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(menu)
                .slots(mask)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonEmpty(new ItemStack(Material.AIR))
                .nextButtonSlot(23)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonEmpty(new ItemStack(Material.AIR))
                .previousButtonSlot(21);

        game.getWorldGenerationSettings().forEach(option -> {
            SlotSettings.Builder slotSettings = SlotSettings.builder()
                    .item(option.getItem())
                    .clickHandler(((player1, clickInformation) -> {
                        if(clickInformation.getClickType().isRightClick()) {
                            option.previous();
                            clickInformation.getClickedSlot().setItem(option.getItem());
                        } else if(clickInformation.getClickType().isLeftClick()) {
                            option.next();
                            clickInformation.getClickedSlot().setItem(option.getItem());
                        }
                    }));

            builder.addItem(slotSettings.build());
        });

        List<Menu> pages = builder.build();
        pages.forEach(page -> {
            page.getSlot(22).setItem(ItemUtils.createItemStack(Material.BARRIER, "Go back"));
            page.getSlot(22).setClickHandler((plr, info) -> {
                previousMenu.open(plr);
            });
        });

        pages.get(0).open(player);
    }

    private Menu createMainMenu() {
        Menu menu = ChestMenu.builder(1).title(ChatColor.RED + "RedGames Menu").redraw(true).build();
        BinaryMask mask = BinaryMask.builder(menu.getDimensions()).pattern("111010111").item(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)).build();
        mask.apply(menu);

        ItemStack createStack = new ItemStack(Material.LIME_BANNER);
        ItemMeta createMeta = createStack.getItemMeta();
        createMeta.setDisplayName("Create Server");
        createStack.setItemMeta(createMeta);
        Slot createSlot = menu.getSlot(3);
        createSlot.setItem(createStack);
        createSlot.setClickHandler((player, info) -> {
            openServerCreationMenu(player);
        });

        ItemStack viewStack = new ItemStack(Material.OAK_SIGN);
        ItemMeta viewMeta = viewStack.getItemMeta();
        viewMeta.setDisplayName("View Servers");
        viewStack.setItemMeta(viewMeta);
        menu.getSlot(5).setItem(viewStack);
        menu.getSlot(5).setClickHandler((player, info) -> {
            openServerSelectionMenu(player, menu);
        });

        return menu;
    }

    private void openScenariosMenu(Game game, Player player, Menu previous) {
        Menu.Builder menu = ChestMenu.builder(3).title("Scenarios").redraw(true);
        Mask mask = BinaryMask.builder(menu.getDimensions())
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("011111110").build(); // Fourth row

        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(menu)
                .slots(mask)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonEmpty(new ItemStack(Material.AIR))
                .nextButtonSlot(23)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonEmpty(new ItemStack(Material.AIR))
                .previousButtonSlot(21);

        for(Scenarios scenarioType : Scenarios.values()) {
            SlotSettings.Builder slotSettings = SlotSettings.builder()
                    .item(scenarioType.getItemStack())
                    .clickHandler(((player1, clickInformation) -> {
                        Scenario scenario = game.getScenarios().get(scenarioType);

                        if(clickInformation.getClickType().isLeftClick()) {
                            if(scenario.isEnabled()) {
                                scenario.setEnabled(false);
                            } else {
                                scenario.setEnabled(true);
                            }

                            ItemStack item = scenarioType.getItemStack();
                            ItemMeta meta = item.getItemMeta();
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            if(scenario.isEnabled()) {
                                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                            } else if(meta.hasEnchant(Enchantment.DURABILITY)) {
                                meta.removeEnchant(Enchantment.DURABILITY);
                            }
                            item.setItemMeta(meta);

                            clickInformation.getClickedSlot().setItem(item);
                        } else if(clickInformation.getClickType().isRightClick()) {
                            openScenarioSettingsMenu(player, game, scenario, clickInformation.getClickedMenu());
                        }
                    }));

            builder.addItem(slotSettings.build());
        }

        List<Menu> pages = builder.build();
        pages.forEach(page -> {
            page.getSlot(22).setItem(ItemUtils.createItemStack(Material.RED_CONCRETE, "Go back"));
            page.getSlot(22).setClickHandler((plr, info) -> {
                previous.open(plr);
            });
        });

        pages.get(0).open(player);
    }

    private void openScenarioSettingsMenu(Player player, Game game, Scenario scenario, Menu previous) {
        Menu.Builder menu = ChestMenu.builder(3).title(Scenarios.getScenarioTypeFromClass(scenario.getClass()) + " Settings").redraw(true);
        Mask mask = BinaryMask.builder(menu.getDimensions())
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("011111110").build(); // Fourth row

        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(menu)
                .slots(mask)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonEmpty(new ItemStack(Material.AIR))
                .nextButtonSlot(23)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonEmpty(new ItemStack(Material.AIR))
                .previousButtonSlot(21);

        scenario.getSettings().forEach(option -> {
            SlotSettings.Builder slotSettings = SlotSettings.builder()
                    .item(option.getItem())
                    .clickHandler(((player1, clickInformation) -> {
                        if(clickInformation.getClickType().isRightClick()) {
                            option.previous();
                            clickInformation.getClickedSlot().setItem(option.getItem());
                        } else if(clickInformation.getClickType().isLeftClick()) {
                            option.next();
                            clickInformation.getClickedSlot().setItem(option.getItem());
                        }
                    }));

            builder.addItem(slotSettings.build());
        });

        List<Menu> pages = builder.build();
        pages.forEach(page -> {
            page.getSlot(22).setItem(ItemUtils.createItemStack(Material.RED_CONCRETE, "Go back"));
            page.getSlot(22).setClickHandler((plr, info) -> {
                previous.open(plr);
            });
        });

        pages.get(0).open(player);
    }

    private void openServerSelectionMenu(Player player, Menu previous) {
        Menu.Builder menu = ChestMenu.builder(3).title("Select Server").redraw(true);
        Mask mask = BinaryMask.builder(menu.getDimensions())
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("011111110").build(); // Fourth row

        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(menu)
                .slots(mask)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonEmpty(new ItemStack(Material.AIR))
                .nextButtonSlot(23)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonEmpty(new ItemStack(Material.AIR))
                .previousButtonSlot(21);

        for(Map.Entry<UUID, Game> entry : this.plugin.getGameManager().getGamesMap().entrySet()) {
            UUID uuid = entry.getKey();
            Game game = entry.getValue();

            SlotSettings settings = SlotSettings.builder().item(ItemUtils.createItemStack(Material.BOOK, ChatColor.LIGHT_PURPLE + game.getTitle(), List.of(ChatColor.YELLOW + "Host: " + ChatColor.AQUA + Bukkit.getPlayer(game.getOwnerUUID()).getName()))).clickHandler(((player1, clickInformation) -> {
                player.closeInventory();
                player1.teleport(new Location(game.getWorld(), 0, 152, 0));
            })).build();
            builder.addItem(settings);
        }

        List<Menu> pages = builder.build();
        pages.get(0).open(player);
    }

    private void openServerCreationMenu(Player plr) {
        AnvilGUI.Builder builder = new AnvilGUI.Builder();

        builder.title(ChatColor.GRAY + "Type server name");
        builder.plugin(this.plugin);
        builder.itemLeft(new ItemStack(Material.PAPER));

        builder.onLeftInputClick(player -> {
            Menu mainMenu = createMainMenu();
            mainMenu.open(player);
        });

        builder.onClose(player -> {
            Menu mainMenu = createMainMenu();
            mainMenu.open(player);
        });

        builder.onComplete((player, text) -> {
            Menu menu = createGameSelectionMenu(text.replace(' ', '_'));
            menu.open(player);
            return AnvilGUI.Response.close();
        });

        builder.open(plr);
    }

    private Menu createGameSelectionMenu(String title) {
        Menu.Builder menu = ChestMenu.builder(3).title("Select Game Mode").redraw(true);
        Mask mask = BinaryMask.builder(menu.getDimensions())
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("011111110").build(); // Fourth row

        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(menu)
                .slots(mask)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonEmpty(new ItemStack(Material.AIR))
                .nextButtonSlot(23)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonEmpty(new ItemStack(Material.AIR))
                .previousButtonSlot(21);

        for(Games game : Games.values()) {
            SlotSettings settings = SlotSettings.builder().item(game.getItemStack()).clickHandler(((player, clickInformation) -> {
                player.closeInventory();
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Creating Server...");
                this.plugin.getGameManager().createServer(title, player, Arrays.stream(Games.values()).filter(g -> g.getItemStack().equals(clickInformation.getClickedSlot().getItem(player))).findFirst().orElse(null));
            })).build();
            builder.addItem(settings);
        }

        List<Menu> pages = builder.build();
        return pages.get(0);
    }
}
