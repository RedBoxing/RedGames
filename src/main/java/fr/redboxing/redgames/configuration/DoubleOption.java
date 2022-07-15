package fr.redboxing.redgames.configuration;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class DoubleOption extends BasicOption<Double> {
    @Getter
    private final double min;
    @Getter
    private final double max;
    @Getter
    private final double step;

    public DoubleOption(ItemStack itemStack, double defaultValue, double min, double max, double step, Object object, Method setter) {
        super(itemStack, defaultValue, object, setter);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void next() {
        double value = this.getValue() + this.step;
        if(value > this.max) {
            value = this.min;
        } else if(value < this.min) {
            value = this.max;
        }

        this.setValue(value);
    }

    @Override
    public void previous() {
        double value = this.getValue() - this.step;
        if(value > this.max) {
            value = this.min;
        } else if(value < this.min) {
            value = this.max;
        }

        this.setValue(value);
    }
}
