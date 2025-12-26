package shop.xmz.lol.loratadine.antileak.utils;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.user.UserData;
import shop.xmz.lol.loratadine.antileak.user.UserLimits;
import shop.xmz.lol.loratadine.antileak.user.UserTime;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketWrapper {
    private final ByteBuf byteBuf;

    public PacketWrapper(final ByteBuf buf) {
        this.byteBuf = buf;
    }

    public byte[] toByteArray() {
        final byte[] allData = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(allData);
        return allData;
    }

    public void writeBytes(byte[] bytes) {
        if (bytes == null) {
            this.writeInt(-1);
            return;
        }

        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    public byte[] readBytes() {
        int length = this.readInt();
        if (length <= 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public void writeInt(int i) {
        byteBuf.writeInt(i);
    }

    public int readInt() {
        return byteBuf.readInt();
    }

    public void writeBoolean(boolean flag) {
        byteBuf.writeBoolean(flag);
    }

    public boolean readBoolean() {
        return byteBuf.readBoolean();
    }

    public void writeUser(UserData user) {
        if (user != null) {
            this.writeString(user.index);
            this.writeString(user.username);
            this.writeString(user.uuid);
            this.writeString(user.userLimits.toObject().toString());
            this.writeString(user.time.toObject().toString());
            this.writeBoolean(user.isFree);
            this.writeBoolean(user.isBeta);
        }
    }

    public void writeUserOthers(UserData user) {
        if (user != null) {
            this.writeString(user.index);
            this.writeString(user.username);
            this.writeString(user.playerName);
            this.writeString(user.userLimits.tag);
            this.writeString(user.clientType);
            this.writeBoolean(user.isFree);
            this.writeBoolean(user.isBeta);
        }
    }

    public UserData readUser() {
        final UserData user = new UserData();

        user.index = this.readString();
        user.username = this.readString();
        user.uuid = this.readString();
        user.userLimits = UserLimits.fromObject(this.readString());
        user.time = UserTime.fromObject(this.readString());
        user.isFree = this.readBoolean();
        user.isBeta = this.readBoolean();

        return user;
    }

    public UserData readUserOthers() {
        final UserData user = new UserData();

        user.index = this.readString();
        user.username = this.readString();
        user.playerName = this.readString();
        user.userLimits = new UserLimits();
        user.userLimits.tag = this.readString();
        user.clientType = this.readString();
        user.isFree = this.readBoolean();
        user.isBeta = this.readBoolean();

        return user;
    }

    public void writeString(String str) {
        if (str == null) {
            this.writeInt(-1);
        } else {
            final byte[] wrapped = CryptUtil.Base64Crypt.encryptToByteArray(str);
            byteBuf.writeInt(wrapped.length);
            byteBuf.writeBytes(wrapped);
        }
    }

    public String readString() {
        final int stringLength = byteBuf.readInt();
        if (stringLength == -1) {
            return null;
        } else {
            final byte[] strBytes = new byte[stringLength];
            byteBuf.readBytes(strBytes);
            return CryptUtil.Base64Crypt.decryptByByteArray(strBytes);
        }
    }

    public void writeLong(long i) {
        byteBuf.writeLong(i);
    }

    public long readLong() {
        return byteBuf.readLong();
    }

    private static ByteBuf getBuf(Packet packet) throws Throwable {
        // 序列化 Packet 数据
        final PacketWrapper wrapper = new PacketWrapper(Unpooled.buffer());
        wrapper.writeInt(packet.packetId());
        packet.write(wrapper);
        final byte[] allData = wrapper.toByteArray();

        // 对数据进行 Base64 编码
        byte[] base64Bytes = CryptUtil.Base64Crypt.encrypt(allData);

        // 构造 ByteBuf
        final ByteBuf fullData = Unpooled.buffer();

        getSlices(base64Bytes, fullData);

        return fullData;
    }

    private static void getSlices(byte[] base64Bytes, final ByteBuf fullData) {
        int offset = 0;

        while (offset < base64Bytes.length) {
            int sliceLength = Math.min(CryptUtil.RSA.MAX_LENGTH, base64Bytes.length - offset);
            byte[] slice = new byte[sliceLength];
            System.arraycopy(base64Bytes, offset, slice, 0, sliceLength);
            offset += sliceLength;

            byte[] encryptedSlice = CryptUtil.RSA.encryptByPublicKey(slice);
            final long thisTime = System.currentTimeMillis();
            ByteBuffer buffer = ByteBuffer.allocate(encryptedSlice.length + 1);
            buffer.put(encryptedSlice);
            buffer.put((byte) (thisTime % 256));
            final byte[] signed = CryptUtil.Sign.sign(buffer.array());
            final ByteBuf buf = Unpooled.buffer();
            buf.writeInt(encryptedSlice.length);
            buf.writeBytes(encryptedSlice);
            buf.writeInt(signed.length);
            buf.writeBytes(signed);
            final byte[] shouldSend = CryptUtil.Base64Crypt.encrypt(buf.array());

            fullData.writeInt(shouldSend.length + 9); // 写入长度
            fullData.writeLong(thisTime);
            fullData.writeBoolean(offset < base64Bytes.length); // 标记是否有更多切片
            fullData.writeBytes(shouldSend); // 写入加密数据

//            System.out.println(thisTime);
//            System.out.println(offset < base64Bytes.length);
//            System.out.println(Arrays.toString(buf.array()));
        }
    }

    public static void release(Packet packet) throws Throwable {
        // 发送数据到指定通道
        packet.channel.writeAndFlush(getBuf(packet));
    }
}
