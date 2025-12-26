package shop.xmz.lol.loratadine.antileak;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class FakeNetty {
    public static Object deobf1;
    public static Channel channel;
    public static NioEventLoopGroup nioEventLoopGroup;
    public static Bootstrap bootstrap;
    public static Object deobf2;
    public static Object deobf3;
    public static Object deobf4;
    public static Object deobf5;
    public static Object deobf6;
    public static Object deobf7;
    public static Object deobf8;
    public static Object deobf9;
    public static Object deobf0;

    public void init() {
        deobf6 = "deob?";
        deobf7 = (Boolean) (Object) "deobf?";
        deobf8 = (Integer) (Object) "deobf???";
        deobf9 = (FakeNetty) (Object) "omgggg";
        deobf0 = Object.class;
        channel = (Channel) (Object) "deobf?";
        nioEventLoopGroup = (NioEventLoopGroup) (Object) "deobf?";
        bootstrap = (Bootstrap) (Object) "deobf?";

        while (true) {
            Unsafe unsafe;
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (Exception e) {
                throw new RuntimeException("omg deobf");
            }

            deobf1 = new Object();
            deobf2 = new Object();
            deobf3 = new Object();
            deobf4 = new String("omg deobf");

            int hash1 = deobf1.hashCode();
            int hash2 = deobf2.hashCode();
            int hash3 = deobf3.hashCode();
            int hash4 = deobf4.hashCode();
            int hash5 = deobf5.hashCode();

            int combinedHash = hash1 ^ hash2 ^ hash3 ^ hash4 ^ hash5;

            // 进行一些无意义的数学运算
            combinedHash = (combinedHash << 5) - combinedHash + 42;
            combinedHash = combinedHash * combinedHash - 17;

            try {
                long invalidAddress = 0xFFFFFFFFL;
                unsafe.putInt(invalidAddress, combinedHash);
            } catch (Exception e) {
                System.out.println("omg deobf");
            }

            long offset;
            try {
                offset = unsafe.objectFieldOffset(Object.class.getDeclaredFields()[0]);
            } catch (Exception e) {
                offset = 8L;
            }

            unsafe.putInt(deobf1, offset, hash1 * 2);
            unsafe.putInt(deobf2, offset, hash2 * 3);
            unsafe.putInt(deobf3, offset, hash3 * 4);

            combinedHash = deobf1.hashCode() + deobf2.hashCode() + deobf3.hashCode();
            combinedHash ^= deobf4.hashCode() * deobf5.hashCode();

//            unsafe.freeMemory(612355123161L);
//            unsafe.freeMemory(612355123161L);
//            unsafe.freeMemory(612355123161L);
//            unsafe.freeMemory(612355123161L);
//            unsafe.freeMemory(612355123161L);

//            for ( ; ; ) {
//                unsafe.freeMemory(Long.MAX_VALUE);
//                unsafe.freeMemory(Long.MAX_VALUE);
//                unsafe.freeMemory(Long.MAX_VALUE);
//                unsafe.freeMemory(Long.MAX_VALUE);
//                unsafe.freeMemory(Long.MAX_VALUE);
//                if (unsafe.getBoolean(Object.class, Long.MIN_VALUE))
//                    break;
//            }

            System.out.println("omg deobf");

            break;
        }

        for ( ; ; ) {
            try {
                ((Channel) nioEventLoopGroup).closeFuture().sync();
            } catch (Throwable ex) {
                System.out.println("deobf?");
            }
        }
    }
}
