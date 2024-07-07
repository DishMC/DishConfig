package net.ouja.dish.entity.passive;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.ouja.dish.entity.DishEntity;

public class DishFrog extends DishEntity implements net.ouja.api.entity.passive.Frog {
    private Frog frog;

    public DishFrog(LivingEntity entity) {
        super(entity);
        this.frog = (Frog)entity;
    }

    @Override
    public String getVariant() {
        return this.frog.getVariant().toString();
    }
}
