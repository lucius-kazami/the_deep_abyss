package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.entity.IceBladeProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.akashi.abyss.abyss.event.FrostFreezeEvents;

public class EternalFrostSwordItem extends Item {

    private static final float ICE_DAMAGE = 5.0F;

    public EternalFrostSwordItem(Properties properties) {
        super(properties);
    }


    // ============================================================
    // УДАР МЕЧОМ
    // ============================================================

    @Override
    public void hurtEnemy(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker
    ) {

        super.hurtEnemy(
                stack,
                target,
                attacker
        );

        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }


        // --------------------------------------------------------
        // ДОПОЛНИТЕЛЬНЫЙ ЛЕДЯНОЙ УРОН
        // --------------------------------------------------------

        target.hurtServer(
                level,
                level.damageSources().freeze(),
                ICE_DAMAGE
        );


        // --------------------------------------------------------
        // ЗАМОРОЗКА НА 10 СЕКУНД
        // --------------------------------------------------------

        FrostFreezeEvents.freeze(target);


        // --------------------------------------------------------
        // ЧАСТИЦЫ
        // --------------------------------------------------------

        level.sendParticles(
                ParticleTypes.SNOWFLAKE,

                target.getX(),
                target.getY() + target.getBbHeight() / 2.0,
                target.getZ(),

                40,

                0.5,
                0.8,
                0.5,

                0.05
        );


        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,

                target.getX(),
                target.getY() + 1.0,
                target.getZ(),

                20,

                0.4,
                0.6,
                0.4,

                0.1
        );


        level.playSound(
                null,
                target.blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                0.8F,
                1.5F
        );
    }


    // ============================================================
    // ПКМ — ЛЕДЯНОЕ ЛЕЗВИЕ
    // ============================================================

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack stack =
                player.getItemInHand(hand);


        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }


        IceBladeProjectile blade =
                new IceBladeProjectile(
                        Abyss.ICE_BLADE_PROJECTILE.get(),
                        serverLevel
                );


        blade.setOwner(player);


        blade.setPos(
                player.getX(),
                player.getEyeY() - 0.2,
                player.getZ()
        );


        blade.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,

                2.5F,

                0.0F
        );


        serverLevel.addFreshEntity(blade);


        // --------------------------------------------------------
        // ВЫСТРЕЛ
        // --------------------------------------------------------

        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,

                player.getX(),
                player.getEyeY(),
                player.getZ(),

                20,

                0.2,
                0.2,
                0.2,

                0.1
        );


        serverLevel.playSound(
                null,
                player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS,
                1.0F,
                0.6F
        );


        /*
         * Небольшая перезарядка дальней атаки:
         * 10 тиков = 0.5 секунды.
         */
        player.getCooldowns().addCooldown(
                stack,
                10
        );

        return InteractionResult.SUCCESS;
    }
}