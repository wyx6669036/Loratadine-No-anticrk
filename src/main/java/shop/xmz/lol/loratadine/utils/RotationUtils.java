package shop.xmz.lol.loratadine.utils;

import cn.lzq.injection.leaked.invoked.*;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.impl.setting.MoveFix;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.helper.Vector3d;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Random;

public class RotationUtils implements Wrapper {
    @Getter
    private static Rotation angle;
    private static float lastForward, lastStrafe;
    public static int keepLength, revTick;

    public static void init() {
        Loratadine.INSTANCE.getEventManager().register(new RotationUtils());
    }

    public static void setRotation(Rotation rotation) {
        angle = rotation;
        keepLength = 0;
    }

    public static void setRotation(Rotation rotation, int keepLength) {
        angle = rotation;
        RotationUtils.keepLength = keepLength;
    }

    public static void reset() {
        if (mc.player == null) return;

        keepLength = 0;
        if (revTick > 0) {
            angle = new Rotation(angle.yaw - getAngleDifference(angle.yaw, mc.player.getYRot()) / revTick
                    , angle.pitch - getAngleDifference(angle.pitch, mc.player.getXRot()) / revTick);
            angle.fixedSensitivity(mc.options.sensitivity().get());
        } else {
            lastForward = lastStrafe = 0;
            angle = null;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (angle != null) {
            keepLength--;
            if (keepLength < 0) {
                if (revTick > 0) {
                    revTick--;
                }
                reset();
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent e) {
        if (e.getSide() == Event.Side.POST) {
            if (e.getPacket() instanceof ServerboundMovePlayerPacket packet) {
                if (!e.isCancelled()) {
                    if (packet.getYRot(0) < 360 && packet.getYRot(0) > -360) {
                        WrapperUtils.setPacketYRot(packet, packet.getYRot(0) + 720F);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUseItem(UseItemEvent event) {
        if (angle != null) {
            event.setYaw(angle.yaw);
            event.setPitch(angle.pitch);
        }
    }

    @EventTarget
    public void onPitchRender(PitchRenderEvent event) {
        if (angle != null && MoveFix.INSTANCE.renderRotation.getValue()) {
            event.pitch = angle.pitch;
        }
    }

    @EventTarget
    public void onRenderer(RenderPlayerEvent event) {
        if (angle != null && MoveFix.INSTANCE.renderRotation.getValue()) {
            event.rotationYaw = angle.yaw;
            event.rotationPitch = angle.pitch;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (angle != null && !event.post) {
            event.setYaw(angle.yaw);
            event.setPitch(angle.pitch);
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (angle != null) {
            event.setRotationYaw(angle.yaw);
        }
    }

    @EventTarget
    public void onJump(JumpEvent event) {
        if (angle != null) {
            event.setRotationYaw(angle.yaw);
        }
    }

    @EventTarget
    public void onLook(LookEvent event) {
        if (angle != null) {
            event.rotationYaw = angle.yaw;
            event.rotationPitch = angle.pitch;
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player == null || angle == null || MoveFix.INSTANCE.strictValue.getValue() || event.forwardImpulse == lastForward && event.leftImpulse == lastStrafe)
            return;

        final float forward = event.forwardImpulse;
        final float strafe = event.leftImpulse;

        final double yaw = Mth.wrapDegrees(Math.toDegrees(MoveUtils.direction(Mth.wrapDegrees(mc.player.getYRot()), forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = Mth.wrapDegrees(Math.toDegrees(MoveUtils.direction(Mth.wrapDegrees(angle.yaw), predictedForward, predictedStrafe)));
                final double difference = Math.abs(yaw - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        lastForward = closestForward;
        lastStrafe = closestStrafe;
        event.forwardImpulse = closestForward;
        event.leftImpulse = closestStrafe;
    }

    /**
     * Returns the distance to the entity. Args: entity
     */
    public static float getDistanceToEntity(Entity target) {
        if (mc.player == null) return 0.0F;

        Vec3 eyes = mc.player.getEyePosition(1F);
        Vec3 pos = getNearestPointBB(eyes, target.getBoundingBox());
        double xDist = Math.abs(pos.x - eyes.x);
        double yDist = Math.abs(pos.y - eyes.y);
        double zDist = Math.abs(pos.z - eyes.z);
        return (float) Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2) + Math.pow(zDist, 2));
    }

    public static double getYaw(Entity entity) {
        if (mc.player == null) return 0.0D;

        return mc.player.getYRot() + Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX())) - 90f - mc.player.getYRot());
    }

    public static Rotation getRotationForEntity(Entity entity) {
        if (mc.player == null) return null;

        Vec3 playerEyePos = mc.player.getEyePosition(1.0F);
        Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        Vec3 direction = targetPos.subtract(playerEyePos).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        return new Rotation(yaw, pitch);
    }

    public static Rotation calculate(final Vector3d position, final Direction direction) {
        double x = position.x + 0.5D;
        double y = position.y + 0.5D;
        double z = position.z + 0.5D;

        // 计算方向向量
        x += direction.getStepX() * 0.5D;
        y += direction.getStepY() * 0.5D;
        z += direction.getStepZ() * 0.5D;

        return calculate(new Vector3d(x, y, z));
    }


    public static Rotation calculate(Vector3d target) {
        if (mc.player == null) return null;

        // 计算视角所需的偏移量
        Vec3 eyePosition = mc.player.getEyePosition(1.0F);
        double deltaX = target.x - eyePosition.x;
        double deltaY = target.y - eyePosition.y;
        double deltaZ = target.z - eyePosition.z;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        return new Rotation(yaw, pitch);
    }

    public static Rotation getAngles(Entity entity) {
        if (entity == null) return null;

        // 获取当前玩家实例
        final LocalPlayer thePlayer = mc.player;

        if (thePlayer == null) return null;

        // 计算位置差异
        final double diffX = entity.getX() - thePlayer.getX(),
                diffY = entity.getY() + entity.getEyeHeight() * 0.9 - (thePlayer.getY() + thePlayer.getEyeHeight()),
                diffZ = entity.getZ() - thePlayer.getZ();

        // 计算水平距离
        final double dist = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));

        // 计算旋转角度
        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        final float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        // 返回旋转结果
        return new Rotation(
                thePlayer.getYRot() + Mth.wrapDegrees(yaw - thePlayer.getYRot()),
                thePlayer.getXRot() + Mth.wrapDegrees(pitch - thePlayer.getXRot())
        );
    }

    public static boolean isInViewRange(float fov, LivingEntity entity) {
        if (mc.player == null) return false;

        Vec3 playerPos = mc.player.getEyePosition(1.0F);
        Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 toTargetVec = targetPos.subtract(playerPos).normalize();

        double dotProduct = lookVec.dot(toTargetVec);
        double angle = Math.toDegrees(Math.acos(dotProduct));

        return fov == 360F || angle <= fov;
    }

    public static float[] getRotations(BlockPos pos, float partialTicks) {
        if (mc.player == null) return new float[]{};

        Vec3 playerVector = new Vec3(mc.player.getX() + mc.player.getDeltaMovement().x * partialTicks, mc.player.getY() + mc.player.getEyeHeight() + mc.player.getDeltaMovement().y() * partialTicks, mc.player.getZ() + mc.player.getDeltaMovement().z() * partialTicks);
        double x = pos.getX() - playerVector.x + 0.5;

        double y = pos.getY() - playerVector.y + 0.5 + 0.2;
        double z = pos.getZ() - playerVector.z + 0.5;
        return diffCalc(x, y, z);
    }

    /**
     * Calculate difference between two angle points
     *
     * @param a angle point
     * @param b angle point
     * @return difference between angle points
     */
    public static float getAngleDifference(final float a, final float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public static double getRotationDifference(final Rotation a, final Rotation b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getYaw()), a.getPitch() - b.getPitch());
    }

    public static float[] diffCalc(double diffX, double diffY, double diffZ) {
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{Mth.wrapDegrees(yaw), Mth.wrapDegrees(pitch)};
    }

    public static Vec3 getNearestPointBB(Vec3 eye, AABB box) {
        double[] origin = {eye.x, eye.y, eye.z};
        double[] destMinis = {box.minX, box.minY, box.minZ};
        double[] destMaxis = {box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i < 3; i++) {
            if (origin[i] > destMaxis[i]) {
                origin[i] = destMaxis[i];
            } else if (origin[i] < destMinis[i]) {
                origin[i] = destMinis[i];
            }
        }

        return new Vec3(origin[0], origin[1], origin[2]);
    }

    public static float[] getRotationFromEyeToPoint(Vector3d point3d) {
        if (mc.player == null) return new float[]{};

        return getRotation(new Vector3d(mc.player.getX(), mc.player.getBoundingBox().minY + mc.player.getEyeHeight(), mc.player.getZ()), point3d);
    }

    public static float[] getRotation(Vector3d from, Vector3d to) {
        final double x = to.getX() - from.getX();
        final double y = to.getY() - from.getY();
        final double z = to.getZ() - from.getZ();

        final double sqrt = Math.sqrt(x * x + z * z);

        final float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90F;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(y, sqrt)));

        return new float[]{yaw, Math.min(Math.max(pitch, -90), 90)};
    }

    private static float[] getRotationsByVec(final Vec3 origin, final Vec3 position) {
        final Vec3 difference = position.subtract(origin);
        final double distance = flat(difference).length();
        final float yaw = (float) Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(difference.y, distance)));
        return new float[]{yaw, pitch};
    }

    public static Vec3 flat(Vec3 s) {
        return new Vec3(s.x, 0.0, s.z);
    }

    public static float[] getRotationBlock(final BlockPos pos) {
        if (mc.player == null) return new float[]{0.0F, 0.0F};

        return getRotationsByVec(mc.player.position().add(0.0, mc.player.getEyeHeight(), 0.0), new Vec3(pos.getX() + 0.51, pos.getY() + 0.51, pos.getZ() + 0.51));
    }

    public static float[] getRotationBlock(final BlockPos pos, final Direction facing) {
        if (mc.player == null || mc.level == null) return new float[]{0.0F, 0.0F};

        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Vec3 dirVec = new Vec3(facing.getNormal().getX(), facing.getNormal().getY(), facing.getNormal().getZ());

        double dirX = dirVec.x * 0.5;
        double dirY = dirVec.y * 0.5;
        double dirZ = dirVec.z * 0.5;

        Vec3 hitVec = center.add(new Vec3(dirX, dirY, dirZ));

        return getRotationsByVec(mc.player.position().add(0.0, mc.player.getEyeHeight(), 0.0), hitVec);
    }

    /**
     * Calculate rotation to vector
     *
     * @param rotation your rotation
     * @return target vector
     */
    public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = (float) Math.cos(-rotation.getYaw() * 0.017453292F - 3.1415927F);
        float yawSin = (float) Math.sin(-rotation.getYaw() * 0.017453292F - 3.1415927F);
        float pitchCos = (float) -Math.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = (float) Math.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    /**
     * Get the closest point on a boundingBox from start
     *
     * @param start       Src
     * @param boundingBox boundingBox to calculate closest point from start
     * @return The closest point on boundingBox as a hit vec
     */
    public static Vec3 getClosestPoint(final Vec3 start,
                                       final AABB boundingBox) {
        final double closestX = start.x >= boundingBox.maxX ? boundingBox.maxX :
                start.x <= boundingBox.minX ? boundingBox.minX :
                        boundingBox.minX + (start.x - boundingBox.minX);

        final double closestY = start.y >= boundingBox.maxY ? boundingBox.maxY :
                start.y <= boundingBox.minY ? boundingBox.minY :
                        boundingBox.minY + (start.y - boundingBox.minY);

        final double closestZ = start.z >= boundingBox.maxZ ? boundingBox.maxZ :
                start.z <= boundingBox.minZ ? boundingBox.minZ :
                        boundingBox.minZ + (start.z - boundingBox.minZ);

        return new Vec3(closestX, closestY, closestZ);
    }

    public static float[] getHVHRotation(Entity entity) {
        if (entity == null || mc.player == null) return null;


        final Player player = mc.player;
        final double playerX = player.getX();
        final double playerY = player.getY() + player.getEyeHeight();
        final double playerZ = player.getZ();


        final Vec3 eyePosition = new Vec3(playerX, playerY, playerZ);
        final Vec3 bestPos = getClosestPoint(eyePosition, entity.getBoundingBox());
        if (bestPos == null) return null;


        final double diffX = bestPos.x - playerX;
        final double diffZ = bestPos.z - playerZ;
        final double diffY = bestPos.y - eyePosition.y;


        final double horizontalDistance = Math.hypot(diffX, diffZ);

        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        final float pitch = (float) -Math.toDegrees(Math.atan2(diffY, horizontalDistance));

        return new float[]{
                Mth.wrapDegrees(yaw),
                Mth.wrapDegrees(pitch)
        };
    }

    public static float[] getSimpleRotations(LivingEntity target) {
        if (mc.player == null) return new float[]{};

        Vector3d targetPos; // i paste
        final double yDist = target.getY() - mc.player.getY();
        if (yDist >= 1.547) {
            targetPos = new Vector3d(target.getX(), target.getY(), target.getZ());
        } else if (yDist <= -1.547) {
            targetPos = new Vector3d(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
        } else {
            targetPos = new Vector3d(target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ());
        }
        return getRotationFromEyeToPoint(targetPos);
    }

    public static float[] getFacingRotations(final int x, final double y, final int z) {
        if (mc.level == null) return new float[]{};

        // 创建一个雪球实体
        Snowball snowball = new Snowball(mc.level, x + 0.5, y + 0.5, z + 0.5);

        // 返回需要的旋转角度
        return getRotationsNeeded(snowball);
    }

    public static float[] getRotationsNeeded(Entity entity) {
        if (mc.player == null) return new float[]{};

        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 entityPos = entity.position();
        Vec3 delta = entityPos.subtract(playerPos);

        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontalDistance));

        return new float[]{yaw, pitch};
    }

    public static void setVisualRotations(float yaw, float pitch) {
        if (mc.player == null) return;

        mc.player.yHeadRot = mc.player.yBodyRot = yaw; // 设置头部和身体的 Yaw 值
        mc.player.xRotO = pitch; // 设置 Pitch 值
    }












    // 仿真参数
  /*  private static final float MAX_TREMOR = 0.01f;

    private static final float HUMAN_REACTION = 0.88f;
    private static final float PREDICT_FACTOR = 0.45f;
    private static final float AIM_STABILITY = 0.92f;
    private static final float SMOOTHNESS = 0.01f;
    private static final Random RANDOM = new Random();
    private static float lastYaw = 0;
    private static float lastPitch = 0;


    private static final float DYNAMIC_RESPONSE = 1.25f;

    private static final float VELOCITY_THRESHOLD = 0.4f;

    private static float perlinNoise1D(float x) {
        int x0 = (int) Math.floor(x);
        float t = x - x0;
        float tRemap = t * t * (3 - 2 * t);

        float a = RANDOM.nextFloat() * 2 - 1;
        float b = RANDOM.nextFloat() * 2 - 1;

        return Mth.lerp(tRemap, a, b);
    }



    private static double perlinNoise3D(double x, double y, double z) {
        return noise(x, y, z);
    }


    private static final int[] p = new int[512];
    private static final int[] permutation = {
            151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
            8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117,
            35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71,
            134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41,
            55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89,
            18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226,
            250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182,
            189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43,
            172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97,
            228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
            49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138,
            236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    static {
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
    }

    public static double noise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        int Z = (int)Math.floor(z) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;

        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                                grad(p[BA], x-1, y, z)),
                        lerp(u, grad(p[AB], x, y-1, z),
                                grad(p[BB], x-1, y-1, z))),
                lerp(v, lerp(u, grad(p[AA+1], x, y, z-1),
                                grad(p[BA+1], x-1, y, z-1)),
                        lerp(u, grad(p[AB+1], x, y-1, z-1),
                                grad(p[BB+1], x-1, y-1, z-1))));
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }


    //test

    private static float[] applyNeuromuscularControl(float targetYaw, float targetPitch) {
        updateAttentionSystem();

        float yawDiff = Mth.wrapDegrees(targetYaw - lastYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - lastPitch);


        if(Math.abs(yawDiff) < 1.2f) yawDiff = 0;
        if(Math.abs(pitchDiff) < 0.8f) pitchDiff = 0;


        float deltaYaw = Mth.clamp(yawDiff * HUMAN_REACTION, -3.8f, 3.8f);
        float deltaPitch = Mth.clamp(pitchDiff * HUMAN_REACTION, -2.7f, 2.7f);


        deltaYaw *= AIM_STABILITY * (0.9f + RANDOM.nextFloat() * 0.2f);
        deltaPitch *= AIM_STABILITY * (0.9f + RANDOM.nextFloat() * 0.2f);


        if(RANDOM.nextFloat() < 0.15f) {
            float overshoot = 1.08f + RANDOM.nextFloat() * 0.15f;
            deltaYaw = Mth.clamp(deltaYaw * overshoot, -4.5f, 4.5f);
            deltaPitch = Mth.clamp(deltaPitch * overshoot, -3.0f, 3.0f);
        }


        lastYaw = Mth.wrapDegrees(lastYaw + deltaYaw * (1 - SMOOTHNESS));
        lastPitch = Mth.clamp(lastPitch + deltaPitch * (1 - SMOOTHNESS), -90, 90);

        float timeFactor = (System.currentTimeMillis() % 1250) / 1250f;
        float tremor = (float) Math.sin(timeFactor * Math.PI * 5) * MAX_TREMOR;
        tremor *= 0.5f + RANDOM.nextFloat() * 0.5f;

        return new float[]{
                Mth.wrapDegrees(lastYaw + tremor * 0.6f),
                Mth.clamp(lastPitch + tremor * 0.3f, -90, 90)
        };
    }



    private static Vec3 getDynamicHitboxPoint(Entity entity) {
        AABB box = entity.getBoundingBox();
        long seed = (long)(entity.getId() * 13762.3f);
        double time = System.currentTimeMillis() / 1200.0;


        Vec3 velocity = entity.getDeltaMovement();
        double velX = velocity.x * PREDICT_FACTOR * (0.8 + RANDOM.nextDouble()*0.4);
        double velY = velocity.y * PREDICT_FACTOR * 0.6;
        double velZ = velocity.z * PREDICT_FACTOR * (0.8 + RANDOM.nextDouble()*0.4);

        return new Vec3(
                box.minX + box.getXsize() * (0.15 + 0.7*perlinNoise3D(seed+time, seed+1000, time)),
                box.minY + box.getYsize() * (0.2 + 0.6*perlinNoise3D(seed+5000, seed+2000, time+0.5)),
                box.minZ + box.getZsize() * (0.15 + 0.7*perlinNoise3D(seed+10000, seed+3000, time+1.0))
        ).add(velX, velY, velZ);
    }




    private static float attentionLevel = 1.0f;
    private static long lastAttentionUpdate = 0;

    private static void updateAttentionSystem() {
        long now = System.currentTimeMillis();
        if(now - lastAttentionUpdate > 1500) {

            attentionLevel = Mth.clamp(attentionLevel * (0.97f + RANDOM.nextFloat()*0.06f), 0.6f, 1.2f);
            lastAttentionUpdate = now;
        }
    }

    public static float[] AIRotation(Entity entity) {

        final int SAMPLE_COUNT = 1;
        float[] yawSamples = new float[SAMPLE_COUNT];
        float[] pitchSamples = new float[SAMPLE_COUNT];

        for(int i=0; i<SAMPLE_COUNT; i++) {
            Vec3 targetPos = getDynamicHitboxPoint(entity);
            Vec3 myEyePos = mc.player.getEyePosition(1f);
            Vec3 diff = targetPos.subtract(myEyePos);

            double dist2D = Math.sqrt(diff.x*diff.x + diff.z*diff.z) + 0.0001;
            yawSamples[i] = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
            pitchSamples[i] = (float) -Math.toDegrees(Math.atan2(diff.y, dist2D));


            try { Thread.sleep(RANDOM.nextInt(8)); } catch (InterruptedException e) {}
        }


        float targetYaw = (yawSamples[0]*0.5f + yawSamples[1]*0.3f + yawSamples[2]*0.2f);
        float targetPitch = (pitchSamples[0]*0.6f + pitchSamples[1]*0.3f + pitchSamples[2]*0.1f);

        return applyNeuromuscularControl(targetYaw, targetPitch);
    }











    private static float[] applyNeuromuscularControls(float targetYaw, float targetPitch) {
        updateAttentionSystem();


        float yawVelocity = Math.abs(Mth.wrapDegrees(targetYaw - lastYaw));
        float pitchVelocity = Math.abs(Mth.wrapDegrees(targetPitch - lastPitch));


        float speedFactor = Mth.clamp((yawVelocity + pitchVelocity) * 0.8f, 1.0f, 2.5f);
        float dynamicReaction = HUMAN_REACTION * speedFactor * DYNAMIC_RESPONSE;


        float yawDiff = Mth.wrapDegrees(targetYaw - lastYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - lastPitch);


        if(yawVelocity > VELOCITY_THRESHOLD) yawDiff *= 1.3f;
        if(pitchVelocity > VELOCITY_THRESHOLD) pitchDiff *= 1.2f;


        float deltaYaw = Mth.clamp(yawDiff * dynamicReaction, -5.2f, 5.2f);
        float deltaPitch = Mth.clamp(pitchDiff * dynamicReaction, -3.8f, 3.8f);


        deltaYaw *= AIM_STABILITY * (0.85f + RANDOM.nextFloat() * 0.3f);
        deltaPitch *= AIM_STABILITY * (0.85f + RANDOM.nextFloat() * 0.3f);


        if(RANDOM.nextFloat() < 0.22f) {
            float overshoot = 1.15f + RANDOM.nextFloat() * 0.25f; 
            deltaYaw = Mth.clamp(deltaYaw * overshoot, -6.0f, 6.0f);
            deltaPitch = Mth.clamp(deltaPitch * overshoot, -4.5f, 4.5f);
        }


        lastYaw = Mth.wrapDegrees(lastYaw + deltaYaw);
        lastPitch = Mth.clamp(lastPitch + deltaPitch, -90, 90);


        float tremor = (float) (perlinNoise3D(
                System.currentTimeMillis()*0.002,
                lastYaw*0.01,
                lastPitch*0.01
        )) * MAX_TREMOR * 1.3f;

        return new float[]{
                Mth.wrapDegrees(lastYaw + tremor * 0.7f),
                Mth.clamp(lastPitch + tremor * 0.4f, -90, 90)
        };
    }


    private static Vec3 getDynamicHitboxPoints(Entity entity) {

        final double PREDICT_MULTIPLIER = 1.8f;
        Vec3 velocity = entity.getDeltaMovement();


        double speed = velocity.length() * 20;
        double dynamicFactor = Mth.clamp(speed * 0.15, 0.8, 1.6);


        double velX = velocity.x * PREDICT_FACTOR * dynamicFactor * PREDICT_MULTIPLIER;
        double velY = velocity.y * PREDICT_FACTOR * 0.8;
        double velZ = velocity.z * PREDICT_FACTOR * dynamicFactor * PREDICT_MULTIPLIER;


        long seed = (long)(entity.getId() * 937);
        double time = System.currentTimeMillis() / 800.0;

        return new Vec3(
                entity.getX() + (RANDOM.nextDouble()*0.6 -0.3) * dynamicFactor,
                entity.getY() + entity.getEyeHeight() * 0.8,
                entity.getZ() + (RANDOM.nextDouble()*0.6 -0.3) * dynamicFactor
        ).add(velX, velY, velZ);
    }


    public static float[] getHVHRotationtest(Entity entity) {

        final int FAST_SAMPLE_COUNT = 2;
        float[] yawSamples = new float[FAST_SAMPLE_COUNT];
        float[] pitchSamples = new float[FAST_SAMPLE_COUNT];

        for(int i=0; i<FAST_SAMPLE_COUNT; i++) {
            Vec3 targetPos = getDynamicHitboxPoints(entity);
            Vec3 myEyePos = mc.player.getEyePosition(1f);
            Vec3 diff = targetPos.subtract(myEyePos);


            double dist2D = Math.hypot(diff.x, diff.z);
            yawSamples[i] = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
            pitchSamples[i] = (float) -Math.toDegrees(Math.atan2(diff.y, dist2D));


            try { Thread.sleep(RANDOM.nextInt(3)); } catch (InterruptedException e) {}
        }


        float targetYaw = yawSamples[0]*0.7f + yawSamples[1]*0.3f;
        float targetPitch = pitchSamples[0]*0.8f + pitchSamples[1]*0.2f;

        return applyNeuromuscularControls(targetYaw, targetPitch);
    }*/

//test









    private static final float MAX_TREMOR = 0.05f;    // 抖动幅度
    private static final float AIM_STABILITY = 0.92f;  // 稳定性系数
    private static final float HUMAN_REACTION = 0.88f; // 反应系数
    private static final float SMOOTHNESS = 0.3f;    // 平滑度
    private static final Random RANDOM = new Random();
    private static float lastYaw = 0;
    private static float lastPitch = 0;


    private static float perlinNoise1D(float x) {
        int x0 = (int) Math.floor(x);
        float t = x - x0;
        float tRemap = t * t * (3 - 2 * t);

        float a = RANDOM.nextFloat() * 2 - 1;
        float b = RANDOM.nextFloat() * 2 - 1;

        return Mth.lerp(tRemap, a, b);
    }



    private static double perlinNoise3D(double x, double y, double z) {
        return noise(x, y, z);
    }


    private static final int[] p = new int[512];
    private static final int[] permutation = {
            151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
            8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117,
            35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71,
            134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41,
            55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89,
            18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226,
            250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182,
            189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43,
            172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97,
            228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
            49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138,
            236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    static {
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
    }

    public static double noise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        int Z = (int)Math.floor(z) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;

        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                                grad(p[BA], x-1, y, z)),
                        lerp(u, grad(p[AB], x, y-1, z),
                                grad(p[BB], x-1, y-1, z))),
                lerp(v, lerp(u, grad(p[AA+1], x, y, z-1),
                                grad(p[BA+1], x-1, y, z-1)),
                        lerp(u, grad(p[AB+1], x, y-1, z-1),
                                grad(p[BB+1], x-1, y-1, z-1))));
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }


    //test

    private static float[] applyNeuromuscularControl(float targetYaw, float targetPitch) {
        updateAttentionSystem();
        float yawDiff = Mth.wrapDegrees(targetYaw - lastYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - lastPitch);
        if(Math.abs(yawDiff) < 1.2f) yawDiff = 0;
        if(Math.abs(pitchDiff) < 0.8f) pitchDiff = 0;

        float deltaYaw = Mth.clamp(yawDiff * HUMAN_REACTION, -3.8f, 3.8f);
        float deltaPitch = Mth.clamp(pitchDiff * HUMAN_REACTION, -2.7f, 2.7f);

        deltaYaw *= AIM_STABILITY * (0.9f + RANDOM.nextFloat() * 0.2f);
        deltaPitch *= AIM_STABILITY * (0.9f + RANDOM.nextFloat() * 0.2f);

        if(RANDOM.nextFloat() < 0.15f) {
            float overshoot = 1.08f + RANDOM.nextFloat() * 0.15f;
            deltaYaw = Mth.clamp(deltaYaw * overshoot, -4.5f, 4.5f);
            deltaPitch = Mth.clamp(deltaPitch * overshoot, -3.0f, 3.0f);
        }

        lastYaw = Mth.wrapDegrees(lastYaw + deltaYaw * (1 - SMOOTHNESS));
        lastPitch = Mth.clamp(lastPitch + deltaPitch * (1 - SMOOTHNESS), -90, 90);


        float timeFactor = (System.currentTimeMillis() % 1250) / 1250f;
        float tremor = (float) Math.sin(timeFactor * Math.PI * 5) * MAX_TREMOR;
        tremor *= 0.5f + RANDOM.nextFloat() * 0.5f;

        return new float[]{
                Mth.wrapDegrees(lastYaw + tremor * 0.6f),
                Mth.clamp(lastPitch + tremor * 0.3f, -90, 90)
        };
    }

    private static final float PREDICT_FACTOR = 0.05f;
    private static Vec3 getDynamicHitboxPoint(Entity entity) {
        AABB box = entity.getBoundingBox();
        long seed = (long)(entity.getId() * 13762.3f);
        double time = System.currentTimeMillis() / 1200.0;

        Vec3 velocity = entity.getDeltaMovement();
        double velX = velocity.x * PREDICT_FACTOR * (0.8 + RANDOM.nextDouble()*0.4);
        double velY = velocity.y * PREDICT_FACTOR * 0.6;
        double velZ = velocity.z * PREDICT_FACTOR * (0.8 + RANDOM.nextDouble()*0.4);

        return new Vec3(
                box.minX + box.getXsize() * (0.15 + 0.7*perlinNoise3D(seed+time, seed+1000, time)),
                box.minY + box.getYsize() * (0.2 + 0.6*perlinNoise3D(seed+5000, seed+2000, time+0.5)),
                box.minZ + box.getZsize() * (0.15 + 0.7*perlinNoise3D(seed+10000, seed+3000, time+1.0))
        ).add(velX, velY, velZ);
    }

    private static float attentionLevel = 1.0f;
    private static long lastAttentionUpdate = 0;

    private static void updateAttentionSystem() {
        long now = System.currentTimeMillis();
        if(now - lastAttentionUpdate > 1500) {
            attentionLevel = Mth.clamp(attentionLevel * (0.97f + RANDOM.nextFloat()*0.06f), 0.6f, 1.2f);
            lastAttentionUpdate = now;
        }
    }

    public static float[] getAiRotationtest(Entity entity) {
        final int FAST_SAMPLE_COUNT = 2;
        float[] yawSamples = new float[FAST_SAMPLE_COUNT];
        float[] pitchSamples = new float[FAST_SAMPLE_COUNT];

        for(int i=0; i<FAST_SAMPLE_COUNT; i++) {
            Vec3 targetPos = getDynamicHitboxPoints(entity);
            Vec3 myEyePos = mc.player.getEyePosition(1f);
            Vec3 diff = targetPos.subtract(myEyePos);

            double dist2D = Math.hypot(diff.x, diff.z);
            yawSamples[i] = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
            pitchSamples[i] = (float) -Math.toDegrees(Math.atan2(diff.y, dist2D));

            try { Thread.sleep(RANDOM.nextInt(3)); } catch (InterruptedException e) {}
        }

        float targetYaw = yawSamples[0]*0.7f + yawSamples[1]*0.3f;
        float targetPitch = pitchSamples[0]*0.8f + pitchSamples[1]*0.2f;

        return applyNeuromuscularControls(targetYaw, targetPitch);
    }
    public static float[] getRotationTest(Entity entity) {
        final int SAMPLE_COUNT = 3;
        float[] yawSamples = new float[SAMPLE_COUNT];
        float[] pitchSamples = new float[SAMPLE_COUNT];

        for(int i=0; i<SAMPLE_COUNT; i++) {
            Vec3 targetPos = getDynamicHitboxPoint(entity);
            Vec3 myEyePos = mc.player.getEyePosition(1f);
            Vec3 diff = targetPos.subtract(myEyePos);

            double dist2D = Math.sqrt(diff.x*diff.x + diff.z*diff.z) + 0.0001;
            yawSamples[i] = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
            pitchSamples[i] = (float) -Math.toDegrees(Math.atan2(diff.y, dist2D));


            try { Thread.sleep(RANDOM.nextInt(8)); } catch (InterruptedException e) {}
        }

        float targetYaw = (yawSamples[0]*0.5f + yawSamples[1]*0.3f + yawSamples[2]*0.2f);
        float targetPitch = (pitchSamples[0]*0.6f + pitchSamples[1]*0.3f + pitchSamples[2]*0.1f);

        return applyNeuromuscularControl(targetYaw, targetPitch);
    }


    private static final float DYNAMIC_RESPONSE = 1.25f;

    private static final float VELOCITY_THRESHOLD = 0.4f;


    private static float[] applyNeuromuscularControls(float targetYaw, float targetPitch) {
        updateAttentionSystem();


        float yawVelocity = Math.abs(Mth.wrapDegrees(targetYaw - lastYaw));
        float pitchVelocity = Math.abs(Mth.wrapDegrees(targetPitch - lastPitch));


        float speedFactor = Mth.clamp((yawVelocity + pitchVelocity) * 0.8f, 1.0f, 2.5f);
        float dynamicReaction = HUMAN_REACTION * speedFactor * DYNAMIC_RESPONSE;


        float yawDiff = Mth.wrapDegrees(targetYaw - lastYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - lastPitch);


        if(yawVelocity > VELOCITY_THRESHOLD) yawDiff *= 1.3f;
        if(pitchVelocity > VELOCITY_THRESHOLD) pitchDiff *= 1.2f;


        float deltaYaw = Mth.clamp(yawDiff * dynamicReaction, -5.2f, 5.2f);
        float deltaPitch = Mth.clamp(pitchDiff * dynamicReaction, -3.8f, 3.8f);


        deltaYaw *= AIM_STABILITY * (0.85f + RANDOM.nextFloat() * 0.3f);
        deltaPitch *= AIM_STABILITY * (0.85f + RANDOM.nextFloat() * 0.3f);


        if(RANDOM.nextFloat() < 0.22f) {
            float overshoot = 1.15f + RANDOM.nextFloat() * 0.25f;
            deltaYaw = Mth.clamp(deltaYaw * overshoot, -6.0f, 6.0f);
            deltaPitch = Mth.clamp(deltaPitch * overshoot, -4.5f, 4.5f);
        }


        lastYaw = Mth.wrapDegrees(lastYaw + deltaYaw);
        lastPitch = Mth.clamp(lastPitch + deltaPitch, -90, 90);


        float tremor = (float) (perlinNoise3D(
                System.currentTimeMillis()*0.001,
                lastYaw*0.01,
                lastPitch*0.01
        )) * MAX_TREMOR * 1.3f;

        return new float[]{
                Mth.wrapDegrees(lastYaw + tremor * 0.7f),
                Mth.clamp(lastPitch + tremor * 0.4f, -90, 90)
        };
    }


    private static Vec3 getDynamicHitboxPoints(Entity entity) {

        final double PREDICT_MULTIPLIER = 1.8f; // 新增预测倍率
        Vec3 velocity = entity.getDeltaMovement();


        double speed = velocity.length() * 20;
        double dynamicFactor = Mth.clamp(speed * 0.15, 0.8, 1.6);


        double velX = velocity.x * PREDICT_FACTOR * dynamicFactor * PREDICT_MULTIPLIER;
        double velY = velocity.y * PREDICT_FACTOR * 0.8;
        double velZ = velocity.z * PREDICT_FACTOR * dynamicFactor * PREDICT_MULTIPLIER;


        long seed = (long)(entity.getId() * 937);
        double time = System.currentTimeMillis() / 800.0;

        return new Vec3(
                entity.getX() + (RANDOM.nextDouble()*0.6 -0.3) * dynamicFactor,
                entity.getY() + entity.getEyeHeight() * 0.8,
                entity.getZ() + (RANDOM.nextDouble()*0.6 -0.3) * dynamicFactor
        ).add(velX, velY, velZ);
    }
}
