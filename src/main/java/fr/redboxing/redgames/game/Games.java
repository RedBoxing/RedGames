package fr.redboxing.redgames.game;

import fr.redboxing.redgames.game.games.DeathSwapGame;
import fr.redboxing.redgames.game.games.ManHuntGame;
import fr.redboxing.redgames.utils.ItemUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public enum Games {
    MAN_HUNT(ManHuntGame.class, ItemUtils.createHeadItem(UUID.fromString("ec70bcaf-702f-4bb8-b48d-276fa52a780c"), "ManHunt", List.of("A player have to beat the game",  "while other players try to kill him"))),
    DEATH_SWAP(DeathSwapGame.class, ItemUtils.createItemStack(Material.WHITE_WOOL, "DeathSwap", List.of("Every x ammount of time every player",  "are swapped and each player have to",  "kill the other one")));
    ;

    @Getter
    private Class<? extends Game> clazz;

    @Getter
    private ItemStack itemStack;
}
