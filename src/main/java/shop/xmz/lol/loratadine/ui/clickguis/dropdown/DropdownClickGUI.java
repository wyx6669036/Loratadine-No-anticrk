package shop.xmz.lol.loratadine.ui.clickguis.dropdown;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.DragManager;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static shop.xmz.lol.loratadine.utils.math.MathUtils.lerp;

public class DropdownClickGUI extends Screen implements Wrapper {
    public static final DropdownClickGUI INSTANCE = new DropdownClickGUI();
    private final List<Frame> frames;

    // HUD编辑模式相关变量
    private boolean hudEditMode = false;
    private List<Module> hudModules = new ArrayList<>();

    // 提供hudEditMode的getter方法供DragManager使用
    public boolean isHudEditMode() {
        return hudEditMode;
    }

    // 保存和取消按钮的属性和位置
    private float saveButtonWidth = 80, saveButtonHeight = 30;
    private float cancelButtonWidth = 80, cancelButtonHeight = 30;
    private float saveButtonX, saveButtonY;
    private float cancelButtonX, cancelButtonY;
    private float saveButtonActualY, cancelButtonActualY;

    // Edit HUD按钮的属性和位置
    private float editHudButtonWidth = 80;
    private float editHudButtonHeight = 20;
    private float editHudButtonX, editHudButtonY;
    private float editHudButtonActualX;

    // 按钮悬停动画
    private boolean isHoveringEditHudButton = false;
    private float editHudButtonHoverProgress = 0f;
    private boolean isHoveringSaveButton = false;
    private float saveButtonHoverProgress = 0f;
    private boolean isHoveringCancelButton = false;
    private float cancelButtonHoverProgress = 0f;
    private static final float HOVER_ANIM_SPEED = 0.1f;

    // 保存HUD模块列表
    private final List<DragManager> dragManagers = new ArrayList<>();

    // 动画状态控制
    private enum AnimationState {
        NONE,
        ENTER_HUD_EDIT,     // 进入HUD编辑模式
        EXIT_HUD_EDIT,      // 退出HUD编辑模式
        RETURN_TO_GUI       // 返回到主GUI
    }
    private AnimationState currentAnimState = AnimationState.NONE;

    // HUD编辑模式动画
    private float hudEditAnimProgress = 0f;
    private boolean isHudEditAnimActive = false;
    private long hudEditAnimStartTime;
    private static final float HUD_EDIT_ANIM_DURATION = 0.3f; // 动画持续时间（秒）

    // 主GUI动画
    private float animProgress = 0f;
    private boolean isClosing = false;
    private static final float ANIM_DURATION = 0.5f;
    private static final float OVERSHOOT_INTENSITY = 1.2f;
    private long animStartTime;

    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 15;
    private boolean reverseScroll = true;

    protected DropdownClickGUI() {
        super(Component.nullToEmpty("Click GUI"));
        frames = new ArrayList<>();
        int offset = 18;

        for (Category category : Category.values()) {
            frames.add(new Frame(offset, 20, 150, 20, category));
            offset += 155;
        }

        // 初始化时间戳
        animStartTime = System.currentTimeMillis();
        hudEditAnimStartTime = System.currentTimeMillis();

        // 收集HUD模块
        collectDraggableHUDModules();
        collectHudModules();
    }

    /**
     * 自动收集所有DraggableHUDModule类型的模块
     */
    private void collectDraggableHUDModules() {
        dragManagers.clear();

        for (Module module : Loratadine.INSTANCE.getModuleManager().getModules()) {
            if (module instanceof DragManager) {
                dragManagers.add((DragManager) module);
            }
        }
    }

    /**
     * 收集所有HUD模块
     */
    private void collectHudModules() {
        hudModules.clear();
        for (Module module : Loratadine.INSTANCE.getModuleManager().getModule(Category.RENDER)) {
            hudModules.add(module);
        }
    }

    /**
     * 保存所有HUD模块的原始位置
     */
    private void saveAllDraggableHUDPositions() {
        for (DragManager module : dragManagers) {
            if (module.isEnabled()) {
                module.saveOriginalPosition();
            }
        }
    }

    /**
     * 恢复所有HUD模块的原始位置
     */
    private void restoreAllDraggableHUDPositions() {
        for (DragManager module : dragManagers) {
            if (module.isEnabled()) {
                module.restoreOriginalPosition();
            }
        }
    }

    /**
     * 应用所有HUD模块的当前位置
     */
    private void applyAllDraggableHUDPositions() {
        for (DragManager module : dragManagers) {
            if (module.isEnabled()) {
                module.applyPosition();
            }
        }
    }

    /**
     * 更新HUD编辑按钮位置
     */
    private void updateHudEditButtonPositions() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Edit HUD按钮位于屏幕右上角
        editHudButtonX = screenWidth - editHudButtonWidth - 10;
        editHudButtonY = screenHeight - editHudButtonHeight - 10;

        // 保存和取消按钮位于屏幕顶部中央
        saveButtonX = screenWidth / 2 - saveButtonWidth - 5;
        saveButtonY = 10;
        cancelButtonX = screenWidth / 2 + 5;
        cancelButtonY = 10;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (mc == null || mc.player == null || mc.level == null) return;

        // 更新HUD编辑按钮位置
        updateHudEditButtonPositions();

        // 更新动画
        updateAnimation(partialTicks);
        updateHudEditAnimation();
        updateEditHudButtonAnimation();

        // 如果动画完成并且正在关闭，则关闭界面
        if (isClosing && animProgress <= 0f) {
            super.onClose();
            return;
        }

        // 渲染背景暗化
        float bgAlpha = Mth.lerp(animProgress, 0f, 0.4f);
        if (hudEditMode || isHudEditAnimActive) {
            // 在HUD编辑模式下使用不同的背景暗化值
            bgAlpha = 0.2f; // 较浅的背景，以便更容易看到HUD元素
        }
        renderBackgroundFade(guiGraphics, bgAlpha);

        // HUD编辑模式下，渲染Edit HUD界面
        if (hudEditMode || currentAnimState == AnimationState.ENTER_HUD_EDIT || currentAnimState == AnimationState.EXIT_HUD_EDIT) {
            renderHudEditMode(guiGraphics, mouseX, mouseY);

            // 如果还在HUD编辑模式或正在进行相关动画，不渲染主GUI
            if (hudEditMode ||
                    (currentAnimState == AnimationState.ENTER_HUD_EDIT && isHudEditAnimActive) ||
                    (currentAnimState == AnimationState.EXIT_HUD_EDIT && isHudEditAnimActive)) {
                // 仍然渲染Edit HUD按钮
                renderEditHudButton(guiGraphics, mouseX, mouseY);
                return;
            }
        }

        // 如果不在HUD编辑模式，正常渲染GUI
        float scale = calculateScale();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(screenWidth / 2f, screenHeight / 2f, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate(-screenWidth / 2f, -screenHeight / 2f, 0);
        guiGraphics.pose().translate(0, scrollOffset, 0);

        for (Frame frame : frames) {
            frame.render(guiGraphics,
                    (int) ((mouseX - screenWidth / 2f) / scale + screenWidth / 2f),
                    (int) ((mouseY - screenHeight / 2f) / scale + screenHeight / 2f - scrollOffset),
                    partialTicks
            );
            frame.updatePosition(mouseX, mouseY - scrollOffset);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.pose().popPose();

        // 渲染Edit HUD按钮（在屏幕右上角）
        renderEditHudButton(guiGraphics, mouseX, mouseY);
    }
    /**
     * Smooth easing function without bounce
     * Creates a natural acceleration/deceleration effect
     */
    private float smoothEasing(float progress) {
        if (progress >= 1.0f) return 1.0f;
        if (progress <= 0.0f) return 0.0f;

        // Cubic easing - provides smooth acceleration and deceleration
        return progress < 0.5f ?
                4 * progress * progress * progress :
                1 - (float)Math.pow(-2 * progress + 2, 3) / 2;
    }

    /**
     * Update the Edit HUD button animation with smooth easing
     */
    private void updateEditHudButtonAnimation() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // Update button hover animation
        if (isHoveringEditHudButton) {
            editHudButtonHoverProgress = Math.min(1.0f, editHudButtonHoverProgress + HOVER_ANIM_SPEED);
        } else {
            editHudButtonHoverProgress = Math.max(0.0f, editHudButtonHoverProgress - HOVER_ANIM_SPEED);
        }

        // Calculate target and start positions
        float targetX = editHudButtonX;
        float startX = screenWidth + 10; // Off-screen to the right

        // Handle different animation states
        if (currentAnimState == AnimationState.ENTER_HUD_EDIT ||
                currentAnimState == AnimationState.EXIT_HUD_EDIT ||
                hudEditMode) {
            // In HUD edit related modes, keep button off-screen
            editHudButtonActualX = startX;
        } else if (currentAnimState == AnimationState.RETURN_TO_GUI) {
            // Return to main GUI animation - button slides in from off-screen
            editHudButtonActualX = lerp(startX, targetX, smoothEasing(animProgress));
        } else if (isClosing) {
            // Closing animation - slide from current position to off-screen
            float progress = 1.0f - animProgress;
            editHudButtonActualX = lerp(targetX, startX, smoothEasing(progress));
        } else {
            // Opening animation - slide from off-screen
            editHudButtonActualX = lerp(startX, targetX, smoothEasing(animProgress));
        }
    }

    /**
     * Update HUD edit mode animations with smooth easing
     */
    private void updateHudEditAnimation() {
        // Update hover animations
        if (isHoveringSaveButton) {
            saveButtonHoverProgress = Math.min(1.0f, saveButtonHoverProgress + HOVER_ANIM_SPEED);
        } else {
            saveButtonHoverProgress = Math.max(0.0f, saveButtonHoverProgress - HOVER_ANIM_SPEED);
        }

        if (isHoveringCancelButton) {
            cancelButtonHoverProgress = Math.min(1.0f, cancelButtonHoverProgress + HOVER_ANIM_SPEED);
        } else {
            cancelButtonHoverProgress = Math.max(0.0f, cancelButtonHoverProgress - HOVER_ANIM_SPEED);
        }

        // Update Save and Cancel buttons' slide positions and opacity
        if (isHudEditAnimActive) {
            long elapsedTime = System.currentTimeMillis() - hudEditAnimStartTime;
            float duration = HUD_EDIT_ANIM_DURATION * 1000; // Convert to milliseconds

            if (currentAnimState == AnimationState.ENTER_HUD_EDIT) {
                // Animation for entering HUD edit mode
                hudEditAnimProgress = Math.min(1.0f, elapsedTime / duration);

                // Calculate buttons' slide-in positions with smooth easing
                float progress = smoothEasing(hudEditAnimProgress);
                float startY = -saveButtonHeight - 10; // Off-screen above

                // 添加轻微的延迟效果，让保存按钮先进入，取消按钮稍后进入
                float saveProgress = progress;
                float cancelProgress = Math.max(0, (progress - 0.1f) / 0.9f); // 给取消按钮添加0.1秒延迟

                saveButtonActualY = lerp(startY, saveButtonY, saveProgress);
                cancelButtonActualY = lerp(startY, cancelButtonY, cancelProgress);

                if (hudEditAnimProgress >= 1.0f) {
                    hudEditAnimProgress = 1.0f;
                    isHudEditAnimActive = false;
                    currentAnimState = AnimationState.NONE;
                }
            } else if (currentAnimState == AnimationState.EXIT_HUD_EDIT) {
                // Animation for exiting HUD edit mode
                hudEditAnimProgress = Math.max(0.0f, 1.0f - (elapsedTime / duration));

                // Calculate buttons' slide-out positions with smooth easing
                float progress = smoothEasing(1.0f - hudEditAnimProgress);
                float endY = -saveButtonHeight - 10; // Off-screen above

                // 添加轻微的延迟效果，让取消按钮先退出，保存按钮稍后退出
                float cancelProgress = progress;
                float saveProgress = Math.max(0, (progress - 0.1f) / 0.9f); // 给保存按钮添加0.1秒延迟

                saveButtonActualY = lerp(saveButtonY, endY, saveProgress);
                cancelButtonActualY = lerp(cancelButtonY, endY, cancelProgress);

                if (hudEditAnimProgress <= 0.0f) {
                    hudEditAnimProgress = 0.0f;
                    isHudEditAnimActive = false;

                    // Start returning to GUI after Save/Cancel buttons exit
                    currentAnimState = AnimationState.RETURN_TO_GUI;

                    // Start animation for returning to GUI
                    animProgress = 0f;
                    animStartTime = System.currentTimeMillis();
                }
            }
        } else {
            // Set button positions based on current state when no active animation
            if (hudEditMode) {
                // Buttons at normal positions in edit mode
                saveButtonActualY = saveButtonY;
                cancelButtonActualY = cancelButtonY;
            } else {
                // Buttons off-screen when not in edit mode
                saveButtonActualY = -saveButtonHeight - 10;
                cancelButtonActualY = -cancelButtonHeight - 10;
            }
        }
    }

    /**
     * Render Edit HUD button with a modern transparent design
     */
    private void renderEditHudButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TrueTypeFont regularFont = Loratadine.INSTANCE.getFontManager().tenacity16;

        // Detect button hover
        boolean hoveringEditHud = isHovering(editHudButtonActualX, editHudButtonY,
                editHudButtonWidth, editHudButtonHeight, mouseX, mouseY);

        // Update hover state
        if (hoveringEditHud != isHoveringEditHudButton) {
            isHoveringEditHudButton = hoveringEditHud;
        }

        // Calculate button colors based on hover animation progress
        // Use more transparent colors for a modern look
        Color baseColor = new Color(20, 20, 30, 120);         // More transparent dark background
        Color hoverColor = new Color(40, 40, 60, 160);       // Slightly more opaque when hovering

        int r = (int)lerp(baseColor.getRed(), hoverColor.getRed(), editHudButtonHoverProgress);
        int g = (int)lerp(baseColor.getGreen(), hoverColor.getGreen(), editHudButtonHoverProgress);
        int b = (int)lerp(baseColor.getBlue(), hoverColor.getBlue(), editHudButtonHoverProgress);
        int a = (int)lerp(baseColor.getAlpha(), hoverColor.getAlpha(), editHudButtonHoverProgress);

        Color buttonBgColor = new Color(r, g, b, a);

        // Draw a simple rectangle without rounded corners
        RenderUtils.drawRectangle(guiGraphics.pose(), editHudButtonActualX, editHudButtonY,
                editHudButtonWidth, editHudButtonHeight, buttonBgColor.getRGB());

        // Add a subtle border/accent at the bottom
        Color accentColor = new Color(80, 120, 255, (int)(180 * editHudButtonHoverProgress + 75));
        RenderUtils.drawRectangle(guiGraphics.pose(),
                editHudButtonActualX,
                editHudButtonY + editHudButtonHeight - 1,
                editHudButtonWidth * Math.min(1.0f, editHudButtonHoverProgress + 0.3f),
                1,
                accentColor.getRGB());

        // Draw text with a subtle glow effect when hovered
        regularFont.drawCenteredString(guiGraphics.pose(), "Edit HUD",
                editHudButtonActualX + editHudButtonWidth / 2,
                editHudButtonY + editHudButtonHeight / 2 - regularFont.getHeight() / 2,
                new Color(255, 255, 255, 180 + (int)(75 * editHudButtonHoverProgress)).getRGB());
    }

    /**
     * Render HUD edit mode interface with modern transparent buttons
     */
    private void renderHudEditMode(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TrueTypeFont boldFont = Loratadine.INSTANCE.getFontManager().tenacityBold20;
        TrueTypeFont regularFont = Loratadine.INSTANCE.getFontManager().tenacity16;

        // 计算按钮透明度 - 基于位置计算渐隐效果
        // 当按钮位置在目标位置和屏幕外之间时，应用不同的透明度
        float saveButtonOpacity, cancelButtonOpacity;

        if (currentAnimState == AnimationState.EXIT_HUD_EDIT) {
            // 渐隐效果 - 基于当前Y位置计算
            float maxY = saveButtonY;
            float minY = -saveButtonHeight - 10; // 屏幕外位置

            // 计算按钮位置在[minY, maxY]范围内的相对位置 (0-1)
            float saveRelativePos = (saveButtonActualY - minY) / (maxY - minY);
            float cancelRelativePos = (cancelButtonActualY - minY) / (maxY - minY);

            // 应用平滑的透明度过渡
            saveButtonOpacity = smoothEasing(saveRelativePos);
            cancelButtonOpacity = smoothEasing(cancelRelativePos);
        } else if (currentAnimState == AnimationState.ENTER_HUD_EDIT) {
            // 渐现效果 - 基于当前Y位置计算
            float maxY = saveButtonY;
            float minY = -saveButtonHeight - 10; // 屏幕外位置

            // 计算按钮位置在[minY, maxY]范围内的相对位置 (0-1)
            float saveRelativePos = (saveButtonActualY - minY) / (maxY - minY);
            float cancelRelativePos = (cancelButtonActualY - minY) / (maxY - minY);

            // 应用平滑的透明度过渡
            saveButtonOpacity = smoothEasing(saveRelativePos);
            cancelButtonOpacity = smoothEasing(cancelRelativePos);
        } else {
            // 非动画状态下的透明度
            saveButtonOpacity = hudEditMode ? 1.0f : 0.0f;
            cancelButtonOpacity = hudEditMode ? 1.0f : 0.0f;
        }

        // Detect button hover states
        boolean hoveringSave = isHovering(saveButtonX, saveButtonActualY, saveButtonWidth, saveButtonHeight, mouseX, mouseY);
        boolean hoveringCancel = isHovering(cancelButtonX, cancelButtonActualY, cancelButtonWidth, cancelButtonHeight, mouseX, mouseY);

        // Update hover states
        if (hoveringSave != isHoveringSaveButton) {
            isHoveringSaveButton = hoveringSave;
        }
        if (hoveringCancel != isHoveringCancelButton) {
            isHoveringCancelButton = hoveringCancel;
        }

        // Save button colors - match EditHUD button transparency
        Color saveBaseColor = new Color(40, 100, 40, (int)(120 * saveButtonOpacity));
        Color saveHoverColor = new Color(50, 150, 50, (int)(160 * saveButtonOpacity));

        int saveR = (int)lerp(saveBaseColor.getRed(), saveHoverColor.getRed(), saveButtonHoverProgress);
        int saveG = (int)lerp(saveBaseColor.getGreen(), saveHoverColor.getGreen(), saveButtonHoverProgress);
        int saveB = (int)lerp(saveBaseColor.getBlue(), saveHoverColor.getBlue(), saveButtonHoverProgress);
        int saveA = (int)lerp(saveBaseColor.getAlpha(), saveHoverColor.getAlpha(), saveButtonHoverProgress);

        Color saveBgColor = new Color(saveR, saveG, saveB, saveA);

        // Cancel button colors - match EditHUD button transparency
        Color cancelBaseColor = new Color(100, 40, 40, (int)(120 * cancelButtonOpacity));
        Color cancelHoverColor = new Color(150, 50, 50, (int)(160 * cancelButtonOpacity));

        int cancelR = (int)lerp(cancelBaseColor.getRed(), cancelHoverColor.getRed(), cancelButtonHoverProgress);
        int cancelG = (int)lerp(cancelBaseColor.getGreen(), cancelHoverColor.getGreen(), cancelButtonHoverProgress);
        int cancelB = (int)lerp(cancelBaseColor.getBlue(), cancelHoverColor.getBlue(), cancelButtonHoverProgress);
        int cancelA = (int)lerp(cancelBaseColor.getAlpha(), cancelHoverColor.getAlpha(), cancelButtonHoverProgress);

        Color cancelBgColor = new Color(cancelR, cancelG, cancelB, cancelA);

        // Draw Save button - flat and modern
        RenderUtils.drawRectangle(guiGraphics.pose(), saveButtonX, saveButtonActualY,
                saveButtonWidth, saveButtonHeight, saveBgColor.getRGB());

        // Add accent line at the bottom of Save button
        Color saveAccentColor = new Color(100, 255, 100, (int)(255 * saveButtonOpacity * (0.5f + saveButtonHoverProgress * 0.5f)));
        RenderUtils.drawRectangle(guiGraphics.pose(),
                saveButtonX,
                saveButtonActualY + saveButtonHeight - 2,
                saveButtonWidth,
                2,
                saveAccentColor.getRGB());

        boldFont.drawCenteredString(guiGraphics.pose(), "Save",
                saveButtonX + saveButtonWidth / 2,
                saveButtonActualY + saveButtonHeight / 2 - boldFont.getHeight() / 2,
                new Color(255, 255, 255, (int)(230 * saveButtonOpacity)).getRGB());

        // Draw Cancel button - flat and modern
        RenderUtils.drawRectangle(guiGraphics.pose(), cancelButtonX, cancelButtonActualY,
                cancelButtonWidth, cancelButtonHeight, cancelBgColor.getRGB());

        // Add accent line at the bottom of Cancel button
        Color cancelAccentColor = new Color(255, 100, 100, (int)(255 * cancelButtonOpacity * (0.5f + cancelButtonHoverProgress * 0.5f)));
        RenderUtils.drawRectangle(guiGraphics.pose(),
                cancelButtonX,
                cancelButtonActualY + cancelButtonHeight - 2,
                cancelButtonWidth,
                2,
                cancelAccentColor.getRGB());

        boldFont.drawCenteredString(guiGraphics.pose(), "Cancel",
                cancelButtonX + cancelButtonWidth / 2,
                cancelButtonActualY + cancelButtonHeight / 2 - boldFont.getHeight() / 2,
                new Color(255, 255, 255, (int)(230 * cancelButtonOpacity)).getRGB());

        // 指令文本的透明度也应该基于按钮的平均透明度
        float textOpacity = (saveButtonOpacity + cancelButtonOpacity) / 2;

        // Instruction text - more subtle appearance
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        float textX = screenWidth / 2 - regularFont.getStringWidth("HUD Edit Mode - Drag HUD elements to reposition") / 2;
        float textY = saveButtonActualY + saveButtonHeight + 10;

        // Draw instruction text with a subtle background for better readability
        float textWidth = regularFont.getStringWidth("HUD Edit Mode - Drag HUD elements to reposition");
        float textPadding = 10;
        RenderUtils.drawRectangle(guiGraphics.pose(),
                textX - textPadding,
                textY - textPadding / 2,
                textWidth + textPadding * 2,
                regularFont.getHeight() + textPadding,
                new Color(20, 20, 20, (int)(100 * textOpacity)).getRGB());

        regularFont.drawString(guiGraphics.pose(), "HUD Edit Mode - Drag HUD elements to reposition",
                textX, textY, new Color(255, 255, 150, (int)(230 * textOpacity)).getRGB());
    }

    private void updateAnimation(float partialTicks) {
        // 只有在非HUD编辑模式下才更新主GUI动画
        if (currentAnimState == AnimationState.NONE || currentAnimState == AnimationState.RETURN_TO_GUI) {
            long currentTime = System.currentTimeMillis();
            float elapsed = (currentTime - animStartTime) / 1000f;

            if (isClosing) {
                animProgress = Mth.clamp(1f - (elapsed / ANIM_DURATION), 0f, 1f); // Decrease progress when closing
                if (animProgress <= 0f) {
                    if (!hudEditMode) { // 只在非HUD编辑模式下关闭
                        super.onClose();
                    }
                }
            } else {
                animProgress = Mth.clamp(elapsed / ANIM_DURATION, 0f, 1f); // Increase progress when opening
                if (animProgress >= 1f && currentAnimState == AnimationState.RETURN_TO_GUI) {
                    // 返回GUI动画完成
                    currentAnimState = AnimationState.NONE;
                }
            }
        }
    }

    /**
     * 启动HUD编辑模式并开始动画
     */
    private void startEnterHudEditAnimation() {
        hudEditMode = true;
        currentAnimState = AnimationState.ENTER_HUD_EDIT;
        isHudEditAnimActive = true;
        hudEditAnimProgress = 0.0f;
        hudEditAnimStartTime = System.currentTimeMillis();

        // 重置按钮悬停状态
        isHoveringSaveButton = false;
        saveButtonHoverProgress = 0f;
        isHoveringCancelButton = false;
        cancelButtonHoverProgress = 0f;

        // 初始化按钮动画起始位置（从屏幕上方开始）
        saveButtonActualY = -saveButtonHeight - 10;
        cancelButtonActualY = -cancelButtonHeight - 10;

        // 备份所有HUD模块的原始位置
        saveAllDraggableHUDPositions();
    }

    /**
     * 启动退出HUD编辑模式并保存更改的动画
     */
    private void startSaveHudChangesAnimation() {
        currentAnimState = AnimationState.EXIT_HUD_EDIT;
        isHudEditAnimActive = true;
        hudEditAnimStartTime = System.currentTimeMillis();

        // 应用所有HUD模块的当前位置
        applyAllDraggableHUDPositions();

        // 设置标志位，动画完成后会关闭HUD编辑模式
        hudEditMode = false;
    }

    /**
     * 启动退出HUD编辑模式并取消更改的动画
     */
    private void startCancelHudChangesAnimation() {
        currentAnimState = AnimationState.EXIT_HUD_EDIT;
        isHudEditAnimActive = true;
        hudEditAnimStartTime = System.currentTimeMillis();

        // 恢复所有HUD模块的原始位置
        restoreAllDraggableHUDPositions();

        // 设置标志位，动画完成后会关闭HUD编辑模式
        hudEditMode = false;
    }

    private float calculateScale() {
        return easeOutBack(animProgress);
    }

    /**
     * 计算带有轻微弹性的动画进度
     */
    private float calculateElasticProgress(float progress) {
        if (progress >= 1.0f) {
            return 1.0f;
        }

        // 使用缓出函数
        float easedProgress = easeOutQuad(progress);

        // 在接近结束时添加轻微的弹性
        if (progress > 0.9f) {
            float overshoot = (progress - 0.9f) / 0.1f; // 0->1
            float bounce = (float) Math.sin(overshoot * Math.PI) * 0.2f;
            easedProgress = Math.min(1.0f, easedProgress + bounce);
        }

        return easedProgress;
    }

    /**
     * 二次方缓出函数 - 使动画开始快结束慢
     */
    private float easeOutQuad(float x) {
        return 1 - (1 - x) * (1 - x);
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f * OVERSHOOT_INTENSITY;
        float c3 = c1 + 1f;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private void renderBackgroundFade(GuiGraphics guiGraphics, float alpha) {
        int alphaValue = (int) (Mth.clamp(alpha, 0f, 0.4f) * 255);
        guiGraphics.fill(0, 0, this.width, this.height, (alphaValue << 24));
    }

    @Override
    public void onClose() {
        // 如果在HUD编辑模式，退出并取消更改
        if (hudEditMode) {
            startCancelHudChangesAnimation();
            return;
        }

        if (!isClosing) {
            isClosing = true;
            animStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;

        // 在HUD编辑模式下禁用滚动
        if (hudEditMode || isHudEditAnimActive) {
            return true;
        }

        scrollOffset += (delta > 0 ? (reverseScroll ? SCROLL_AMOUNT : -SCROLL_AMOUNT) : (reverseScroll ? -SCROLL_AMOUNT : SCROLL_AMOUNT));
        scrollOffset = (int) (scrollOffset * 0.9);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;

        // 如果在HUD编辑模式下，优先处理HUD组件的鼠标释放
        if (hudEditMode) {
            for (DragManager module : dragManagers) {
                if (module.isEnabled()) {
                    module.handleMouseReleased(mouseX, mouseY, button);
                }
            }
            return true;
        }

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY - scrollOffset, button); // Adjust mouse Y coordinate
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;

        // 如果正在进行HUD编辑模式的动画，不处理点击
        if (isHudEditAnimActive) return false;

        // 如果在HUD编辑模式下，检查是否点击了保存或取消按钮
        if (hudEditMode) {
            // 检查是否点击了保存按钮 - 使用动画后的实际位置
            if (isHovering(saveButtonX, saveButtonActualY, saveButtonWidth, saveButtonHeight, mouseX, mouseY) && button == 0) {
                startSaveHudChangesAnimation();
                return true;
            }

            // 检查是否点击了取消按钮 - 使用动画后的实际位置
            if (isHovering(cancelButtonX, cancelButtonActualY, cancelButtonWidth, cancelButtonHeight, mouseX, mouseY) && button == 0) {
                startCancelHudChangesAnimation();
                return true;
            }

            // 在HUD编辑模式下，优先处理HUD模块的拖动
            for (DragManager module : dragManagers) {
                if (module.isEnabled() && module.handleMouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }

            return true; // 在HUD编辑模式下，阻止其他点击操作
        }

        // 检查是否点击了Edit HUD按钮（在屏幕右上角）
        if (isHovering(editHudButtonActualX, editHudButtonY, editHudButtonWidth, editHudButtonHeight, mouseX, mouseY) && button == 0) {
            startEnterHudEditAnimation();
            return true;
        }

        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY - scrollOffset, button); // Adjust mouse Y coordinate
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // 如果在HUD编辑模式下，优先处理HUD组件的拖动
        if (hudEditMode) {
            for (DragManager module : dragManagers) {
                if (module.isEnabled()) {
                    module.handleMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 1) {  // Escape key
            // 如果在HUD编辑模式，按ESC退出编辑模式并取消更改
            if (hudEditMode) {
                startCancelHudChangesAnimation();
                return true;
            }

            // 否则正常关闭GUI
            onClose();
            return true;
        }

        // 在HUD编辑模式下禁用键盘输入
        if (hudEditMode) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isAnimationFinished() {
        return isClosing ? animProgress <= 0f : animProgress >= 1f;
    }

    private boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    protected void init() {
        if (mc == null || mc.player == null || mc.level == null) return;

        animProgress = 0f;
        isClosing = false;
        scrollOffset = 0;

        // 初始化HUD编辑模式动画变量
        hudEditAnimProgress = 0f;
        isHudEditAnimActive = false;
        currentAnimState = AnimationState.NONE;

        // 初始化按钮悬停动画变量
        editHudButtonHoverProgress = 0f;
        isHoveringEditHudButton = false;
        saveButtonHoverProgress = 0f;
        isHoveringSaveButton = false;
        cancelButtonHoverProgress = 0f;
        isHoveringCancelButton = false;

        // 更新按钮位置
        updateHudEditButtonPositions();

        // 初始化按钮动画位置
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        editHudButtonActualX = screenWidth + 10; // 屏幕外右侧
        saveButtonActualY = -saveButtonHeight - 10; // 屏幕顶部外部
        cancelButtonActualY = -cancelButtonHeight - 10; // 屏幕顶部外部

        // 重新设置动画开始时间
        animStartTime = System.currentTimeMillis();

        super.init();
    }
}