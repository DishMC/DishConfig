package net.ouja.dish.entity.monster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.ouja.dish.entity.DishEntity;

public class DishWitherSkeleton extends DishEntity implements net.ouja.api.entity.monster.WitherSkeleton {
    private WitherSkeleton witherSkeleton;

    public DishWitherSkeleton(LivingEntity entity) {
        super(entity);
        this.witherSkeleton = (WitherSkeleton)entity;
    }
}
