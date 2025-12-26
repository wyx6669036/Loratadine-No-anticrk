package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import javax.annotation.Nullable;
import java.awt.*;

public class Loratadine {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static LivingEntity lastTarget;
    private static float easingHealth = 0f;

    public static void drawTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int x, int y) {
        final FontManager fontManager = shop.xmz.lol.loratadine.Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float maxHealth = target != null ? target.getMaxHealth() : 20;
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        // 更新缓动血量
        updateEasingHealth(health, maxHealth);

        final Color color = healthPresent > 0.5 ? new Color(63, 157, 4, 150) :
                (healthPresent > 0.25 ? new Color(255, 144, 2, 150) : new Color(168, 1, 1, 150));

        // 受伤间隔
        int offset = target != null ? -target.hurtTime * 23 : 0;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        int width = target != null ? (int) Math.max(140, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : 140;
        float barWidth = width - 55;

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        RenderUtils.drawRoundedRect(poseStack, x, y, width, 40, 12, new Color(0, 0, 0, 160));

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 7, y + 7, 26, 26, player);
            } else {
                RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 28, 28, 6, Color.BLACK);
            }
        } catch (Exception e) {
            RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 28, 28, 6, Color.BLACK);
        }

        // 绘制文字
        fontManager.zw22.drawString(poseStack, name, x + 40, y + 5, new Color(200, 200, 200, 255).getRGB());
        fontManager.ax18.drawString(poseStack, String.valueOf(health), x + 40, y + 18, new Color(200, 200, 200, 255).getRGB());
        fontManager.icon30.drawString(poseStack, "P", x + fontManager.ax20.getStringWidth(String.valueOf(health)) + 42, y + 17, ColorUtils.getColor(255, 255 + offset, 255 + offset));

        // 绘制平滑血条
        drawSmoothHealthBar(
                poseStack,
                x + 40,
                y + 30,
                barWidth,
                4,
                easingHealth,
                maxHealth,
                new Color(0, 0, 0, 30),
                color
        );

        // 结束绘制
        poseStack.popPose();
    }

    /**
     * 绘制平滑血条
     */
    private static void drawSmoothHealthBar(PoseStack poseStack, float x, float y, float width, float height, float health, float maxHealth, Color bgColor, Color barColor) {
        RenderUtils.drawRoundedRect(poseStack, x, y, width, height, 0, bgColor);
        float healthPercent = easingHealth / maxHealth;
        float barWidth = Math.min(healthPercent * width, width);
        RenderUtils.drawRoundedRect(poseStack, x, y, barWidth, height, 0, barColor);
    }

    private static void updateEasingHealth(float targetHealth, float maxHealth) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float changeAmount = Math.abs(easingHealth - targetHealth);
        float baseSpeed = 0.02f;
        float speed = baseSpeed * deltaTime;

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
        easingHealth = Math.min(easingHealth, maxHealth);
    }
}
