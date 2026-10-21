package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WaterBladeProjectile extends ThrowableItemProjectile {
    private static final float DAMAGE = 10.0F;
    private static final int MAX_AGE = 40;

    public WaterBladeProjectile(EntityType<? extends WaterBladeProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public WaterBladeProjectile(Level level, LivingEntity owner) {
        super(Abyss.WATER_BLADE.get(), owner, level, new ItemStack(Items.PRISMARINE_SHARD));
        setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.PRISMARINE_SHARD;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 3, 0.05, 0.05, 0.05, 0.0);
            if (tickCount > MAX_AGE) {
                discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level() instanceof ServerLevel server) {
            result.getEntity().hurtServer(server, damageSources().thrown(this, getOwner()), DAMAGE);
            result.getEntity().clearFire();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 20, 0.2, 0.2, 0.2, 0.1);
            discard();
        }
    }
}
