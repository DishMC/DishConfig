package net.ouja.dish.entity.passive;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.ouja.dish.entity.DishEntity;

public class DishSheep extends DishEntity implements net.ouja.api.entity.passive.Sheep {
    private Sheep sheep;

    public DishSheep(Entity entity) {
        super(entity);
        this.sheep = (Sheep)entity;
    }

    @Override
    public boolean isSheared() {
        return this.sheep.isSheared();
    }
}
