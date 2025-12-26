package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import javax.annotation.Nullable;
import java.awt.*;

public class Modern {
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
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        // 更新缓动血量
        updateEasingHealth(health, maxHealth);

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        float width = fontManager.zw22.getStringWidth(name) + 75;
        float presentWidth = Math.min(healthPresent, 1) * width;

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        RenderUtils.drawRectangle(poseStack, x, y, width, 40, new Color(0, 0, 0, 100).getRGB());
        RenderUtils.drawRectangle(poseStack, x, y, presentWidth, 40, new Color(230, 230, 230, 100).getRGB());

        // 垂直血条指示
        Color healthColor = healthPresent > 0.5 ? new Color(63, 157, 4, 150) :
                (healthPresent > 0.25 ? new Color(255, 144, 2, 150) : new Color(168, 1, 1, 150));
        RenderUtils.drawRectangle(poseStack, x, y + 12.5f, 3, 15, healthColor.getRGB());

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 7, y + 7, 26, 26, player);
            } else {
                RenderUtils.drawRectangle(poseStack, x + 6, y + 6, 28, 28, Color.BLACK.getRGB());
            }
        } catch (Exception e) {
            RenderUtils.drawRectangle(poseStack, x + 6, y + 6, 28, 28, Color.BLACK.getRGB());
        }

        // 绘制文字
        fontManager.zw22.drawString(poseStack, name, x + 40, y + 7, new Color(200, 200, 200, 255).getRGB());
        fontManager.ax18.drawString(poseStack, health + " HP", x + 40, y + 22, new Color(200, 200, 200, 255).getRGB());

        // 绘制物品
        if (target != null && !target.getMainHandItem().isEmpty()) {
            RenderUtils.renderItemIcon(poseStack, x + fontManager.zw22.getStringWidth(name) + 50, y + 12, target.getMainHandItem());
        } else {
            fontManager.zw30.drawString(poseStack, "?", x + fontManager.zw22.getStringWidth(name) + 55, y + 11, new Color(200, 200, 200, 255).getRGB());
        }

        // 结束绘制
        poseStack.popPose();
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
