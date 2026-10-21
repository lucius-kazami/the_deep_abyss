package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AbyssForgeScreen extends AbstractContainerScreen<AbyssForgeMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/gui/abyss_forge.png");

    private static final int TEX_W = 256;
    private static final int TEX_H = 256;

    // Стрелка прогресса (вниз): позиция на экране и размер
    private static final int ARROW_X = 84, ARROW_Y = 40, ARROW_W = 8, ARROW_H = 16;
    // Максимум, который возвращает menu.getCookProgress(). У печки это 24. Если у вас иначе, поправьте.
    private static final int COOK_MAX = 24;

    public AbyssForgeScreen(AbyssForgeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 10000; // убирает надпись «Инвентарь» за пределы экрана
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F,
                imageWidth, imageHeight, TEX_W, TEX_H);

        // Стрелка прогресса
        int cook = menu.getCookProgress();
        if (cook > 0) {
            int h = Math.min(ARROW_H, cook * ARROW_H / COOK_MAX);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + ARROW_X, y + ARROW_Y,
                    176.0F, 0.0F, ARROW_W, h, TEX_W, TEX_H);
        }

        // Полоски остатка заряда под слотами рубина и угля
        drawChargeBar(graphics, x + 44, y + 78, menu.getRubyCharge(), AbyssForgeBlockEntity.RUBY_USES, 0xFFD02030);
        drawChargeBar(graphics, x + 80, y + 78, menu.getCoalCharge(), AbyssForgeBlockEntity.COAL_USES, 0xFF2A2A2A);
    }

    private static void drawChargeBar(GuiGraphics graphics, int bx, int by, int charge, int max, int color) {
        graphics.fill(bx, by, bx + 16, by + 3, 0xFF555555);            // фон
        if (charge > 0) {
            graphics.fill(bx, by, bx + 16 * charge / max, by + 3, color); // заполнение
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}