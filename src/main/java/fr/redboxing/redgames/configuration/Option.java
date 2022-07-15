package fr.redboxing.redgames.configuration;

import org.bukkit.inventory.ItemStack;

public interface Option<T> {
    T getValue();
    void setValue(T value);

    void next();
    void previous();

    ItemStack getItem();
}
