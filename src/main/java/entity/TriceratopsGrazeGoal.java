package com.akashi.abyss.abyss.entity;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class TriceratopsGrazeGoal extends Goal {
    private final TriceratopsEntity dino;
    private int timer;

    public TriceratopsGrazeGoal(TriceratopsEntity dino) {
        this.dino = dino;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!dino.onGround() || dino.isInWater()) return false;
        return dino.getRandom().nextInt(dino.isBaby() ? 100 : 400) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return timer > 0;
    }

    @Override
    public void start() {
        timer = this.adjustedTickDelay(60);
        dino.setEating(true);
        dino.getNavigation().stop();
    }

    @Override
    public void tick() {
        timer = Math.max(0, timer - 1);
    }

    @Override
    public void stop() {
        if (timer == 0) dino.heal(2.0F);
        dino.setEating(false);
    }
}