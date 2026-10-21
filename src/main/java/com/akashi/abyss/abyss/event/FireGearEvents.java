package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = Abyss.MODID)
public class FireGearEvents {

    private static final float SWORD_FIRE_BONUS = 4.0F;
    private static final int SWORD_IGNITE_SECONDS = 6;
    private static final float BOW_FIRE_BONUS = 13.0F;
    private static final int BOW_IGNITE_SECONDS = 8;
    private static final float SHIELD_BURN_DAMAGE = 4.0F;
    private static final int SHIELD_IGNITE_SECONDS = 5;

    public static boolean hasFullSet(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(Abyss.FIRE_HELMET.get())
                && p.getItemBySlot(EquipmentSlot.CHEST).is(Abyss.FIRE_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(Abyss.FIRE_LEGGINGS.get())
                && p.getItemBySlot(EquipmentSlot.FEET).is(Abyss.FIRE_BOOTS.get());
    }

    // ---------- броня: огонь, лава, плавание в лаве ----------
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!hasFullSet(player)) return;

        // Клиент: плавание в лаве
        if (player.level().isClientSide()) {
            if (player.isInLava()) {
                swimInLava(player);
            }

            return;
        }
        // Сервер: полная защита от огня и лавы
        if (player.level() instanceof ServerLevel server && server.getGameTime() % 10 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
        }
        player.clearFire();
    }

    private static void swimInLava(Player player) {
        Vec3 dm = player.getDeltaMovement();
        double x = dm.x, y = dm.y + 0.02, z = dm.z;      // 0.02: компенсируем медленное погружение
        if (player.zza > 0.0F) {                          // вперёд: плывём туда, куда смотрим (включая вверх и вниз)
            Vec3 look = player.getLookAngle();
            x += look.x * 0.07;
            y += look.y * 0.07;
            z += look.z * 0.07;
        }
        if (player.isShiftKeyDown()) {
            y -= 0.05;                                    // Shift: погружаться
        }
        player.setDeltaMovement(x, y, z);
    }

    // ---------- кирка и топор: переплавка выпавших предметов ----------
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        boolean pickaxe = tool.is(Abyss.FIRE_PICKAXE.get());
        boolean axe = tool.is(Abyss.FIRE_AXE.get());
        if (!pickaxe && !axe) return;

        ServerLevel level = event.getLevel();
        BlockState state = event.getState();
        if (pickaxe && !isOre(state)) return;      // кирка переплавляет только руды; топор сжигает всё

        boolean changed = false;
        for (ItemEntity drop : event.getDrops()) {
            ItemStack smelted = smelt(level, drop.getItem());
            if (!smelted.isEmpty()) {
                drop.setItem(smelted);
                changed = true;
            }
        }
        if (changed) {
            BlockPos pos = event.getPos();
            level.sendParticles(ParticleTypes.FLAME,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.4F, 1.4F);
        }
    }

    private static boolean isOre(BlockState state) {
        if (state.is(Tags.Blocks.ORES)) return true;
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore");
    }

    private static ItemStack smelt(ServerLevel level, ItemStack in) {
        SingleRecipeInput input = new SingleRecipeInput(in);
        Optional<RecipeHolder<SmeltingRecipe>> recipe =
                level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
        if (recipe.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = recipe.get().value().assemble(input, level.registryAccess());
        if (out.isEmpty()) return ItemStack.EMPTY;
        out.setCount(out.getCount() * in.getCount());
        return out;
    }

    // ---------- меч, лук, щит ----------
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        boolean changed = false;

        // меч: «облик огня» +4 урона и поджог
        if (source.getEntity() instanceof Player player && source.getDirectEntity() == player
                && player.getMainHandItem().is(Abyss.FIRE_SWORD.get())) {
            amount += SWORD_FIRE_BONUS;
            victim.igniteForSeconds(SWORD_IGNITE_SECONDS);
            changed = true;
        }

        // лук: +13 огнём и поджог
        if (source.getDirectEntity() instanceof AbstractArrow arrow) {
            ItemStack weapon = arrow.getWeaponItem();
            if (weapon != null && weapon.is(Abyss.FIRE_BOW.get())) {
                amount += BOW_FIRE_BONUS;
                victim.igniteForSeconds(BOW_IGNITE_SECONDS);
                changed = true;
            }
        }

        // щит: заблокированный удар обжигает атаковавшего
        if (victim.isBlocking() && victim.getUseItem().is(Abyss.FIRE_SHIELD.get())
                && victim.level() instanceof ServerLevel server
                && source.getEntity() instanceof LivingEntity attacker && attacker != victim) {
            Vec3 from = source.getSourcePosition() != null ? source.getSourcePosition() : attacker.position();
            Vec3 toSource = from.subtract(victim.position()).multiply(1, 0, 1);
            Vec3 look = victim.getViewVector(1.0F).multiply(1, 0, 1);
            if (toSource.lengthSqr() > 1.0E-4 && look.lengthSqr() > 1.0E-4
                    && look.normalize().dot(toSource.normalize()) > 0.42) {      // удар спереди, в пределах ~130 градусов
                attacker.hurtServer(server, server.damageSources().inFire(), SHIELD_BURN_DAMAGE);
                attacker.igniteForSeconds(SHIELD_IGNITE_SECONDS);
                server.sendParticles(ParticleTypes.FLAME,
                        attacker.getX(), attacker.getY() + 1.0, attacker.getZ(), 15, 0.3, 0.4, 0.3, 0.03);
            }
        }

        if (changed) {
            event.setAmount(amount);
        }
    }
}
