package fr.redboxing.redgames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class ItemUtils {
    public static ItemStack createItemStack(Material material, String name) {
        return createItemStack(material, name, null);
    }

    public static ItemStack createItemStack(Material material, String name, List<String> lores) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(name != null) {
            itemMeta.setDisplayName(name);
        }
        if(lores != null && lores.size() > 0) {
            itemMeta.setLore(lores);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createHeadItem(UUID owner, String name) {
        return createHeadItem(owner, name, null);
    }

    public static ItemStack createHeadItem(UUID owner, String name, List<String> lores) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        if(name != null) {
            skullMeta.setDisplayName(name);
        }
        if(lores != null && lores.size() > 0) {
            skullMeta.setLore(lores);
        }
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }
}
