package shop.xmz.lol.loratadine.antileak.client;

import org.jetbrains.annotations.NotNull;
import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.packet.impls.receive.*;
import shop.xmz.lol.loratadine.antileak.utils.CryptUtil;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.nio.ByteBuffer;
import java.util.*;

public class Handler extends ChannelInboundHandlerAdapter implements Wrapper {
    private static final HashMap<Integer, Class<? extends Packet>> packetMap = new HashMap<>();

    public static void initPackets() {
        packetMap.put(-1, SPacketKeepAlive.class);
        packetMap.put(0, SPacketHandShake.class);
        packetMap.put(1, SPacketNotifications.class);
        packetMap.put(2, SPacketPlayer.class);
        packetMap.put(3, SPacketCrash.class);
        packetMap.put(4, SPacketChat.class);
    }

    public static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        channelGroup.remove(ctx.channel());
        channelSlices.remove(ctx.channel());
    }

    private final Map<Channel, List<byte[]>> channelSlices = new HashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, @NotNull Object msg) {
        final Channel channel = ctx.channel();
        if (msg instanceof ByteBuf byteBuf) {

            final long signTime = byteBuf.readLong();
            boolean moreSlices = byteBuf.readBoolean();
            if (Math.abs(signTime - System.currentTimeMillis()) >= 60000L) {
                channel.close();
                return;
            }
            byte[] slice = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(slice);
            final ByteBuf decrypted = Unpooled.wrappedBuffer(CryptUtil.Base64Crypt.decrypt(slice));
            final byte[] encryptedSlice = new byte[decrypted.readInt()];
            decrypted.readBytes(encryptedSlice);
            final byte[] signed = new byte[decrypted.readInt()];
            decrypted.readBytes(signed);

            ByteBuffer buffer = ByteBuffer.allocate(encryptedSlice.length + 1);
            buffer.put(encryptedSlice);
            buffer.put((byte) (signTime % 256));
            final byte[] sliceToSign = CryptUtil.Sign.sign(buffer.array());
            if (!Arrays.equals(sliceToSign, signed)) {
                channel.close();
                return;
            }

            try {
                byte[] decryptedSlice = CryptUtil.RSA.decryptByPrivateKey(encryptedSlice);

                channelSlices.computeIfAbsent(channel, k -> new ArrayList<>()).add(decryptedSlice);

                if (!moreSlices) {
                    byte[] fullMessage = mergeSlices(channelSlices.get(channel));

                    channelSlices.remove(channel);

                    handleFullMessage(ctx, channel, fullMessage);
                }
            } catch (Throwable ex) {
                ctx.close();
            }
        }
    }

    private byte[] mergeSlices(List<byte[]> slices) {
        int totalLength = slices.stream().mapToInt(arr -> arr.length).sum();
        byte[] fullMessage = new byte[totalLength];

        int currentIndex = 0;
        for (byte[] slice : slices) {
            System.arraycopy(slice, 0, fullMessage, currentIndex, slice.length);
            currentIndex += slice.length;
        }
        return CryptUtil.Base64Crypt.decrypt(fullMessage);
    }

    private void handleFullMessage(ChannelHandlerContext ctx, Channel channel, byte[] decrypt) throws Throwable {
        final ByteBuf byteBuf = Unpooled.wrappedBuffer(decrypt);

        final int packetId = byteBuf.readInt();

        final Class<? extends Packet> packetClass = packetMap.get(packetId);
        if (packetClass == null) {
            channel.close();
            return;
        }
        final Packet packet = packetClass.getConstructor(Channel.class).newInstance(channel);
        packet.read(new PacketWrapper(byteBuf));
    }
}