package shop.xmz.lol.loratadine.modules.impl.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.DragManager;
import shop.xmz.lol.loratadine.modules.impl.setting.Theme;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;

public class InventoryHUD extends DragManager {
    // 设置和常量
    public final ModeSetting mode = new ModeSetting("Inventory Mode", this, new String[]{"Classic", "LoraSense"}, "Simple");
    private static final FontManager fontManager = Loratadine.INSTANCE.getFontManager();
    private static final float TITLE_HEIGHT = 12;

    public InventoryHUD() {
        super("InventoryHUD", "背包显示", 165, 65);
        xPercentSetting.setValue(5);
        yPercentSetting.setValue(50);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc == null || mc.getWindow() == null) return;

        updatePosition();
        PoseStack poseStack = event.poseStack();

        // 根据模式渲染内容
        switch (mode.getValue()) {
            case "Classic" -> renderClassicMode(event, poseStack);
            case "LoraSense" -> renderLoraSenseMode(event, poseStack);
            default -> renderInventoryItems(event, poseStack, getX() + 2.0f, getY() + 2);
        }

        renderDragEffects(poseStack);
        updateHighlightAnimation();
    }

    /**
     * Classic
     */
    private void renderClassicMode(Render2DEvent event, PoseStack poseStack) {
        // 绘制标题栏
        RenderUtils.drawRoundedRect(poseStack, getX(), getY(), width, TITLE_HEIGHT, 0,
                ColorUtils.applyOpacity(Theme.INSTANCE.firstColor, 110));

        // 绘制内容区域
        RenderUtils.drawRoundedRect(poseStack, getX(), getY() + TITLE_HEIGHT, width, height - TITLE_HEIGHT, 0,
                new Color(0, 0, 0, 110));

        // 绘制标题文本和图标
        fontManager.tenacity20.drawString(poseStack, HUD.INSTANCE.languageValue.is("Chinese") ? "背包" : "Inventory",
                getX() + 12, getY(), new Color(255, 255, 255, 200).getRGB());
        fontManager.icon33.drawString(poseStack, "k", getX() + 1, getY(), new Color(255, 255, 255, 200).getRGB());

        // 渲染物品
        renderInventoryItems(event, poseStack, getX() + 2.0f, getY() + TITLE_HEIGHT + 2);
    }

    /**
     * LoraSense
     */
    private void renderLoraSenseMode(Render2DEvent event, PoseStack poseStack) {
        // 绘制整体背景和顶部渐变
        RenderUtils.drawRoundedRect(poseStack, getX(), getY(), width, height, 0.0f, new Color(23, 23, 23));
        RenderUtils.drawGradientRectL2R(poseStack, getX(), getY(), width, 1,
                HUD.INSTANCE.getColor(0).getRGB(), HUD.INSTANCE.getColor(4).getRGB());

        // 绘制标题文本居中
        fontManager.tenacity20.drawCenteredString(poseStack, "Inventory", getX() + width / 2, getY() + 4, -1);

        // 渲染物品
        renderInventoryItems(event, poseStack, getX() + 2.0f, getY() + TITLE_HEIGHT + 2);
    }

    /**
     * 渲染物品
     */
    private void renderInventoryItems(Render2DEvent event, PoseStack poseStack, float startX, float startY) {
        int itemX = (int) startX;
        int itemY = (int) startY;
        boolean hasStacks = false;

        // 渲染背包物品
        for (int i = 9; i <= 35; ++i) {
            Slot slot = mc.player.inventoryMenu.getSlot(i);
            ItemStack stack = slot.getItem();
            poseStack.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (!stack.isEmpty()) hasStacks = true;

            event.guiGraphics().renderItemDecorations(mc.font, stack, itemX, itemY);
            event.guiGraphics().renderItem(stack, itemX, itemY);

            poseStack.popPose();
            if (itemX < getX() + 144.0f) {
                itemX += 18;
            } else {
                itemX = (int) startX;
                itemY += 16;
            }
        }

        // 显示状态消息
        if (mc.screen instanceof InventoryScreen) {
            fontManager.tenacity20.drawString(poseStack, "Already in inventory",
                    39 + getX(), getY() + 22, new Color(255, 255, 255, 155).getRGB());
        } else if (!hasStacks) {
            fontManager.tenacity20.drawString(poseStack, "Empty... ",
                    70 + getX(), getY() + 22, new Color(255, 255, 255, 155).getRGB());
        }
    }
}