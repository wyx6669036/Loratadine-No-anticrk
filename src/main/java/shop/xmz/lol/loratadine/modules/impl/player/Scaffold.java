package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.setting.MoveFix;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.impl.DecelerateAnimation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.item.SpoofItemUtil;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.player.FallingPlayer;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;
import shop.xmz.lol.loratadine.utils.player.PlayerUtil;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;
import java.util.List;
import java.util.*;

import static shop.xmz.lol.loratadine.utils.player.MoveUtils.isMoving;


public class Scaffold extends Module {
    public static Scaffold INSTANCE;
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Snap", "Telly", "Watchdog Telly"}, "Snap");
    private final BooleanSetting keepRotation = new BooleanSetting("Keep Rotation", this, true);
    private final BooleanSetting spoof = new BooleanSetting("Spoof Item", this, true);
    private final BooleanSetting silent = new BooleanSetting("Silent Swing", this, true);
    private final BooleanSetting keepY = new BooleanSetting("KeepY", this, false);
    private final BooleanSetting telly = new BooleanSetting("Telly", this, true);
    private final BooleanSetting auto3rdPerson = new BooleanSetting("Auto 3rd Person", this, false);
    private final ModeSetting sneak_Value = new ModeSetting("Sneak Mode", this, new String[]{"None", "Legit", "Fast"}, "None");
    private final ModeSetting jump_Value = new ModeSetting("Jump Mode", this, new String[]{"None", "Legit", "Fast"}, "None");
    private final ModeSetting count_Value = new ModeSetting("Count", this, new String[]{"None", "Modern", "Basic", "Simple"}, "None");
    private final Animation animation = new DecelerateAnimation(250, 1);
    private int ticksOnAir, sneakingTicks;
    private BlockPosWithFacing data;
    private boolean lastJumpPressed;
    public int bigVelocityTick = 0;
    private int offGroundTicks = 0;
    private boolean onKeepY = false;
    boolean canTellyPlace;
    private int prevItem = 0;
    private int baseY = -1;
    private int slot;

    public Scaffold() {
        super("Scaffold", "自动搭路", Category.PLAYER, GLFW.GLFW_KEY_G);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        prevItem = mc.player.getInventory().selected;
        if (spoof.getValue()) {
            SpoofItemUtil.startSpoof(prevItem);
        }

        if (auto3rdPerson.getValue()) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }

        prevItem = mc.player.getInventory().selected; // 记录当前选择的物品槽位
        canTellyPlace = false;
        this.data = null;
        this.slot = -1;
        bigVelocityTick = 0;
        lastJumpPressed = false;
        baseY = 10000;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        if (auto3rdPerson.getValue()) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }

        KeyMapping.set(mc.options.keyShift.getKey(), false);
        if (spoof.getValue() && SpoofItemUtil.spoofing) {
            SpoofItemUtil.stopSpoof();
        } else {
            mc.player.getInventory().selected = prevItem;
        }
        lastJumpPressed = false;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (this.slot < 0) return;

        if ((!telly.getValue() && !lastJumpPressed)) {
            canTellyPlace = true;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket velocity) {
            if (velocity.getId() == mc.player.getId()) {
                double strength = new Vec3(velocity.getXa() / 8000.0D, 0, velocity.getZa() / 8000.0D).length();
                if (strength >= 1.5D) {
                    bigVelocityTick = 60;
                }
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent moveInputEvent) {
        if (mc.player == null || mc.level == null) return;

        if (jump_Value.is("Legit") && mc.player.onGround()) {
            moveInputEvent.keyJump = true;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (jump_Value.is("Fast") && mc.player.onGround()) {
            mc.player.jumpFromGround();
            return;
        }

        if (keepY.getValue() && jump_Value.is("None")) {
            if (onKeepY && mc.player.onGround() && isMoving() && !mc.options.keyJump.isDown()) {
                mc.player.jumpFromGround();
            }
        }
    }

    @EventTarget
    public void onSet(TickEvent e) {
        if (telly.getValue()) {
            onKeepY = !mc.options.keyJump.isDown();
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.post) return;
        calculateEagle();
        calculateSneaking();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.onGround()) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        // 用于检测何时放置方块，如果是在空中，则允许放置方块
        if (PlayerUtil.blockRelativeToPlayer(0, -1, 0) instanceof AirBlock) {
            ticksOnAir++;
        } else {
            ticksOnAir = 0;
        }

        lastJumpPressed = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyJump.getKey().getValue()) || !jump_Value.is("None");

        if (bigVelocityTick > 0) {
            bigVelocityTick--;
        }
        if (mc.player.onGround() && bigVelocityTick <= 30) {
            bigVelocityTick = 0;
        }

        if ((mode_Value.getValue().equals("Snap") || lastJumpPressed)) {
            place();
        }

        if (baseY == -1 || baseY > (int) Math.floor(mc.player.getY()) - 1 || bigVelocityTick > 0 || mc.player.onGround() || lastJumpPressed) {
                baseY = (int) Math.floor(mc.player.getY()) - 1;
        }

        getBestBlocks();
        this.slot = getBlockSlot();

        if (this.slot < 0) {
            return;
        }

        this.findBlock();
        mc.player.getInventory().selected = this.slot;

        canTellyPlace = bigVelocityTick > 0 || (mode_Value.getValue().equals("Snap") && !lastJumpPressed) || offGroundTicks >= 1;

        if (!canTellyPlace) {
            return;
        }

        if (mode_Value.is("Watchdog Telly") && data != null && canTellyPlace) {
            place();
        }
    }

    @EventTarget
    public void onRenderPlayer(RenderPlayerEvent event) {
        if (canTellyPlace && data != null && MoveFix.INSTANCE.renderRotation.getValue()) {
            float[] rotationF = RotationUtils.getRotationBlock(data.position());
            Rotation rotation = new Rotation(rotationF[0], rotationF[1]);
            event.rotationYaw = rotation.yaw;
            event.rotationPitch = rotation.pitch;
        }
    }

    public void calculateRotations() {
        if (mc.player == null || mc.level == null) return;

        if (data != null) {
            float[] rotationF = RotationUtils.getRotationBlock(data.position());
            Rotation rotation = new Rotation(rotationF[0], rotationF[1]);

            if (mode_Value.is("Telly") && !canTellyPlace) {
                return; // 如果是Telly模式且不能放置，不执行后续代码
            }
            RotationUtils.setRotation(rotation, keepRotation.getValue() ? 20 : 0);
        }
    }

    public void calculateEagle() {
        if (mc.player == null || mc.level == null || !sneak_Value.getValue().equals("Legit")) return;

        if (PlayerUtil.getBlockUnderPlayer(mc.player) instanceof AirBlock) {
            if (mc.player.onGround()) {
                KeyMapping.set(mc.options.keyShift.getKey(), true);
            }
        } else if (mc.player.onGround()) {
            KeyMapping.set(mc.options.keyShift.getKey(), false);
        }
    }

    public void calculateSneaking() {
        if (mc.player == null || mc.level == null || !sneak_Value.getValue().equals("Fast")) return;

        mc.options.keyShift.setDown(false);

        // 如果玩家没有移动，则退出
        if (!MoveUtils.isMoving()) {
            return;
        }

        this.sneakingTicks--;

        // 随机生成 Sneak 开始时间、放置时间和结束时间
        int ahead = 0;
        int place = 0;
        int after = 0;

        // 如果玩家悬空，计算 Sneak 的持续时间
        if (this.ticksOnAir > 0) {
            this.sneakingTicks = (int) (Math.ceil((after + (place - this.ticksOnAir)) / 0.2D));
        }

        // 判断是否需要进入 Sneak 状态
        if (this.sneakingTicks >= 0) {
            mc.options.keyShift.setDown(true); // 开启 Sneak

            return;
        }

        // 如果提前时间和放置时间为 0，且玩家悬空，则保持 Sneak 一次
        if (this.ticksOnAir > 0) {
            this.sneakingTicks = 1;
            return;
        }

        // 检测玩家前方的方块是否为空气
        Vec3 motion = mc.player.getDeltaMovement();
        BlockPos targetPos = mc.player.blockPosition().offset(BlockPos.containing(motion.x * ahead * 0.2D, MoveUtils.HEAD_HITTER_MOTION, motion.z * ahead * 0.2D));

        if (mc.level.getBlockState(targetPos).getBlock() instanceof AirBlock) {
            this.sneakingTicks = (int) Math.floor((5 + place + after) / 0.2D);
        }
    }

    @EventTarget
    public void onPlaceable(PlaceEvent event) {
        if (mode_Value.getValue().equals("Telly") && !lastJumpPressed) {
            if (data != null && canTellyPlace) place();
        }
    }

    public void place() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (!canTellyPlace) {
            return;
        }

        this.slot = getBlockSlot();
        if (this.slot < 0) {
            return;
        }

        ItemStack currentItem = mc.player.getInventory().getItem(mc.player.getInventory().selected);
        if (!(currentItem.getItem() instanceof BlockItem)) {
            int blockSlot = getBlockSlot();
            if (blockSlot != -1) {
                mc.player.getInventory().selected = blockSlot;
            } else {
                return;
            }
        }

        if (data != null) {
            Block block = mc.level.getBlockState(data.position()).getBlock();

            if (mc.player.getDeltaMovement().y < -0.1) {
                FallingPlayer fallingPlayer = new FallingPlayer(mc.player);
                fallingPlayer.calculate(2);
            }

            calculateRotations();

            if (!(block instanceof CraftingTableBlock
                    || block instanceof ChestBlock
                    || block instanceof AnvilBlock
                    || block instanceof FurnaceBlock
                    || block instanceof HopperBlock
                    || block instanceof DispenserBlock
                    || block instanceof EnchantmentTableBlock
                    || block instanceof BrewingStandBlock
                    || block instanceof NoteBlock))
            {
                InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(getVec3(data.position(), data.facing()), data.facing(), data.position(), false));
                if (result == InteractionResult.SUCCESS) {
                    if (silent.getValue()) {
                        mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
            if (mode_Value.is("Snap") || (telly.getValue() && lastJumpPressed)) data = null;
        }
    }

    public int getBlockCount() {
        if (mc.player == null || mc.level == null) return 0;

        int blockCount = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.player.inventoryMenu.getSlot(i).getItem().isEmpty()) continue;
            ItemStack stack = mc.player.inventoryMenu.getSlot(i).getItem();
            if (!(stack.getItem() instanceof BlockItem) || !isValid(stack.getItem())) continue;
            blockCount += stack.getCount();
        }
        return blockCount;
    }

    public void findBlock() {
        if (mc.player == null || mc.level == null) return;

        BlockPos blockPos2 = BlockPos.containing(mc.player.getX(), baseY, mc.player.getZ());

        data = getPlaceBlockCoord(blockPos2);

    }

    public int getBlockSlot() {
        if (mc.player == null || mc.level == null) return 0;

        for (int i = 0; i < 9; ++i) {
            if (!mc.player.containerMenu.getSlot(i + 36).hasItem()) continue;
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();
            if (!(stack.getItem() instanceof BlockItem) || !isValid(stack.getItem())) continue;
            return i;
        }
        return -1;
    }


    public void getBestBlocks() {
        if (mc.player == null || mc.level == null) return;

        if (getBlockCount() == 0) return;

        if (hotbarContainBlock()) {
            for (int a = 36; a < 45; a++) {
                if (mc.player.containerMenu.getSlot(a).hasItem()) {
                    Item item = mc.player.containerMenu.getSlot(a).getItem().getItem();
                    if (item instanceof BlockItem && isValid(item)) {
                        break;
                    }
                }
            }
        } else {
            for (int a = 36; a < 45; a++) {
                if (!mc.player.containerMenu.getSlot(a).hasItem()) {
                    break;
                }
            }
        }
    }

    public static Vec3 getVec3(BlockPos pos, Direction face) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        if (face == Direction.UP || face == Direction.DOWN) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        } else {
            y += 0.08;
        }

        if (face == Direction.WEST || face == Direction.EAST) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }

    private boolean hotbarContainBlock() {
        if (mc.player == null || mc.level == null) return false;

        int i = 36;

        while (i < 45) {
            try {
                ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();
                if ((stack.isEmpty()) || (stack.getItem() instanceof AirItem) || !(stack.getItem() instanceof BlockItem) || !isValid(stack.getItem())) {
                    i++;
                    continue;
                }
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean isValid(final Item item) {
        if (!(item instanceof BlockItem)) return false;

        Block block = ((BlockItem) item).getBlock();

        // 定义需要排除的方块集合
        Set<Block> excludedBlocks = new HashSet<>(Arrays.asList(
                Blocks.SUGAR_CANE, Blocks.SAND, Blocks.GRAVEL,
                Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.CHEST,
                Blocks.BREWING_STAND, Blocks.ANVIL, Blocks.ENCHANTING_TABLE,
                Blocks.BEACON, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
                Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WIRE,
                Blocks.REDSTONE_LAMP, Blocks.BLAST_FURNACE, Blocks.GRINDSTONE,
                Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL,
                Blocks.HORN_CORAL, Blocks.TUBE_CORAL, Blocks.MAGMA_BLOCK,
                Blocks.TORCH, Blocks.SOUL_TORCH, Blocks.LANTERN, Blocks.CHAIN,
                Blocks.END_ROD, Blocks.GLOW_LICHEN, Blocks.STONECUTTER, Blocks.FLETCHING_TABLE,
                Blocks.CARTOGRAPHY_TABLE, Blocks.LOOM, Blocks.CHIPPED_ANVIL,
                Blocks.DAMAGED_ANVIL, Blocks.NOTE_BLOCK, Blocks.CAULDRON, Blocks.BELL,
                Blocks.CONDUIT, Blocks.LADDER,
                Blocks.SCAFFOLDING, Blocks.LIGHTNING_ROD, Blocks.BARREL,
                Blocks.SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
                Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX,
                Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
                Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
                Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
                Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.CANDLE, Blocks.WHITE_CANDLE,
                Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.LIGHT_BLUE_CANDLE, Blocks.YELLOW_CANDLE,
                Blocks.LIME_CANDLE, Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE,
                Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE, Blocks.BLUE_CANDLE, Blocks.BROWN_CANDLE,
                Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE, Blocks.WHITE_BANNER,
                Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER,
                Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER,
                Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER,
                Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.SKELETON_SKULL,
                Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_HEAD, Blocks.PLAYER_HEAD, Blocks.CREEPER_HEAD,
                Blocks.DRAGON_HEAD, Blocks.DRAGON_EGG
        ));

        // 排除植物类方块
        if (block instanceof CropBlock ||
                block instanceof SaplingBlock ||
                block instanceof FlowerBlock ||
                block instanceof TallFlowerBlock ||
                block instanceof NetherWartBlock ||
                block instanceof BambooSaplingBlock ||
                block instanceof BambooStalkBlock ||
                block instanceof BedBlock || // 排除所有颜色的床
                block instanceof CandleBlock || // 排除所有颜色的蜡烛
                block instanceof BannerBlock || // 排除所有颜色的旗帜
                block instanceof AbstractSkullBlock || // 排除所有种类的头颅
                excludedBlocks.contains(block)) {
            return false;
        }

        // 检查是否在无效方块列表中
        return !invalidBlocks.contains(block);
    }

    private static final List<Block> invalidBlocks = Arrays.asList(
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE,
            Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR,
            Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.FLOWER_POT, Blocks.COBWEB, Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.TNT
    );

    private BlockPosWithFacing getPlaceBlockCoord(BlockPos blockPos) {
        int[][] offsets = {
                {0, 0, 0},     // 原始位置

                {-1, 0, 0},    // 左1格
                {1, 0, 0},     // 右1格
                {0, 0, 1},     // 前1格
                {0, 0, -1},    // 后1格

                {-2, 0, 0},    // 左2格
                {2, 0, 0},     // 右2格
                {0, 0, 2},     // 前2格
                {0, 0, -2},    // 后2格

                {1, 0, 1},
                {-1, 0, 1},
                {1, 0, -1},
                {-1, 0, -1},

                {2, 0, 2},
                {-2, 0, 2},
                {2, 0, -2},
                {-2, 0, -2},

                {-3, 0, 0},    // 左3格
                {3, 0, 0},     // 右3格
                {0, 0, 3},     // 前3格
                {0, 0, -3},    // 后3格

                {1, 0, 2},
                {-1, 0, 2},
                {1, 0, -2},
                {-1, 0, -2},

                {2, 0, 1},
                {-2, 0, 1},
                {2, 0, -1},
                {-2, 0, -1},

                {0, -1, 0},    // 正下方1层
                {1, -1, 0},    // 右1格 + 下方1层
                {-1, -1, 0},   // 左1格 + 下方1层
                {0, -1, 1},    // 前1格 + 下方1层
                {0, -1, -1},   // 后1格 + 下方1层

                {1, 0, 3},
                {-1, 0, 3},
                {1, 0, -3},
                {-1, 0, -3},

                {3, 0, 1},
                {-3, 0, 1},
                {3, 0, -1},
                {-3, 0, -1},

                {-4, 0, 0},    // 左4格
                {4, 0, 0},     // 右4格
                {0, 0, 4},     // 前4格
                {0, 0, -4},    // 后4格
        };

        // 循环遍历所有偏移量
        for (int[] offset : offsets) {
            BlockPos currentPos = blockPos.offset(offset[0], offset[1], offset[2]);
            BlockPosWithFacing result = canPlaceCheck(currentPos);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static BlockPosWithFacing canPlaceCheck(BlockPos blockPos) {
        if (mc.level != null) {
            if (mc.level.getBlockState(blockPos.below()).isSolidRender(mc.level, blockPos.below()))
                return new BlockPosWithFacing(blockPos.below(), Direction.UP);

            else if (mc.level.getBlockState(blockPos.west()).isSolidRender(mc.level, blockPos.west()))
                return new BlockPosWithFacing(blockPos.west(), Direction.EAST);

            else if (mc.level.getBlockState(blockPos.east()).isSolidRender(mc.level, blockPos.east()))
                return new BlockPosWithFacing(blockPos.east(), Direction.WEST);

            else if (mc.level.getBlockState(blockPos.south()).isSolidRender(mc.level, blockPos.south()))
                return new BlockPosWithFacing(blockPos.south(), Direction.NORTH);

            else if (mc.level.getBlockState(blockPos.north()).isSolidRender(mc.level, blockPos.north()))
                return new BlockPosWithFacing(blockPos.north(), Direction.SOUTH);
        }

        return null;
    }

    public void renderCounter(PoseStack poseStack) {
        animation.setDirection(this.isEnabled() ? shop.xmz.lol.loratadine.utils.animations.Direction.FORWARDS : shop.xmz.lol.loratadine.utils.animations.Direction.BACKWARDS);
        if ((!isEnabled() && animation.isDone()) || mc.player == null) return;
        int slot = getBlockSlot();
        ItemStack heldItem = slot == -1 ? null : mc.player.getMainHandItem();
        int count = slot == -1 ? 0 : getBlockCount();
        String countStr = String.valueOf(count);
        String str = countStr + " block" + (count != 1 ? "s" : "");
        float x, y;
        float output = (float) animation.getOutput();

        // 绘制方块数量的颜色
        int color = count > 32 ? new Color(63, 157, 4, 150).getRGB() : (count < 16 ? new Color(168, 1, 1, 150).getRGB() : new Color(255, 144, 2, 150).getRGB());

        switch (count_Value.getValue()) {
            case "Modern": {
                FontManager font = Loratadine.INSTANCE.getFontManager();

                float blockWH = heldItem != null ? 15 : -2;
                String text = "§l" + countStr + "§r Block" + (count != 1 ? "s" : "");
                float textWidth = Loratadine.INSTANCE.getFontManager().tenacity18.getStringWidth(text);

                float totalWidth = ((textWidth + blockWH + 3) + 6 + 2) * output;
                x = mc.getWindow().getGuiScaledWidth() / 2f - (totalWidth / 2f);
                y = mc.getWindow().getGuiScaledHeight() - (mc.getWindow().getGuiScaledHeight() / 2f + 30);
                float height = 20;

                // 初始绘制
                poseStack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderUtils.startGlScissor((int) (x - 1.5), (int) (y - 1.5), (int) (totalWidth + 3), (int) (height + 3));
                RenderUtils.drawRectangle(poseStack, x, y, totalWidth, height, new Color(20, 20, 20, 100).getRGB());
                RenderUtils.drawRectangle(poseStack, x, y + height / 2 - 4, 1, 8, color);

                font.tenacity18.drawString(poseStack, text, x + 2 + blockWH + 3 + 2, y + Loratadine.INSTANCE.getFontManager().tenacity18.getMiddleOfBox(height), -1);
             
                if (heldItem != null) {
                    Lighting.setupFor3DItems();
                    RenderUtils.renderGuiItem(new PoseStack(), heldItem, (int) x + 3 + 1, (int) (y + 10 - (blockWH / 2)));
                    Lighting.setupForFlatItems();

                }

                RenderUtils.stopGlScissor();
                poseStack.popPose();


                break;
            }

            case "Basic": {
                // 初始绘制
                poseStack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                double scale = 0.7;
                RenderSystem.setShaderColor(1, 1, 1, 1);
                poseStack.scale((float) scale, (float) scale, (float) scale);

                x = mc.getWindow().getGuiScaledWidth() / 2F - mc.font.width(str) / 2F + 1;
                y = mc.getWindow().getGuiScaledHeight() / 2F + 10;
                WrapperUtils.drawShadow(poseStack, str, x, y, Color.WHITE.getRGB());

                // 结束绘制
                poseStack.popPose();
                break;
            }

            case "Simple":{
                Font font = mc.font;

                x = mc.getWindow().getGuiScaledWidth() / 2F - font.width(countStr) / 2F + (heldItem != null ? 6 : 1);
                y = mc.getWindow().getGuiScaledHeight() / 2F + 10;

                // 初始绘制
                poseStack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // 动画
                poseStack.translate(x + (heldItem == null ? 1 : 0), y, 1);
                poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 1);
                poseStack.translate(-x - (heldItem == null ? 1 : 0), -y, 1);

                RenderUtils.drawMcFontOutlinedString(poseStack, countStr, x, y, ColorUtils.applyOpacity(color, output),false);

                if (heldItem != null) {
                    Lighting.setupFor3DItems();
                    double scale = 0.7;
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    poseStack.scale((float) scale, (float) scale, (float) scale);

                    RenderUtils.renderGuiItem(
                            poseStack,
                            heldItem,
                            (int) ((mc.getWindow().getGuiScaledWidth() / 2F - font.width(countStr) / 2F - 7) / scale),
                            (int) ((mc.getWindow().getGuiScaledHeight() / 2F + 8.5F) / scale)
                    );
                    Lighting.setupForFlatItems();
                }

                // 结束绘制
                poseStack.popPose();
                break;
            }
        }
    }

    private record BlockPosWithFacing(BlockPos position, Direction facing) {
        // Empty ？ WTF ?
    }
}