package fr.redboxing.redgames.game.scenarios;

import fr.redboxing.redgames.configuration.BooleanOption;
import fr.redboxing.redgames.game.Game;
import fr.redboxing.redgames.game.Scenario;
import fr.redboxing.redgames.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class BlockShuffleScenario extends Scenario {
    private final Map<Material, Material> randomisedItems = new HashMap<>();
    private final Map<Recipe, Material> randomisedRecipes = new HashMap<>();
    private Random random = new Random();

    @Getter
    @Setter
    private boolean randomiseCrafts = true;

    public BlockShuffleScenario(Game game) throws NoSuchMethodException {
        super(game);

        this.getSettings().add(new BooleanOption(ItemUtils.createItemStack(Material.CRAFTING_TABLE, "Randomise Crafts", List.of("Randomise crafts")), true, this, BlockShuffleScenario.class.getDeclaredMethod("setRandomiseCrafts", boolean.class)));
    }

    @Override
    public void onGameStart() {
        randomisedItems.clear();
        for(Material material : Material.values()) {
            if(material.isItem()) {
                Material material1 = Material.values()[random.nextInt(Material.values().length)];
                while(!material1.isItem() && material1 != material && material1 != Material.AIR) {
                    material1 = Material.values()[random.nextInt(Material.values().length)];
                }

                this.randomisedItems.put(material, material1);
            }
        }

        if(this.randomiseCrafts) {
            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {
                Material randomMaterial = Material.values()[random.nextInt(Material.values().length)];
                while(!randomMaterial.isItem() && randomMaterial != recipe.getResult().getType() && randomMaterial != Material.AIR) {
                    randomMaterial = Material.values()[random.nextInt(Material.values().length)];
                }

                this.randomisedRecipes.put(recipe, randomMaterial);
            });
        }
    }

    @EventHandler
    private void onBlockBroken(BlockBreakEvent event) {
        if(!this.shouldProcessEvent(event.getPlayer().getWorld())) return;

        ItemStack item = event.getPlayer().getItemInUse();
        Collection<ItemStack> drops = event.getBlock().getDrops(item);
        if(drops.size() == 0) return;

        event.setDropItems(false);
        Location location = event.getBlock().getLocation();

        for(ItemStack drop : drops) {
            if(randomisedItems.containsKey(drop.getType())) {
                event.getBlock().getWorld().dropItemNaturally(location, new ItemStack(randomisedItems.get(drop.getType()), drop.getAmount()));
            }
        }
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        if(!this.shouldProcessEvent(event.getEntity().getWorld())) return;

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();

        for (ItemStack drop : drops) {
            if (randomisedItems.containsKey(drop.getType())) {
                event.getDrops().add(new ItemStack(randomisedItems.get(drop.getType()), drop.getAmount()));
            }
        }
    }

    @EventHandler
    private void onCraft(CraftItemEvent event) {
        if(!this.shouldProcessEvent(event.getWhoClicked().getWorld())) return;

        if(this.randomiseCrafts && this.randomisedRecipes.containsKey(event.getRecipe())) {
            CraftingInventory result = event.getInventory();
            result.setResult(new ItemStack(this.randomisedRecipes.get(event.getRecipe()), event.getCurrentItem().getAmount()));
        }
    }
}
