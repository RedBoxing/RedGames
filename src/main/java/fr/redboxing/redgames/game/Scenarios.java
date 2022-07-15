package fr.redboxing.redgames.game;

import fr.redboxing.redgames.game.scenarios.BlockShuffleScenario;
import fr.redboxing.redgames.utils.ItemUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public enum Scenarios {
    BLOCK_SHUFFLE("Block Shuffle", BlockShuffleScenario.class, ItemUtils.createItemStack(Material.GRASS_BLOCK, "Block Shuffle", List.of("Blocks drops are randomised"))),
    ;

    @Getter
    private String name;

    @Getter
    private Class<? extends Scenario> clazz;

    @Getter
    private ItemStack itemStack;

    public static Scenarios getScenarioTypeFromClass(Class<?> clazz) {
        for(Scenarios scenario : values()) {
            if(scenario.getClazz().equals(clazz)) {
                return scenario;
            }
        }

        return null;
    }
}
