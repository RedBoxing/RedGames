package fr.redboxing.redgames.configuration;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BasicOption<T> implements Option<T> {
    @Getter
    private ItemStack itemStack;
    private T value;
    private Object object;
    private Method setter;

    public BasicOption(ItemStack itemStack, T defaultValue, Object object, Method setter) {
        this.itemStack = itemStack;
        this.value = defaultValue;
        this.object = object;
        this.setter = setter;

        this.setValue(defaultValue);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        try {
            Object obj = this.setter.invoke(this.object, value);
            this.value = value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void next() {
    }

    @Override
    public void previous() {

    }

    @Override
    public ItemStack getItem() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Value: " + ChatColor.WHITE + getValueString());
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public String getValueString() {
        return this.value.toString();
    }
}
