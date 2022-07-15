package fr.redboxing.redgames.configuration;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class BooleanOption extends BasicOption<Boolean> {

    public BooleanOption(ItemStack itemStack, boolean defaultValue, Object object, Method setter) {
        super(itemStack, defaultValue, object, setter);
    }

    @Override
    public void next() {
        this.setValue(!this.getValue());
    }

    @Override
    public void previous() {
        this.setValue(!this.getValue());
    }

    @Override
    public ItemStack getItem() {
        ItemStack itemStack = super.getItem();
        if(this.getValue()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}