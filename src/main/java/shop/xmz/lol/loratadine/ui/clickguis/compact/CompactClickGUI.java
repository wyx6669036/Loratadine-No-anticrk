package shop.xmz.lol.loratadine.ui.clickguis.compact;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.DragManager;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.ui.clickguis.compact.impl.ModuleRect;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static shop.xmz.lol.loratadine.utils.math.MathUtils.lerp;

/**
 * Compact Click GUI
 * @author DSJ_
 */
public class CompactClickGUI extends Screen implements Wrapper {
    public static final CompactClickGUI INSTANCE = new CompactClickGUI();

    // 面板尺寸和位置
    private float rectWidth = 475;
    private float rectHeight = 300;
    private float x = 40, y = 40;

    // HUD编辑模式相关变量
    private boolean hudEditMode = false;
    private List<Module> hudModules = new ArrayList<>();

    // 动画流程控制
    private enum AnimationState {
        NONE,
        ENTER_HUD_EDIT,      // 进入HUD编辑模式
        EXIT_HUD_EDIT,       // 退出HUD编辑模式
        RETURN_TO_GUI        // 返回到主GUI
    }
    private AnimationState currentAnimState = AnimationState.NONE;

    // 提供hudEditMode的getter方法供DragManager使用
    public boolean isHudEditMode() {
        return hudEditMode;
    }

    // 修改保存和取消按钮位置，放在屏幕上部位置
    private float saveButtonWidth = 80, saveButtonHeight = 30;
    private float cancelButtonWidth = 80, cancelButtonHeight = 30;
    private float saveButtonX, saveButtonY;
    private float cancelButtonX, cancelButtonY;
    private float saveButtonActualX, cancelButtonActualX;

    // 将Edit HUD按钮移动到屏幕右上角，添加动画支持
    private float editHudButtonWidth = 80;
    private float editHudButtonHeight = 20;
    private float editHudButtonX, editHudButtonY;
    private float editHudButtonActualX; // 用于动画的实际X位置

    // 按钮悬停动画
    private boolean isHoveringEditHudButton = false;
    private float editHudButtonHoverProgress = 0f;
    private boolean isHoveringSaveButton = false;
    private float saveButtonHoverProgress = 0f;
    private boolean isHoveringCancelButton = false;
    private float cancelButtonHoverProgress = 0f;
    private static final float HOVER_ANIM_SPEED = 0.1f;

    // Save和Cancel按钮滑动动画
    private float saveButtonActualY, cancelButtonActualY; // 用于垂直方向的动画
    private boolean waitingForButtonExit = false;

    // HUD编辑模式动画
    private float hudEditAnimProgress = 0f;
    private boolean isHudEditAnimActive = false;
    private boolean isEnteringHudEdit = false;
    private long hudEditAnimStartTime;
    private static final float HUD_EDIT_ANIM_DURATION = 0.3f; // 动画持续时间（秒）

    // 拖动支持
    private boolean dragging = false;
    private float dragX, dragY;

    // 动画支持 - 优化为更丝滑的动画
    private float animProgress = 0f;
    private boolean isClosing = false;
    private static final float ANIM_DURATION = 0.3f; // 动画持续时间（秒）
    private long animStartTime; // 记录动画开始时间
    private static final float ELASTIC_FACTOR = 0.2f; // 弹性因子

    // 背景暗化支持
    private float backgroundDimAlpha = 0f;
    private static final float MAX_BACKGROUND_DIM = 180f; // 最大背景暗化程度 (0-255)

    // 模块面板
    private final ModulePanel modulePanel;

    // 当前选中的分类
    private Category activeCategory;

    // 模块矩形缓存
    private HashMap<Category, List<ModuleRect>> moduleRects;

    // 保存所有可拖动HUD模块的列表
    private final List<DragManager> dragManagers = new ArrayList<>();

    public boolean typing;

    // 每个分类的Y位置的缓存
    private final HashMap<Category, Float> categoryPositions = new HashMap<>();

    // 添加侧边栏分类切换动画所需的变量
    private Category previousCategory = null;
    private float categoryTransitionProgress = 1.0f; // 1.0表示动画完成
    private static final float CATEGORY_TRANSITION_DURATION = 0.5f; // 转换动画持续时间(秒)
    private boolean isTransitioning = false;
    private long categoryTransitionStartTime; // 记录分类切换动画开始时间

    protected CompactClickGUI() {
        super(Component.nullToEmpty("Compact Click GUI"));
        modulePanel = new ModulePanel();
        moduleRects = new HashMap<>();
        animStartTime = System.currentTimeMillis();
        hudEditAnimStartTime = System.currentTimeMillis();
        categoryTransitionStartTime = System.currentTimeMillis();

        // 自动收集所有DraggableHUDModule实例
        collectDraggableHUDModules();

        // 收集所有HUD模块
        for (Module module : Loratadine.INSTANCE.getModuleManager().getModule(Category.RENDER)) {
            hudModules.add(module);
        }
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
     * 在左侧边栏底部渲染用户头像、游戏名和当前时间
     */
    private void renderProfileSection(GuiGraphics guiGraphics, float x, float y, float sidebarWidth, float rectHeight) {
        // 区域参数
        float sectionHeight = 65; // 个人信息区域高度
        float sectionY = y + rectHeight - sectionHeight;

        // 背景 - 稍微暗一点区分
        RenderUtils.drawRectangle(guiGraphics.pose(), x, sectionY, sidebarWidth, sectionHeight,
                new Color(70, 70, 70).getRGB());

        // 上边线条
        RenderUtils.drawRectangle(guiGraphics.pose(), x + 5, sectionY + 5, sidebarWidth - 10, 1,
                new Color(100, 100, 100).getRGB());

        // 获取字体
        TrueTypeFont nameFont = Loratadine.INSTANCE.getFontManager().tenacityBold14;
        TrueTypeFont infoFont = Loratadine.INSTANCE.getFontManager().tenacity14;

        // 绘制玩家头像
        float avatarSize = 40;
        float avatarX = x + 10;
        float avatarY = sectionY + 15;

        // 绘制头像背景和边框
        RenderUtils.drawPlayerHead(guiGraphics.pose(), avatarX, avatarY, avatarSize, avatarSize, mc.player);

        // 尝试绘制边框 - 如果RenderUtils中有相应方法
        try {
            // 使用1像素边框
            RenderUtils.drawRectangle(guiGraphics.pose(), avatarX - 1, avatarY - 1, avatarSize + 2, 1,
                    new Color(90, 90, 90).getRGB()); // 上边框
            RenderUtils.drawRectangle(guiGraphics.pose(), avatarX - 1, avatarY + avatarSize, avatarSize + 2, 1,
                    new Color(90, 90, 90).getRGB()); // 下边框
            RenderUtils.drawRectangle(guiGraphics.pose(), avatarX - 1, avatarY, 1, avatarSize,
                    new Color(90, 90, 90).getRGB()); // 左边框
            RenderUtils.drawRectangle(guiGraphics.pose(), avatarX + avatarSize, avatarY, 1, avatarSize,
                    new Color(90, 90, 90).getRGB()); // 右边框
        } catch (Exception e) {
            // 忽略错误，如果RenderUtils没有相应方法
        }

        // 玩家名字
        String playerName = mc.player.getName().getString();
        nameFont.drawString(guiGraphics.pose(), playerName,
                avatarX + avatarSize + 5,
                avatarY + 8,
                -1);

        // 获取当前时间
        String timeString = getCurrentTimeString();
        infoFont.drawString(guiGraphics.pose(), timeString,
                avatarX + avatarSize + 5,
                avatarY + 22,
                new Color(200, 200, 200).getRGB());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (mc == null || mc.player == null || mc.level == null) return;

        // 更新HUD编辑按钮位置
        updateHudEditButtonPositions();

        // 更新动画
        updateAnimation();
        updateHudEditAnimation();
        updateCategoryTransition(); // 更新分类切换动画
        updateEditHudButtonAnimation(); // 更新Edit HUD按钮动画

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
                return;
            }
        }

        // 应用平滑的动画效果
        float animX;

        // 处理进入和退出动画
        if (isClosing) {
            // 关闭动画：从当前位置滑出到屏幕外
            float progress = 1.0f - animProgress; // 反转进度，0->1表示开始关闭到完全关闭
            float smoothProgress = smoothEasing(progress);

            // 从当前X位置滑动到屏幕外
            float startX = x;
            float endX = -rectWidth; // 向左滑出屏幕
            animX = startX + (endX - startX) * smoothProgress;
        } else {
            // 打开动画：从屏幕外滑入到目标位置
            float smoothProgress = smoothEasing(animProgress);

            // 从屏幕外滑入到目标位置
            float startX = -rectWidth; // 从屏幕左侧外部开始
            float endX = x;
            animX = startX + (endX - startX) * smoothProgress;
        }

        // 背景
        RenderUtils.drawRectangle(guiGraphics.pose(), animX, y, rectWidth, rectHeight, ColorUtils.color(40, 40, 40, 255));

        // 侧边栏背景
        RenderUtils.drawRectangle(guiGraphics.pose(), animX, y, 90, rectHeight, ColorUtils.color(80, 80, 80, 255));

        // Logo
        TrueTypeFont boldFont = Loratadine.INSTANCE.getFontManager().tenacityBold20;
        TrueTypeFont regularFont = Loratadine.INSTANCE.getFontManager().tenacity16;

        boldFont.drawString(guiGraphics.pose(), "Loratadine", animX + 10, y + 10, -1);
        regularFont.drawString(guiGraphics.pose(), "Compact GUI", animX + 12, y + 22, Color.LIGHT_GRAY.getRGB());

        // 分类分隔线
        RenderUtils.drawRectangle(guiGraphics.pose(), animX + 5, y + 35, 80, 1, new Color(110, 110, 110).getRGB());

        // 绘制Edit HUD按钮
        renderEditHudButton(guiGraphics, mouseX, mouseY);

        // 渲染分类列表 - 考虑个人信息区域高度
        float catHeight = (rectHeight - 45 - 65) / Category.values().length; // 减去顶部和个人信息区域高度
        float separation = 0;

        // 设置剪裁区域，防止动画超出侧边栏
        guiGraphics.enableScissor((int) animX, (int) y + 40, (int) (animX + 90), (int) (y + rectHeight - 65));

        for (Category category : Category.values()) {
            float catY = y + 40 + separation;
            boolean hovering = isHovering(animX, catY, 90, catHeight, mouseX, mouseY);

            Color categoryColor = hovering ? new Color(200, 200, 200) : new Color(150, 150, 150);
            Color selectColor = (activeCategory == category) ? Color.WHITE : categoryColor;

            // 为选中的分类绘制背景 - 带有动画效果
            if (activeCategory == category) {
                // 动画背景效果：从左侧滑入
                if (isTransitioning && categoryTransitionProgress < 1.0f) {
                    // 使用缓动函数使动画更丝滑
                    float easedProgress = easeOutQuad(categoryTransitionProgress);
                    float slideWidth = 90 * easedProgress;
                    RenderUtils.drawRectangle(guiGraphics.pose(), animX, catY, slideWidth, catHeight, new Color(40, 40, 40).getRGB());

                    // 添加右侧渐变边缘效果
                    float gradientWidth = Math.min(10, 90 - slideWidth);
                    if (gradientWidth > 0) {
                        for (int i = 0; i < gradientWidth; i++) {
                            float alpha = 1.0f - (i / gradientWidth);
                            Color gradientColor = new Color(40, 40, 40, (int) (255 * alpha));
                            RenderUtils.drawRectangle(guiGraphics.pose(),
                                    animX + slideWidth + i,
                                    catY,
                                    1,
                                    catHeight,
                                    gradientColor.getRGB());
                        }
                    }
                } else {
                    // 完全选中状态
                    RenderUtils.drawRectangle(guiGraphics.pose(), animX, catY, 90, catHeight, new Color(40, 40, 40).getRGB());
                }
            }
            // 为前一个分类绘制渐出动画
            else if (isTransitioning && previousCategory == category) {
                // 动画背景效果：向右侧滑出
                float easedProgress = easeOutQuad(categoryTransitionProgress);
                float remainingWidth = 90 * (1.0f - easedProgress);
                if (remainingWidth > 0) {
                    RenderUtils.drawRectangle(guiGraphics.pose(), animX, catY, remainingWidth, catHeight, new Color(40, 40, 40).getRGB());

                    // 添加右侧渐变边缘效果
                    float gradientWidth = Math.min(10, remainingWidth);
                    for (int i = 0; i < gradientWidth; i++) {
                        float alpha = i / gradientWidth;
                        Color gradientColor = new Color(40, 40, 40, (int) (255 * alpha));
                        RenderUtils.drawRectangle(guiGraphics.pose(),
                                animX + remainingWidth - i - 1,
                                catY,
                                1,
                                catHeight,
                                gradientColor.getRGB());
                    }
                }
            }

            boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                    .languageValue.getValue().equals("English");
            String categoryName = isEnglish ? category.name : category.cnName;

            boldFont.drawString(guiGraphics.pose(), categoryName, animX + 8, catY + catHeight / 2 - boldFont.getHeight() / 2, selectColor.getRGB());

            separation += catHeight;
        }

        // 关闭剪裁
        guiGraphics.disableScissor();

        // 渲染左下角的个人信息区域
        renderProfileSection(guiGraphics, animX, y, 90, rectHeight);

        // 更新模块面板
        modulePanel.currentCategory = activeCategory;
        modulePanel.moduleRects = getModuleRects(activeCategory);
        modulePanel.x = animX;
        modulePanel.y = y;
        modulePanel.rectHeight = rectHeight;
        modulePanel.rectWidth = rectWidth;
        modulePanel.render(guiGraphics, mouseX, mouseY, partialTicks);

        typing = modulePanel.typing;

        // 处理GUI主体拖动
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
    }

    /**
     * 为CompactClickGUI的Edit HUD按钮渲染方法
     */
    private void renderEditHudButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TrueTypeFont regularFont = Loratadine.INSTANCE.getFontManager().tenacity16;

        // 检测按钮悬停
        boolean hoveringEditHud = isHovering(editHudButtonActualX, editHudButtonY,
                editHudButtonWidth, editHudButtonHeight, mouseX, mouseY);

        // 更新悬停状态
        if (hoveringEditHud != isHoveringEditHudButton) {
            isHoveringEditHudButton = hoveringEditHud;
        }

        // 根据悬停动画进度计算按钮颜色 - 使用灰色系，无透明度
        Color baseColor = new Color(60, 60, 60, 255);
        Color hoverColor = new Color(80, 80, 80, 255);

        int r = (int)lerp(baseColor.getRed(), hoverColor.getRed(), editHudButtonHoverProgress);
        int g = (int)lerp(baseColor.getGreen(), hoverColor.getGreen(), editHudButtonHoverProgress);
        int b = (int)lerp(baseColor.getBlue(), hoverColor.getBlue(), editHudButtonHoverProgress);

        Color editHudBgColor = new Color(r, g, b, 255);

        // 绘制直角按钮
        RenderUtils.drawRectangle(guiGraphics.pose(), editHudButtonActualX, editHudButtonY,
                editHudButtonWidth, editHudButtonHeight, editHudBgColor.getRGB());

        // 添加底部强调线 - 与GUI整体风格一致的灰色
        Color accentColor = new Color(100, 100, 100, 255);
        RenderUtils.drawRectangle(guiGraphics.pose(),
                editHudButtonActualX,
                editHudButtonY + editHudButtonHeight - 2,
                editHudButtonWidth,
                2,
                accentColor.getRGB());

        regularFont.drawCenteredString(guiGraphics.pose(), "Edit HUD",
                editHudButtonActualX + editHudButtonWidth / 2,
                editHudButtonY + editHudButtonHeight / 2 - regularFont.getHeight() / 2,
                new Color(220, 220, 220).getRGB());
    }

    /**
     * 渲染HUD编辑模式界面
     */
    private void renderHudEditMode(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TrueTypeFont boldFont = Loratadine.INSTANCE.getFontManager().tenacityBold20;
        TrueTypeFont regularFont = Loratadine.INSTANCE.getFontManager().tenacity16;

        // 计算按钮透明度基于位置计算渐隐效果
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

        // 检测按钮悬停状态
        boolean hoveringSave = isHovering(saveButtonX, saveButtonActualY, saveButtonWidth, saveButtonHeight, mouseX, mouseY);
        boolean hoveringCancel = isHovering(cancelButtonX, cancelButtonActualY, cancelButtonWidth, cancelButtonHeight, mouseX, mouseY);

        // 更新悬停状态
        if (hoveringSave != isHoveringSaveButton) {
            isHoveringSaveButton = hoveringSave;
        }
        if (hoveringCancel != isHoveringCancelButton) {
            isHoveringCancelButton = hoveringCancel;
        }

        // Color interpolation for Save button - grey theme without transparency
        Color saveBaseColor = new Color(60, 60, 60, 255);
        Color saveHoverColor = new Color(80, 80, 80, 255);

        int saveR = (int)lerp(saveBaseColor.getRed(), saveHoverColor.getRed(), saveButtonHoverProgress);
        int saveG = (int)lerp(saveBaseColor.getGreen(), saveHoverColor.getGreen(), saveButtonHoverProgress);
        int saveB = (int)lerp(saveBaseColor.getBlue(), saveHoverColor.getBlue(), saveButtonHoverProgress);

        Color saveBgColor = new Color(saveR, saveG, saveB, (int)(255 * saveButtonOpacity));
        // Add a subtle grey accent color
        Color saveAccentColor = new Color(100, 100, 100, (int)(255 * saveButtonOpacity));

        // Color interpolation for Cancel button - grey theme without transparency
        Color cancelBaseColor = new Color(60, 60, 60, 255);
        Color cancelHoverColor = new Color(80, 80, 80, 255);

        int cancelR = (int)lerp(cancelBaseColor.getRed(), cancelHoverColor.getRed(), cancelButtonHoverProgress);
        int cancelG = (int)lerp(cancelBaseColor.getGreen(), cancelHoverColor.getGreen(), cancelButtonHoverProgress);
        int cancelB = (int)lerp(cancelBaseColor.getBlue(), cancelHoverColor.getBlue(), cancelButtonHoverProgress);

        Color cancelBgColor = new Color(cancelR, cancelG, cancelB, (int)(255 * cancelButtonOpacity));
        // Add a subtle grey accent color
        Color cancelAccentColor = new Color(100, 100, 100, (int)(255 * cancelButtonOpacity));

        // Render Save button with sharp corners
        RenderUtils.drawRectangle(guiGraphics.pose(), saveButtonX, saveButtonActualY,
                saveButtonWidth, saveButtonHeight, saveBgColor.getRGB());

        // Add subtle green accent at the bottom
        RenderUtils.drawRectangle(guiGraphics.pose(), saveButtonX,
                saveButtonActualY + saveButtonHeight - 2,
                saveButtonWidth, 2, saveAccentColor.getRGB());

        boldFont.drawCenteredString(guiGraphics.pose(), "Save",
                saveButtonX + saveButtonWidth / 2,
                saveButtonActualY + saveButtonHeight / 2 - boldFont.getHeight() / 2,
                new Color(220, 220, 220, (int)(255 * saveButtonOpacity)).getRGB());

        // Render Cancel button with sharp corners
        RenderUtils.drawRectangle(guiGraphics.pose(), cancelButtonX, cancelButtonActualY,
                cancelButtonWidth, cancelButtonHeight, cancelBgColor.getRGB());

        // Add subtle red accent at the bottom
        RenderUtils.drawRectangle(guiGraphics.pose(), cancelButtonX,
                cancelButtonActualY + cancelButtonHeight - 2,
                cancelButtonWidth, 2, cancelAccentColor.getRGB());

        boldFont.drawCenteredString(guiGraphics.pose(), "Cancel",
                cancelButtonX + cancelButtonWidth / 2,
                cancelButtonActualY + cancelButtonHeight / 2 - boldFont.getHeight() / 2,
                new Color(220, 220, 220, (int)(255 * cancelButtonOpacity)).getRGB());

        // 指令文本的透明度也应该基于按钮的平均透明度
        float textOpacity = (saveButtonOpacity + cancelButtonOpacity) / 2;

        // Instruction text - more subtle appearance
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        float textX = screenWidth / 2 - regularFont.getStringWidth("HUD Edit Mode - Drag HUD elements to reposition") / 2;
        float textY = saveButtonActualY + saveButtonHeight + 10;

        // Add subtle background for instruction text with sharp corners
        float textWidth = regularFont.getStringWidth("HUD Edit Mode - Drag HUD elements to reposition");
        float textPadding = 6;
        RenderUtils.drawRectangle(guiGraphics.pose(),
                textX - textPadding,
                textY - textPadding / 2,
                textWidth + textPadding * 2,
                regularFont.getHeight() + textPadding,
                new Color(40, 40, 40, (int)(255 * textOpacity)).getRGB());

        regularFont.drawString(guiGraphics.pose(), "HUD Edit Mode - Drag HUD elements to reposition",
                textX, textY, new Color(200, 200, 200, (int)(255 * textOpacity)).getRGB());
    }

    /**
     * Update HUD edit mode animations with smooth easing and sequential button animations
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

                // Add slight delay effect for sequential animation
                float saveProgress = progress;
                float cancelProgress = Math.max(0, (progress - 0.1f) / 0.9f); // 0.1s delay for cancel button

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

                // Add slight delay effect with reverse order
                float cancelProgress = progress;
                float saveProgress = Math.max(0, (progress - 0.1f) / 0.9f); // 0.1s delay for save button

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
                    waitingForButtonExit = false;
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
     * Update the Edit HUD button animation with smooth easing
     */
    private void updateEditHudButtonAnimation() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Update button hover animation
        if (isHoveringEditHudButton) {
            editHudButtonHoverProgress = Math.min(1.0f, editHudButtonHoverProgress + HOVER_ANIM_SPEED);
        } else {
            editHudButtonHoverProgress = Math.max(0.0f, editHudButtonHoverProgress - HOVER_ANIM_SPEED);
        }

        // Calculate target and start positions
        float targetX = screenWidth - editHudButtonWidth - 10; // Right bottom corner
        float targetY = screenHeight - editHudButtonHeight - 10;
        float startX = screenWidth + 10; // Off-screen to the right

        // Handle waiting for button exit
        if (waitingForButtonExit) {
            if (!isHudEditAnimActive) {
                waitingForButtonExit = false;
                if (isClosing) {
                    isClosing = false;
                    animProgress = 0f;
                    animStartTime = System.currentTimeMillis();
                }
            } else {
                editHudButtonActualX = startX;
                return;
            }
        }

        if (hudEditMode) {
            // Keep button off-screen in HUD edit mode
            editHudButtonActualX = startX;
        } else if (isClosing) {
            // Animate from current position to off-screen
            float progress = 1.0f - animProgress;
            editHudButtonActualX = lerp(targetX, startX, smoothEasing(progress));
        } else {
            // Animate from off-screen to target position
            editHudButtonActualX = lerp(startX, targetX, smoothEasing(animProgress));
        }

        // Update the button position
        editHudButtonX = targetX;
        editHudButtonY = targetY;
    }

    /**
     * Update HUD edit button position
     */
    private void updateHudEditButtonPositions() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Edit HUD按钮位于屏幕右下角
        editHudButtonX = screenWidth - editHudButtonWidth - 10;
        editHudButtonY = screenHeight - editHudButtonHeight - 10;

        // 保存和取消按钮位于屏幕顶部中央
        saveButtonX = screenWidth / 2 - saveButtonWidth - 5;
        saveButtonY = 10;
        cancelButtonX = screenWidth / 2 + 5;
        cancelButtonY = 10;
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

    private String getCurrentTimeString() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    private void updateCategoryTransition() {
        // 检查是否正在进行过渡动画
        if (isTransitioning) {
            // 使用系统时间计算进度，而不是依赖partialTicks
            long elapsedTime = System.currentTimeMillis() - categoryTransitionStartTime;
            float duration = CATEGORY_TRANSITION_DURATION * 1000; // 转为毫秒

            categoryTransitionProgress = Math.min(1.0f, elapsedTime / duration);

            // 检查动画是否完成
            if (categoryTransitionProgress >= 1.0f) {
                categoryTransitionProgress = 1.0f;
                isTransitioning = false;
                previousCategory = null;
            }
        }
    }

    private void startCategoryTransition(Category newCategory) {
        if (activeCategory == newCategory) return;

        previousCategory = activeCategory;
        activeCategory = newCategory;

        isTransitioning = true;
        categoryTransitionProgress = 0.0f;
        categoryTransitionStartTime = System.currentTimeMillis(); // 记录开始时间
    }

    private void updateAnimation() {
        // 使用系统时间计算进度，而不是依赖partialTicks
        long elapsedTime = System.currentTimeMillis() - animStartTime;
        float duration = ANIM_DURATION * 1000; // 转为毫秒

        if (isClosing) {
            animProgress = Math.max(0f, 1f - (elapsedTime / duration));
            // 更新背景暗化效果 - 关闭时变浅
            backgroundDimAlpha = animProgress * MAX_BACKGROUND_DIM;
        } else {
            animProgress = Math.min(1f, elapsedTime / duration);
            // 更新背景暗化效果 - 打开时变深
            backgroundDimAlpha = animProgress * MAX_BACKGROUND_DIM;
        }
    }

    private boolean isAnimationFinished() {
        return isClosing ? animProgress <= 0f : animProgress >= 1f;
    }

    /**
     * 二次方缓出函数 - 使动画开始快结束慢
     */
    private float easeOutQuad(float x) {
        return 1 - (1 - x) * (1 - x);
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
            float bounce = (float) Math.sin(overshoot * Math.PI) * ELASTIC_FACTOR;
            easedProgress = Math.min(1.0f, easedProgress + bounce);
        }

        return easedProgress;
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 如果GUI主界面动画没完成，不处理点击
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

        // 非编辑模式下不处理DraggableHUD的点击
        // 因为DragManager已经有检查了，这里不需要额外检查

        // 处理拖动 - 点击顶部区域
        if (isHovering(x, y, rectWidth, 20, mouseX, mouseY) && button == 0) {
            dragging = true;
            dragX = (float) (mouseX - x);
            dragY = (float) (mouseY - y);
            return true;
        }

        // 处理分类选择 - 移除了isTransitioning检查，允许在动画过程中点击
        float catHeight = (rectHeight - 45 - 65) / Category.values().length; // 减去顶部和个人信息区域高度
        float separation = 0;

        for (Category category : Category.values()) {
            float catY = y + 40 + separation;
            if (isHovering(x, catY, 90, catHeight, mouseX, mouseY) && button == 0) {
                if (activeCategory != category) {
                    // 即使当前有动画在进行，也可以启动新的分类切换动画
                    startCategoryTransition(category);
                    return true;
                }
                return true;
            }
            separation += catHeight;
        }

        // 处理模块面板点击 - 允许在分类切换动画期间点击
        modulePanel.mouseClicked(mouseX, mouseY, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderBackgroundFade(GuiGraphics guiGraphics, float alpha) {
        int alphaValue = (int) (Mth.clamp(alpha, 0f, 0.4f) * 255);
        guiGraphics.fill(0, 0, this.width, this.height, (alphaValue << 24));
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

        if (button == 0) {
            // ClickGUI自身的拖动释放
            if (dragging) {
                dragging = false;
            }
        }

        modulePanel.mouseReleased(mouseX, mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;

        // 在HUD编辑模式下禁用滚动
        if (hudEditMode) {
            return true;
        }

        modulePanel.mouseScrolled(mouseX, mouseY, delta);

        return super.mouseScrolled(mouseX, mouseY, delta);
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
            isClosing = true;
            animStartTime = System.currentTimeMillis(); // 重设动画开始时间
            return true;
        }

        // 在HUD编辑模式下禁用键盘输入
        if (hudEditMode) {
            return true;
        }

        modulePanel.keyPressed(keyCode, scanCode, modifiers);

        return super.keyPressed(keyCode, scanCode, modifiers);
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
            animStartTime = System.currentTimeMillis(); // 重设动画开始时间
        }
    }

    @Override
    protected void init() {
        if (mc == null || mc.player == null || mc.level == null) return;

        animProgress = 0f;
        isClosing = false;
        backgroundDimAlpha = 0f; // 初始化背景暗化透明度
        animStartTime = System.currentTimeMillis(); // 记录动画开始时间

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

        // 如果第一次加载，设置默认选中的分类
        if (activeCategory == null && Category.values().length > 0) {
            activeCategory = Category.values()[0];
        }

        modulePanel.initGui();

        super.init();
    }

    private List<ModuleRect> getModuleRects(Category category) {
        if (category == null) return new ArrayList<>();

        // 如果缓存中没有或者需要重新加载
        if (!moduleRects.containsKey(category)) {
            List<ModuleRect> rects = new ArrayList<>();
            for (Module module : Loratadine.INSTANCE.getModuleManager().getModule(category)) {
                rects.add(new ModuleRect(module));
            }
            moduleRects.put(category, rects);

            // 初始化所有模块矩形
            rects.forEach(ModuleRect::initGui);
        }

        return moduleRects.get(category);
    }

    private boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}