package shop.xmz.lol.loratadine.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.ui.clickguis.compact.CompactClickGUI;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.DropdownClickGUI;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;

/**
 * @author DSJ
 */
public abstract class DragManager extends Module {
    protected final NumberSetting xPercentSetting, yPercentSetting;
    @Getter
    protected float x = 0, y = 0, originalX = 0, originalY = 0;

    protected float cachedPixelX = 0, cachedPixelY = 0;
    protected boolean useDirectPosition = false;

    protected boolean dragging = false;
    protected float dragX, dragY;

    protected int lastScreenWidth = 0, lastScreenHeight = 0;
    protected boolean needsPositionUpdate = true;
    protected boolean isBeingDragged = false;

    protected float width, height;

    @Getter
    protected float coordAlpha = 0f;
    @Getter
    protected float coordScale = 0.7f;
    @Getter
    protected float currentCoordX = 0f;
    protected float targetCoordX = 0f;
    protected boolean isCoordOnLeft = false;

    // 动画
    protected float highlightAlpha = 0f;
    protected float highlightScale = 0.8f;
    protected boolean shouldAnimate = false;
    protected long lastAnimationTime = 0;
    protected static final float FADE_SPEED = 0.08f;    // 透明度渐变速度
    protected static final float SCALE_SPEED = 0.03f;   // 缩放动画速度
    protected static final float POSITION_SPEED = 0.12f; // 位置动画速度

    // 初始化
    protected boolean positionInitialized = false;

    public DragManager(String name, String description, float width, float height) {
        super(name, description, Category.RENDER);
        this.width = width;
        this.height = height;
        // 使用更高精度的步长值以保存更精确的百分比
        this.xPercentSetting = new NumberSetting("X Percent", this, 0, 0, 100, 0.0001);
        this.yPercentSetting = new NumberSetting("Y Percent", this, 0, 0, 100, 0.0001);
    }

    @Override
    public void onEnable() {
        positionInitialized = false;
        needsPositionUpdate = true;
        super.onEnable();
    }

    /**
     * 更新组件位置
     */
    protected void updatePosition() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 检查屏幕尺寸变化
        boolean screenSizeChanged = screenWidth != lastScreenWidth || screenHeight != lastScreenHeight;
        if (screenSizeChanged) {
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
            needsPositionUpdate = true;
        }

        // 处理位置计算
        if (!positionInitialized || needsPositionUpdate || screenSizeChanged) {
            double xPercent = xPercentSetting.getValue().doubleValue();
            double yPercent = yPercentSetting.getValue().doubleValue();

            boolean precisionLoss = xPercent == Math.floor(xPercent) && yPercent == Math.floor(yPercent)
                    && (x > 0 || y > 0);

            float newX, newY;

            if (precisionLoss && cachedPixelX > 0 && cachedPixelY > 0) {
                if (lastScreenWidth > 0 && lastScreenHeight > 0 && screenSizeChanged) {
                    float xRatio = (float)screenWidth / lastScreenWidth;
                    float yRatio = (float)screenHeight / lastScreenHeight;

                    newX = cachedPixelX * xRatio;
                    newY = cachedPixelY * yRatio;
                } else {
                    newX = cachedPixelX;
                    newY = cachedPixelY;
                }

                newX = Math.max(0, Math.min(newX, screenWidth - width));
                newY = Math.max(0, Math.min(newY, screenHeight - height));

                double updatedXPercent = (newX / (double)screenWidth) * 100.0;
                double updatedYPercent = (newY / (double)screenHeight) * 100.0;
                xPercentSetting.setValue(updatedXPercent);
                yPercentSetting.setValue(updatedYPercent);
            } else {
                xPercent = Math.max(0, Math.min(xPercent, 100.0));
                yPercent = Math.max(0, Math.min(yPercent, 100.0));

                newX = (float)(screenWidth * (xPercent / 100.0));
                newY = (float)(screenHeight * (yPercent / 100.0));
            }

            x = newX;
            y = newY;
            cachedPixelX = x;
            cachedPixelY = y;

            positionInitialized = true;
            needsPositionUpdate = false;
        } else if (isBeingDragged) {
            x = cachedPixelX;
            y = cachedPixelY;
        }

        // 更新动画
        updateCoordinateAnimations();
    }

    /**
     * 更新动画
     */
    private void updateCoordinateAnimations() {
        if (dragging) {
            coordAlpha = lerp(coordAlpha, 1.0f, FADE_SPEED);
            coordScale = lerp(coordScale, 1.0f, SCALE_SPEED);
        } else {
            coordAlpha = lerp(coordAlpha, 0.0f, FADE_SPEED);
            coordScale = lerp(coordScale, 0.7f, SCALE_SPEED);
        }

        // 更新坐标显示位置
        float[] position = calculateCoordinatePosition();
        targetCoordX = position[0];

        boolean shouldBeOnLeft = isCoordinateShouldBeOnLeft();

        if (shouldBeOnLeft != isCoordOnLeft) {
            isCoordOnLeft = shouldBeOnLeft;
        }

        currentCoordX = lerp(currentCoordX, targetCoordX, POSITION_SPEED);
    }

    /**
     * 线性插值
     */
    protected float lerp(float start, float end, float amount) {
        return start + amount * (end - start);
    }

    /**
     * 判断坐标是否应该显示在左侧
     */
    private boolean isCoordinateShouldBeOnLeft() {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        // 如果靠近右边缘，显示在左侧
        return x + width + 70 > screenWidth;
    }

    /**
     * 根据像素位置更新百分比设置
     * 确保精确转换像素位置到百分比
     */
    protected void updatePercentFromPosition() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 边界检查
        float safeX = Math.max(0, Math.min(x, screenWidth - width));
        float safeY = Math.max(0, Math.min(y, screenHeight - height));

        if (Math.abs(x - safeX) > 0.5f || Math.abs(y - safeY) > 0.5f) {
            x = safeX;
            y = safeY;
            cachedPixelX = x;
            cachedPixelY = y;
        }

        double xPercent = (x / (double)screenWidth) * 100.0;
        double yPercent = (y / (double)screenHeight) * 100.0;

        xPercent = Math.max(0, Math.min(xPercent, 100.0));
        yPercent = Math.max(0, Math.min(yPercent, 100.0));

        xPercentSetting.setValue(xPercent);
        yPercentSetting.setValue(yPercent);

        savePositionToPixelCache();
    }

    /**
     * 保存当前位置到像素缓存
     */
    private void savePositionToPixelCache() {
        // 可以在这里添加额外的缓存机制，比如保存到配置文件
        cachedPixelX = x;
        cachedPixelY = y;
    }

    public void setX(float x) {
        this.x = x;
        this.cachedPixelX = x;
        updatePercentFromPosition();
    }

    public void setY(float y) {
        this.y = y;
        this.cachedPixelY = y;
        updatePercentFromPosition();
    }

    // 位置保存和恢复
    public void saveOriginalPosition() {
        originalX = x;
        originalY = y;
    }

    public void restoreOriginalPosition() {
        x = originalX;
        y = originalY;
        cachedPixelX = x;
        cachedPixelY = y;
        updatePercentFromPosition();
    }

    public void applyPosition() {
        updatePercentFromPosition();
    }

    /**
     * 计算坐标显示的理想位置
     */
    public float[] calculateCoordinatePosition() {
        float coordX, coordY;

        // 默认在右侧显示
        if (!isCoordinateShouldBeOnLeft()) {
            coordX = x + width + 5;
        } else {
            // 在左侧显示
            coordX = x - 65;
        }

        coordY = y + height / 2;

        return new float[]{coordX, coordY};
    }

    /**
     * 更新高亮动画状态
     */
    protected void updateHighlightAnimation() {
        long currentTime = System.currentTimeMillis();

        float deltaTime = (currentTime - lastAnimationTime) / 1000f;
        lastAnimationTime = currentTime;

        deltaTime = Math.min(deltaTime, 0.05f);

        boolean shouldHighlight = shouldRenderDragHighlight();

        if (shouldHighlight != shouldAnimate) {
            shouldAnimate = shouldHighlight;
        }

        if (shouldAnimate) {
            highlightAlpha = Math.min(1.0f, highlightAlpha + (deltaTime * 5.0f));  // 200ms完成渐变
            highlightScale = Math.min(1.0f, highlightScale + (deltaTime * 1.0f));  // 1000ms完成缩放
        } else {
            highlightAlpha = Math.max(0.0f, highlightAlpha - (deltaTime * 5.0f));  // 200ms完成渐变
            highlightScale = Math.max(0.8f, highlightScale - (deltaTime * 1.0f));  // 1000ms完成缩放
        }
    }

    /**
     * 渲染拖动效果和坐标显示
     */
    protected void renderDragEffects(PoseStack poseStack) {
        // 更新高亮动画状态
        updateHighlightAnimation();

        // 如果高亮透明度足够，显示高亮效果
        if (highlightAlpha > 0.01f) {
            poseStack.pushPose();

            float centerX = getX() + width / 2;
            float centerY = getY() + height / 2;

            poseStack.translate(centerX, centerY, 0);
            poseStack.scale(highlightScale, highlightScale, 1.0f);
            poseStack.translate(-centerX, -centerY, 0);
            RenderUtils.drawRoundedRect(poseStack, getX(), getY(), width, height, 0, new Color(255, 255, 255, (int)(30 * highlightAlpha)));
            poseStack.popPose();
        }

        float alpha = getCoordAlpha();

        if (alpha > 0.01f) {
            float scale = getCoordScale();

            float coordX = getCurrentCoordX();
            float coordY = getY() + height / 2;

            String coords = String.format("X: %d Y: %d", (int)getX(), (int)getY());

            poseStack.pushPose();

            float textWidth = Loratadine.INSTANCE.getFontManager().tenacity20.getStringWidth(coords);
            float centerX = coordX + textWidth / 2;
            float centerY = coordY;

            poseStack.translate(centerX, centerY, 0);
            poseStack.scale(scale, scale, 1.0f);
            poseStack.translate(-centerX, -centerY, 0);
            Loratadine.INSTANCE.getFontManager().tenacity20.drawString(poseStack, coords, coordX, coordY, new Color(255, 255, 255, (int)(alpha * 255)).getRGB());
            poseStack.popPose();
        }
    }

    /**
     * 检查是否在HUD编辑模式中，确保只有在Edit HUD模式下才能移动HUD组件
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        // 检查是否在HUD编辑模式
        boolean inEditMode = false;

        if (mc.screen instanceof CompactClickGUI) {
            inEditMode = ((CompactClickGUI)mc.screen).isHudEditMode();
        } else if (mc.screen instanceof DropdownClickGUI) {
            inEditMode = ((DropdownClickGUI)mc.screen).isHudEditMode();
        }

        if (!inEditMode) {
            return false; // 如果不在HUD编辑模式，不允许拖动
        }

        boolean isOverComponent = isHovering(x, y, width, height, mouseX, mouseY);
        if (isOverComponent) {
            dragging = true;
            isBeingDragged = true;
            dragX = (float) mouseX - x;
            dragY = (float) mouseY - y;
            saveOriginalPosition();

            // 初始化坐标显示位置，避免闪烁
            if (currentCoordX == 0) {
                float[] pos = calculateCoordinatePosition();
                currentCoordX = pos[0];
                targetCoordX = pos[0];
            }

            return true;
        }
        return false;
    }

    /**
     * 在ClickGUI中处理鼠标释放事件
     */
    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            isBeingDragged = false;
            updatePercentFromPosition();
            return true;
        }
        return false;
    }

    /**
     * 在ClickGUI中处理鼠标拖动事件
     */
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            float newX = (float) (mouseX - dragX);
            float newY = (float) (mouseY - dragY);

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            newX = Math.max(0, Math.min(newX, screenWidth - width));
            newY = Math.max(0, Math.min(newY, screenHeight - height));

            this.x = newX;
            this.y = newY;

            this.cachedPixelX = newX;
            this.cachedPixelY = newY;

            // 更新
            updatePercentFromPosition();
            return true;
        }
        return false;
    }

    /**
     * 检查是否应该绘制拖动高亮
     */
    public boolean shouldRenderDragHighlight() {
        if (dragging) {
            if (mc.screen instanceof CompactClickGUI) {
                return ((CompactClickGUI)mc.screen).isHudEditMode();
            } else if (mc.screen instanceof DropdownClickGUI) {
                return ((DropdownClickGUI)mc.screen).isHudEditMode();
            }
        }
        return false;
    }

    /**
     * 检查鼠标是否在特定区域上方
     */
    protected boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}