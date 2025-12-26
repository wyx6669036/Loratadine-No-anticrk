package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import javax.annotation.Nullable;
import java.awt.*;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class Simple {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static LivingEntity lastTarget;
    private static float easingHealth = 0f;
    private static float currentWidth = 0;
    private static int alpha = 0;

    public static void drawTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, int x, int y) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 各种颜色
        Color bgColor = new Color(0, 0, 0, 160);

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;

        // 更新缓动血量动画
        updateEasingHealth(health);

        // 盔甲
        int armor = target != null ? target.getArmorValue() : 0;

        // 受伤间隔
        int offset = target != null ? -target.hurtTime * 23 : 0;

        // 目标宽度
        int targetWidth = (target != null && KillAura.target != null)
                ? (int) (mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + mc.font.width(String.valueOf(health)) + fontManager.icon18.getStringWidth("E") + mc.font.width(String.valueOf(armor)) + 10)
                : mc.font.width("TargetInfo") + 4;

        int height = mc.font.lineHeight + 4;

        // 计算时间差
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // 平滑过渡宽度
        float widthSpeed = 0.5f; // 宽度变化速度
        if (currentWidth < targetWidth) {
            currentWidth = Math.min(currentWidth + widthSpeed * deltaTime, targetWidth);
        } else if (currentWidth > targetWidth) {
            currentWidth = Math.max(currentWidth - widthSpeed * deltaTime, targetWidth);
        }

        // 平滑过渡透明度
        int alphaSpeed = 2; // 透明度变化速度
        if (target != null && KillAura.target != null) {
            alpha = Math.min(alpha + alphaSpeed, 255); // 渐显
        } else {
            alpha = Math.max(alpha - alphaSpeed, 0); // 渐隐
        }

        // 绘制背景
        RenderUtils.drawRoundedRect(poseStack, (float) x, (float) y, (int) currentWidth, height, 0, bgColor);
        RenderUtils.drawGradientRectU2D(poseStack, (float) x, (float) y, 1, height, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());

        // 绘制文字
        Color textColor = new Color(200, 200, 200, alpha);
        Color narmalTextColor = new Color(200, 200, 200, 255);
        WrapperUtils.drawShadow(poseStack, "TargetInfo", x + 2, y + 2.5F, narmalTextColor.getRGB());

        // 绘制头像
        if (target != null && KillAura.target != null || mc.screen instanceof ChatScreen) {
            AbstractClientPlayer player = (AbstractClientPlayer) target;
            RenderUtils.drawPlayerHead(poseStack, (float) x - 20, (float) y - 2, 18, 18, player);

            // 绘制名字
            String name = target.getName().getString();
            WrapperUtils.drawShadow(poseStack, name, x + 2, y + 15, new Color(200, 0, 0, alpha).getRGB());

            // 绘制血量
            fontManager.icon18.drawString(poseStack, "P",
                    (float) x + mc.font.width("TargetInfo") + 4,
                    (float) y + 4,
                    ColorUtils.getColor(255, 255 + offset, 255 + offset, alpha));

            WrapperUtils.drawShadow(poseStack, String.valueOf(health),
                    x + mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + 6,
                    y + 2.5F,
                    textColor.getRGB());

            // 绘制盔甲
            fontManager.icon18.drawString(poseStack, "E",
                    (float) x + mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + mc.font.width(String.valueOf(health)) + 7,
                    (float) y + 4,
                    textColor.getRGB());

            WrapperUtils.drawShadow(poseStack, String.valueOf(armor),
                    x + mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + mc.font.width(String.valueOf(health)) + fontManager.icon18.getStringWidth("E") + 8,
                    y + 2.5F,
                    textColor.getRGB());
        }
    }

    /**
     * 更新缓动动画血量值
     */
    private static void updateEasingHealth(float targetHealth) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float changeAmount = Math.abs(easingHealth - targetHealth);
        float baseSpeed = 0.02f; // 基础速度
        float speed = baseSpeed * deltaTime; // 根据时间差缩放速度

        if (changeAmount > 5) {
            speed *= 2.0f;
        } else if (changeAmount > 2) {
            speed *= 1.5f;
        }

        if (Math.abs(easingHealth - targetHealth) < 0.1) {
            easingHealth = targetHealth;
        } else if (easingHealth > targetHealth) {
            easingHealth -= Math.min(speed * 1.2f, easingHealth - targetHealth);
        } else {
            easingHealth += Math.min(speed, targetHealth - easingHealth);
        }
    }
}
