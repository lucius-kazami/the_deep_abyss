package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.entity.EarthStoneProjectile;
import com.akashi.abyss.abyss.util.AbyssRay;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RawElementItem extends Item {

    public static final int MAX_USES = 5;

    public enum Element {
        FIRE,
        WATER,
        LIGHTNING,
        WIND,
        EARTH
    }

    private final Element element;

    public RawElementItem(Properties properties, Element element) {
        super(properties);
        this.element = element;
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        /*
         * =========================================================
         * ЗЕМЛЯ
         * =========================================================
         *
         * Земля не использует луч.
         * Она создаёт настоящий летящий камень.
         */
        if (element == Element.EARTH) {

            EarthStoneProjectile stone =
                    new EarthStoneProjectile(serverLevel, player);

            // Урон камня
            stone.setDamage(10.0F);

            // Запускаем туда, куда смотрит игрок
            stone.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    1.8F,   // скорость
                    0.5F    // разброс
            );

            serverLevel.addFreshEntity(stone);

            // Звук броска
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.SNOWBALL_THROW,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.7F
            );

            // Используем один заряд сырой руды
            ElementCharge.consume(player, hand);

            return InteractionResult.SUCCESS;
        }

        /*
         * =========================================================
         * ОСТАЛЬНЫЕ СТИХИИ
         * =========================================================
         */

        double range = switch (element) {
            case FIRE, WATER -> 14.0;
            case LIGHTNING -> 24.0;
            case WIND -> 9.0;

            // EARTH обработан выше
            case EARTH -> 0.0;
        };

        AbyssRay.Result ray =
                AbyssRay.trace(serverLevel, player, range);

        ParticleOptions particle = switch (element) {
            case FIRE -> ParticleTypes.FLAME;
            case WATER -> ParticleTypes.SPLASH;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case WIND -> ParticleTypes.SWEEP_ATTACK;

            // Земля обрабатывается раньше через EarthStoneProjectile,
            // поэтому сюда фактически не попадёт.
            case EARTH -> ParticleTypes.POOF;
        };

        AbyssRay.drawLine(
                serverLevel,
                ray.start(),
                ray.end(),
                particle
        );

        switch (element) {

            /*
             * ОГОНЬ
             */
            case FIRE -> {

                if (!ray.entities().isEmpty()) {

                    Entity target = ray.entities().get(0);

                    target.hurtServer(
                            serverLevel,
                            serverLevel.damageSources()
                                    .playerAttack(player),
                            4.0F
                    );

                    target.setRemainingFireTicks(100);
                }
            }

            /*
             * ВОДА
             */
            case WATER -> {

                if (!ray.entities().isEmpty()) {

                    Entity target = ray.entities().get(0);

                    target.hurtServer(
                            serverLevel,
                            serverLevel.damageSources()
                                    .playerAttack(player),
                            3.0F
                    );

                    target.extinguishFire();
                }
            }

            /*
             * МОЛНИЯ
             */
            case LIGHTNING -> {

                Vec3 strike =
                        ray.entities().isEmpty()
                                ? ray.end()
                                : ray.entities().get(0).position();

                LightningBolt bolt =
                        new LightningBolt(
                                EntityType.LIGHTNING_BOLT,
                                serverLevel
                        );

                bolt.setPos(
                        strike.x,
                        strike.y,
                        strike.z
                );

                serverLevel.addFreshEntity(bolt);
            }

            /*
             * ВЕТЕР
             */
            case WIND -> {

                // Ветер пробивает всех существ на линии
                for (Entity target : ray.entities()) {

                    target.hurtServer(
                            serverLevel,
                            serverLevel.damageSources()
                                    .playerAttack(player),
                            5.0F
                    );
                }
            }

            /*
             * Земля уже обработана выше
             */
            case EARTH -> {
            }
        }

        ElementCharge.consume(player, hand);

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ElementCharge.isUsed(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {

        return Math.round(
                13.0F
                        - ElementCharge.getUses(stack)
                        * 13.0F
                        / MAX_USES
        );
    }

    @Override
    public int getBarColor(ItemStack stack) {

        float f = Math.max(
                0.0F,
                (MAX_USES - ElementCharge.getUses(stack))
                        / (float) MAX_USES
        );

        return net.minecraft.util.Mth.hsvToRgb(
                f / 3.0F,
                1.0F,
                1.0F
        );
    }
}