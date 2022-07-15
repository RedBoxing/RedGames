package fr.redboxing.redgames.configuration;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class IntegerOption extends BasicOption<Integer> {
    @Getter
    private final int min;
    @Getter
    private final int max;
    @Getter
    private final int step;

    public IntegerOption(ItemStack itemStack, int defaultValue, int min, int max, int step, Object object, Method setter) {
        super(itemStack, defaultValue, object, setter);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void next() {
        int value = this.getValue() + this.step;
        if(value > this.max) {
            value = this.min;
        } else if(value < this.min) {
            value = this.max;
        }

        this.setValue(value);
    }

    @Override
    public void previous() {
        int value = this.getValue() - this.step;
        if(value > this.max) {
            value = this.min;
        } else if(value < this.min) {
            value = this.max;
        }

        this.setValue(value);
    }
}
