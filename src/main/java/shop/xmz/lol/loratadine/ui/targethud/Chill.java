package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.CharUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import javax.annotation.Nullable;
import java.awt.*;

public class Chill {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static LivingEntity lastTarget;
    private static float easingHealth = 0f;

    public static void drawTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int x, int y) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();
        final CharUtils numberRenderer = new CharUtils();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 血量
        float health = target != null ? target.getHealth() : 0;
        float maxHealth = target != null ? target.getMaxHealth() : 20;

        // 更新缓动血量动画
        updateEasingHealth(health);

        // 计算宽度
        float tWidth = Math.max(45F + Math.max(fontManager.zw22.getStringWidth(name), fontManager.zw22.getStringWidth(String.format("%.2f", health))), 120F);

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) tWidth / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 背景
        RenderUtils.drawGradientRectL2R(poseStack, x, y - 1, tWidth, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());
        RenderUtils.drawRoundedRect(poseStack, x, y, tWidth, 48F, 0, new Color(23, 23, 23));

        // 头像
        if (target != null) {
            try {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 4, y + 4, 30, 30, player);
            } catch (Exception e) {
                RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 30, 30, 0, Color.BLACK);
            }
        } else {
            RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 30, 30, 0, Color.BLACK);
        }

        // 名字和血量
        fontManager.zw22.drawString(poseStack, name, x + 38F, y + 5F, new Color(200, 200, 200, 255).getRGB());

        // 使用CharRenderer渲染血量数字
        float calcTranslateX = 0f; // 这里需要替换为实际的计算值
        float calcTranslateY = 5f; // 这里需要替换为实际的计算值
        float calcScaleX = 1f;     // 默认缩放为1
        float calcScaleY = 1f;     // 默认缩放为1

        numberRenderer.renderChar(
                poseStack,
                easingHealth, // 使用缓动的血量值
                calcTranslateX,
                calcTranslateY,
                x + 38F,
                y + 17F,
                calcScaleX,
                calcScaleY,
                false,
                0.5F, // 字体速度
                new Color(200, 200, 200, 255).getRGB()
        );

        // 绘制血条背景
        RenderUtils.drawRoundedRect(poseStack, x + 4, y + 38, tWidth - 8, 6, 0, new Color(0, 0, 0, 100));

        Color barColor;
        float healthPercent = easingHealth / maxHealth; // 使用缓动的血量值计算百分比

        if (healthPercent > 0.66f) {
            // 血量高 - 绿色
            barColor = new Color(30, 220, 30);
        } else if (healthPercent > 0.33f) {
            // 血量中等 - 黄色
            barColor = new Color(220, 220, 30);
        } else {
            // 血量低 - 红色
            barColor = new Color(220, 30, 30);
        }

        // 获取HUD颜色或使用上面计算的颜色
        Color finalBarColor = barColor;

        // 绘制血条
        RenderUtils.drawRoundedRect(poseStack, x + 4, y + 38, (easingHealth / maxHealth) * (tWidth - 8), 6, 0, finalBarColor);

        // 结束绘制
        poseStack.popPose();
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
