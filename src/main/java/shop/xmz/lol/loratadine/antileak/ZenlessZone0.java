package shop.xmz.lol.loratadine.antileak;

import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static shop.xmz.lol.loratadine.antileak.Fucker.getAllQQ;

public class ZenlessZone0 {
    public static void a(Object object) {

        List<String> a = new ArrayList<>();
        a.addAll(getAllQQ());
        if (a.indexOf("896433399") > -1) {
            try {
                Class.forName("com.heypixel.heypixel.Heypixel");
            } catch (ClassNotFoundException e) {
//                UnsafeUtils.freeMemory(1163911367127L);
//                UnsafeUtils.freeMemory(1163911367127L);
//                UnsafeUtils.freeMemory(1163911367127L);
//                UnsafeUtils.freeMemory(1163911367127L);
//                System.exit(0);
                throw new SafeException();
            }
        }
        Unsafe unsafe;

        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Failed to load NAL!!!");

//            System.exit(0);
            throw new SafeException();
        }

        if ("today".contains("g")) {

//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            System.exit(0);
            throw new SafeException();
        }

        if ("today.akarin.client.protocol.forge.IHandshakeState".equals("today.akarin.client.protocol.forge.HandshakeManager")) {

//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            System.exit(0);
            throw new SafeException();
        }

        if (object != null) {
            Object o = object;
            int h = o.hashCode();
        }

        List<String> vmArgs = null;
        try {
            ManagementFactory.getRuntimeMXBean().getInputArguments();

            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
//                Unsafe unsafe = getUnsafe();
            long fieldOffset = unsafe.objectFieldOffset(runtimeMXBean.getClass().getDeclaredField("jvm"));

            Object jvm = unsafe.getObject(runtimeMXBean, fieldOffset);

            fieldOffset = unsafe.objectFieldOffset(jvm.getClass().getDeclaredField("vmArgs"));

            vmArgs = new ArrayList<>();
            vmArgs.addAll((List<String>) unsafe.getObject(jvm, fieldOffset));

            unsafe.putObject(jvm, fieldOffset, vmArgs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        assert vmArgs != null;
        List<String> inputArguments = Collections.unmodifiableList(vmArgs);

        if (!ManagementFactory.getRuntimeMXBean().getInputArguments().equals(inputArguments)) {
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            unsafe.freeMemory(1163911367127L);
//            System.exit(0);
            throw new SafeException();
        }


        List<String> args = Arrays.asList("-agentpath", "-Xbootclasspath");
        for (String argument : inputArguments) {
            boolean matchFound = false;
            for (String s : args) {
                if (argument.indexOf(s) > -1) {
                    matchFound = true;
                    break;
                }
            }

            if (matchFound) {
//                unsafe.freeMemory(1163911367127L);
//                unsafe.freeMemory(1163911367127L);
//                unsafe.freeMemory(1163911367127L);
//                unsafe.freeMemory(1163911367127L);
//                System.exit(0);
                throw new SafeException();
            }
        }
    }
}
