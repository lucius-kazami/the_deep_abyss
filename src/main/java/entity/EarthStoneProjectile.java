package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EarthStoneProjectile extends ThrowableItemProjectile {
    private float damage = 10.0F;

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public EarthStoneProjectile(EntityType<? extends EarthStoneProjectile> type, Level level) {
        super(type, level);
    }

    public EarthStoneProjectile(Level level, LivingEntity owner) {
        super(Abyss.EARTH_STONE.get(), owner, level, new ItemStack(Items.COBBLESTONE));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBBLESTONE;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(level() instanceof ServerLevel server)) {
            return;
        }

        var target = result.getEntity();

        target.hurtServer(
                server,
                damageSources().thrown(this, getOwner()),
                damage
        );

        // Небольшое отбрасывание тяжёлым камнем
        double dx = getDeltaMovement().x;
        double dz = getDeltaMovement().z;

        double length = Math.max(
                0.1,
                Math.sqrt(dx * dx + dz * dz)
        );

        target.push(
                dx / length * 0.5,
                0.2,
                dz / length * 0.5
        );

        target.hurtMarked = true;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    getX(), getY(), getZ(), 12, 0.15, 0.15, 0.15, 0.05);
            discard();
        }
    }
}