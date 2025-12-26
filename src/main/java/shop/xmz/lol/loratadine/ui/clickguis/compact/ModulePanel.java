package shop.xmz.lol.loratadine.ui.clickguis.compact;

import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.ui.clickguis.compact.impl.ModuleRect;
import shop.xmz.lol.loratadine.ui.clickguis.compact.impl.ModuleScroll;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class ModulePanel implements Wrapper {
    public float x, y, rectWidth, rectHeight;
    public Category currentCategory;
    public List<ModuleRect> moduleRects;

    // 死妈滚动
    private final HashMap<Category, ModuleScroll> scrollHashMap = new HashMap<>();

    private float animationTime = 0f;

    private final float SCROLL_AMOUNT = 30f;
    private static final float DECORATION_WIDTH = 2f;
    private static final float DECORATION_RIGHT_MARGIN = 10f;
    private static final float DECORATION_HEIGHT_SMALL = 15f;
    private static final float DECORATION_HEIGHT_MEDIUM = 25f;
    private static final float DECORATION_HEIGHT_LARGE = 40f;
    private static final float DECORATION_SPACING = 15f;
    private static final float DECORATION_SPEED = 0.02f;

    public boolean typing;

    public ModulePanel() {
        for (Category category : Category.values()) {
            scrollHashMap.put(category, new ModuleScroll());
        }
        // 防止NPE，然后你全家死没人了
        scrollHashMap.put(null, new ModuleScroll());
    }

    public void initGui() {
        // 初始化所有模块矩形
        if (moduleRects != null) {
            moduleRects.forEach(ModuleRect::initGui);
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        typing = false;

        if (currentCategory == null || moduleRects == null) return;

        ModuleScroll moduleScroll = scrollHashMap.get(currentCategory);
        if (moduleScroll == null) {
            moduleScroll = new ModuleScroll();
            scrollHashMap.put(currentCategory, moduleScroll);
        }

        // 更新动画
        moduleScroll.updateScrollAnimation();
        updateDecorativeAnimation(partialTicks);

        float totalContentHeight = calculateTotalHeight();
        float visibleHeight = rectHeight;

        float maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        moduleScroll.setMaxScroll(maxScroll);

        // 玉面剪刀王
        guiGraphics.enableScissor((int)(x + 95), (int)y + 1, (int)(x + rectWidth - 20), (int)(y + rectHeight));

        // 渲染模块
        int count = 0;
        float leftColumnHeight = 0;
        float rightColumnHeight = 0;

        for (ModuleRect moduleRect : moduleRects) {
            boolean rightColumn = count % 2 == 1;

            moduleRect.rectWidth = (rectWidth - (90 + 40)) / 2f;
            moduleRect.width = rectWidth;
            moduleRect.height = rectHeight;

            moduleRect.x = x + 100 + (rightColumn ? moduleRect.rectWidth + 10 : 0);
            // 使用平滑滚动值定位模块
            moduleRect.y = y + 10 + (rightColumn ? rightColumnHeight : leftColumnHeight) + moduleScroll.getScroll();

            // 只渲染可见的模块
            if (moduleRect.y + moduleRect.rectHeight >= y && moduleRect.y <= y + rectHeight) {
                moduleRect.render(guiGraphics, mouseX, mouseY, partialTicks);
            }

            if (!typing) {
                typing = moduleRect.typing;
            }

            if (rightColumn) {
                rightColumnHeight += moduleRect.rectHeight + 10;
            } else {
                leftColumnHeight += moduleRect.rectHeight + 10;
            }
            count++;
        }

        // 玉面剪刀王
        guiGraphics.disableScissor();

        // 玉面剪刀王
        guiGraphics.enableScissor((int)(x + rectWidth - DECORATION_RIGHT_MARGIN - DECORATION_WIDTH - 5), (int)y + 1, (int)(x + rectWidth), (int)(y + rectHeight));

        // 绘制
        renderDecorativeElements(guiGraphics);

        // 玉面剪刀王
        guiGraphics.disableScissor();
    }

    private void updateDecorativeAnimation(float partialTicks) {
        animationTime += DECORATION_SPEED * partialTicks;
        if (animationTime > 1.0f) {
            animationTime -= 1.0f;
        }
    }

    private void renderDecorativeElements(GuiGraphics guiGraphics) {
        Color accentColor = HUD.INSTANCE.getColor(0);

        float decorX = x + rectWidth - DECORATION_RIGHT_MARGIN;

        float totalPatternHeight = DECORATION_HEIGHT_SMALL + DECORATION_HEIGHT_MEDIUM +
                DECORATION_HEIGHT_LARGE + DECORATION_SPACING * 3;

        int repetitions = (int)Math.ceil(rectHeight / totalPatternHeight) + 1;

        float startOffset = -totalPatternHeight * animationTime;

        // 绘制线条
        for (int i = 0; i < repetitions; i++) {
            float currentY = y + startOffset + i * totalPatternHeight;

            drawDecorativeLine(guiGraphics, decorX, currentY,
                    DECORATION_HEIGHT_LARGE, accentColor, 1.0f);

            currentY += DECORATION_HEIGHT_LARGE + DECORATION_SPACING;

            drawDecorativeLine(guiGraphics, decorX, currentY,
                    DECORATION_HEIGHT_MEDIUM, accentColor, 0.8f);

            currentY += DECORATION_HEIGHT_MEDIUM + DECORATION_SPACING;

            drawDecorativeLine(guiGraphics, decorX, currentY,
                    DECORATION_HEIGHT_SMALL, accentColor, 0.6f);

            currentY += DECORATION_HEIGHT_SMALL + DECORATION_SPACING;
            // 差不多得了...
        }
    }

    /**
     * 绘制单个装饰线条
     */
    private void drawDecorativeLine(GuiGraphics guiGraphics, float x, float y, float height, Color baseColor, float alpha) {
        // 警告：不需要手动检查可见性！！！因为玉面剪刀王会处理这个 :)
        Color color = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(255 * alpha));
        RenderUtils.drawRectangle(guiGraphics.pose(), x, y, DECORATION_WIDTH, height, color.getRGB());

        float glowWidth = 1.0f; // 低配版 XD
        Color glowColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(120 * alpha));

        RenderUtils.drawRectangle(guiGraphics.pose(), x - glowWidth, y, glowWidth, height, glowColor.getRGB());
    }

    private float calculateTotalHeight() {
        if (moduleRects == null) return 0;

        float leftColumnHeight = 0;
        float rightColumnHeight = 0;

        for (int i = 0; i < moduleRects.size(); i++) {
            ModuleRect moduleRect = moduleRects.get(i);

            if (i % 2 == 0) {
                leftColumnHeight += moduleRect.rectHeight + 10;
            } else {
                rightColumnHeight += moduleRect.rectHeight + 10;
            }
        }

        return Math.max(leftColumnHeight, rightColumnHeight);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (currentCategory == null || moduleRects == null) return;

        for (ModuleRect moduleRect : moduleRects) {
            if (moduleRect.y + moduleRect.rectHeight >= y && moduleRect.y <= y + rectHeight) {
                if (isHovering(moduleRect.x, moduleRect.y, moduleRect.rectWidth, moduleRect.rectHeight, mouseX, mouseY)) {
                    moduleRect.mouseClicked(mouseX, mouseY, button);
                    return;
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.mouseReleased(mouseX, mouseY, button));
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentCategory == null) return;

        ModuleScroll moduleScroll = scrollHashMap.get(currentCategory);
        if (moduleScroll == null) return;

        moduleScroll.onScroll((float)delta * SCROLL_AMOUNT);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.keyPressed(keyCode, scanCode, modifiers));
        }
    }

    private boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}