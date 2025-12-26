package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import javax.annotation.Nullable;
import java.awt.*;

public class LSD {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static LivingEntity lastTarget;
    private static float easingHealth = 0f;

    public static void drawTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int x, int y) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float maxHealth = target != null ? target.getMaxHealth() : 20;

        // 盔甲
        int armor = target != null ? target.getArmorValue() : 5;

        // 更新缓动血量
        updateEasingHealth(health, maxHealth);

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        int width = target != null ? (int) Math.max(140, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : 140;
        float barWidth = width - 60;

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        RenderUtils.drawRoundedRect(poseStack, x, y, width, 40, 2, new Color(31, 30, 29));

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

        // 绘制Target Name
        fontManager.zw22.drawString(poseStack, name, x + 41, y + 4, new Color(200, 200, 200, 255).getRGB());

        // 绘制血量图标
        fontManager.icon18.drawString(poseStack, "P", x + 41, y + 17, HUD.INSTANCE.getColor(1).getRGB());

        // 绘制盔甲图标
        fontManager.icon18.drawString(poseStack, "E", x + 41, y + 28.5F, Color.WHITE.getRGB());

        // 绘制血条
        Color healthBgColor = new Color(70, 70, 70);
        Color healthColor = HUD.INSTANCE.getColor(1);
        Color healthGradientColor = HUD.INSTANCE.getColor(4);

        drawSmoothArmorBar(
                poseStack,
                x + 50,
                y + 18.5F,
                barWidth,
                3,
                easingHealth,
                maxHealth,
                healthBgColor,
                healthColor,
                healthGradientColor
        );

        // 绘制盔甲条
        drawSmoothArmorBar(
                poseStack,
                x + 50,
                y + 30,
                barWidth,
                3,
                armor,
                20,
                new Color(70, 70, 70),
                Color.WHITE
        );

        // 结束绘制
        poseStack.popPose();
    }

    // 新增一个支持渐变的平滑血条绘制方法
    private static void drawSmoothArmorBar(PoseStack poseStack, float x, float y, float width, float height, float health, float maxHealth, Color bgColor, Color barColor, Color gradientColor) {
        RenderUtils.drawRectangle(poseStack, x, y, width, height, bgColor.getRGB());
        float healthPercent = Math.min(easingHealth / maxHealth, 1);
        float barWidth = healthPercent * width;

        if (gradientColor != null) {
            RenderUtils.drawGradientRectL2R(poseStack, x, y, barWidth, height, barColor.getRGB(), gradientColor.getRGB());
        } else {
            RenderUtils.drawRectangle(poseStack, x, y, barWidth, height, barColor.getRGB());
        }
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

    /**
     * 绘制平滑血条
     */
    private static void drawSmoothArmorBar(PoseStack poseStack, float x, float y, float width, float height, float armor, float maxHealth, Color bgColor, Color barColor) {
        RenderUtils.drawRoundedRect(poseStack, x, y, width, height, 0, bgColor);
        float healthPercent = armor / maxHealth;
        float barWidth = Math.min(healthPercent * width, width);

        RenderUtils.drawRoundedRect(poseStack, x, y, barWidth, height, 0, barColor);
    }
}
