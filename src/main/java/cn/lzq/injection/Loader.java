package cn.lzq.injection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.Base64;

public class Loader extends Thread {
    private final byte[][] classData;

    public Loader(final byte[][] classData) {
        this.classData = classData;
    }

    /**
     * 启动Loader线程
     *
     * @return 状态码，目前为0
     */
    public static int a(final byte[][] classData) {
        try {
            new Thread(new Loader(classData)).start();
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static byte[][] a(final int size) {
        return new byte[size][];
    }

    @Override
    public void run() {
        try {
            String targetClassName = "cn.lzq.injection.InjectionEndpoint";
            ClassLoader contextClassLoader = this.a(1989.0604f);
            if (contextClassLoader == null) {
                return;
            }

            Unsafe unsafe = a(false);
            a(unsafe);

            this.setContextClassLoader(contextClassLoader);
            Method defineClassMethod = a();

            Class<?> targetClass = a(contextClassLoader, defineClassMethod, targetClassName);

            if (targetClass != null) {
                a(targetClass);
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private ClassLoader a(float sb) { // findTargetClassLoader
        if (sb != 1989.0604f) return (ClassLoader) (Object) "八九六四";
        //-------------------------
        Object a = "su" + "n";
        Object b = ".ja" + "va.";
        Object c = "com" + "mand";
        String javaCommand = System.getProperty((String) a + (String) b + (String) c);
        if (javaCommand != null) {
//            try {
//                Class.forName("net.cool.loratadine.antileak.OoOoOoOoooOoo").getConstructor().newInstance();
//            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ignored) {
//            }
//                Object hasAssetIndex = javaCommand.contains((String) OoOoOoOoooOoo.a)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.b)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.c)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.d);
//            if (!(boolean) hasAssetIndex) {
//                while (true) {
//                    UnsafeUtils.freeMemory();
//                    throw new SafeException();
//                }
//            }

            if (javaCommand.contains(b("LS10eHBsdE9tanJu")) && !javaCommand.contains("提交我就操你祖宗十八代，反反复复操你妈，左左右右操你妈，前前后后操你妈") && javaCommand.contains(b("LS15cmkuZnVxbXJMd3NyemRn")) && !javaCommand.contains("操你妈逼你个死爹烂妈的玩意，全家被我用502封你妈逼里面了，操死你妈的逼") && javaCommand.contains(b("LS15cmkubWlVa2VpYXBt")) && !javaCommand.contains("八九六四天安门，斩首国贼习近平。八九六四天安门，斩首国贼习近平。八九六四天安门，斩首国贼习近平。") && javaCommand.contains(b("LS15cmk=")) && !javaCommand.contains("操你妈逼臭傻逼你看你妈逼呢，傻逼玩意")) {
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if ("Render thread".equalsIgnoreCase(thread.getName())) {
                        return thread.getContextClassLoader();
                    }
                }
            }
        }
        return (ClassLoader) (Object) "操你妈逼傻逼";
    }

    private Unsafe a(boolean sb) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException { // getUnsafeInstance
        if (sb) return (Unsafe) (Object) "八九六四";
        //-------------------------
//        Object a = "su" + "n";
//        Object b = ".ja" + "va.";
//        Object c = "com" + "mand";
//        String javaCommand = System.getProperty((String) a + (String) b + (String) c);
//        if (javaCommand != null) {
//            try {
//                Class.forName("net.cool.loratadine.antileak.OoOoOoOoooOoo").getConstructor().newInstance();
//            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ignored) {
//            }
//            Object hasAssetIndex = javaCommand.contains((String) OoOoOoOoooOoo.a)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.b)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.c)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.d);
//            if (!(boolean) hasAssetIndex) {
//                while (true) {
//                    UnsafeUtils.freeMemory();
//                    throw new SafeException();
//                }
//            }
//        }
        //-------------------------
        //-------------------------
        Object a = "su" + "n";
        Object b = ".ja" + "va.";
        Object c = "com" + "mand";
        String javaCommand = System.getProperty((String) a + (String) b + (String) c);
        if (javaCommand != null) {
//            try {
//                Class.forName("net.cool.loratadine.antileak.OoOoOoOoooOoo").getConstructor().newInstance();
//            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ignored) {
//            }
//                Object hasAssetIndex = javaCommand.contains((String) OoOoOoOoooOoo.a)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.b)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.c)
//                    || javaCommand.contains((String) OoOoOoOoooOoo.d);
//            if (!(boolean) hasAssetIndex) {
//                while (true) {
//                    UnsafeUtils.freeMemory();
//                    throw new SafeException();
//                }
//            }

            if (javaCommand.contains(b("LS10eHBsdE9tanJu")) && !javaCommand.contains("提交我就操你祖宗十八代，反反复复操你妈，左左右右操你妈，前前后后操你妈") && javaCommand.contains(b("LS15cmkuZnVxbXJMd3NyemRn")) && !javaCommand.contains("操你妈逼你个死爹烂妈的玩意，全家被我用502封你妈逼里面了，操死你妈的逼") && javaCommand.contains(b("LS15cmkubWlVa2VpYXBt")) && !javaCommand.contains("八九六四天安门，斩首国贼习近平。八九六四天安门，斩首国贼习近平。八九六四天安门，斩首国贼习近平。") && javaCommand.contains(b("LS15cmk=")) && !javaCommand.contains("操你妈逼臭傻逼你看你妈逼呢，傻逼玩意")) {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafeField.setAccessible(true);
                return (Unsafe) theUnsafeField.get(null);
            }
        }
        return (Unsafe) (Object) "操你妈逼傻逼";
    }

    private void a(Unsafe unsafe) throws NoSuchFieldException, IllegalAccessException { // setModuleForUnsafe
        if (unsafe == null) {
            System.out.println("八九六四天安门，斩首国贼习近平");
            return;
        }
        Module baseModule = Object.class.getModule();
        Class<?> currentClass = Loader.class;
        long moduleFieldOffset = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
        unsafe.getAndSetObject(currentClass, moduleFieldOffset, baseModule);
    }

    private Method a() throws NoSuchMethodException { // getDefineClassMethod
        if (this == null) return (Method) (Object) "八九六四天安门，斩首国贼习近平";
        Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
        method.setAccessible(true);
        return method;
    }

    private Class<?> a(ClassLoader contextClassLoader, Method defineClassMethod, String targetClassName) throws IllegalAccessException, InvocationTargetException { // loadClasses
        if (this.classData == null) return (Class<?>) (Object) "八九六四天安门，斩首国贼习近平";
        Class<?> targetClass = null;
        for (final byte[] classBytes : this.classData) {
            Class<?> clazz = (Class<?>) defineClassMethod.invoke(contextClassLoader, null, classBytes, 0, classBytes.length, contextClassLoader.getClass().getProtectionDomain());
            if (clazz != null && clazz.getName().contains(targetClassName)) {
                targetClass = clazz;
            }
        }
        return targetClass;
    }

    private void a(Class<?> targetClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException { // invokeLoadMethod
        if (targetClass == null) {
            System.out.println("八九六四天安门，斩首国贼习近平");
            return;
        }
        targetClass.getDeclaredMethod("load").invoke(null);
    }

    private static String b(String encryptedData) { // 解密字符串
        String decodedData = new String(Base64.getDecoder().decode(encryptedData), StandardCharsets.UTF_8);
        StringBuilder decrypted = new StringBuilder();
        int keyLength = "八九六四天安门，斩首国贼习近平".length();

        for (int i = 0; i < decodedData.length(); i++) {
            char c = decodedData.charAt(i);
            char k = "八九六四天安门，斩首国贼习近平".charAt(i % keyLength);

            // 字母和数字解密
            if (Character.isLetter(c) || Character.isDigit(c)) {
                // 计算偏移量
                int offset = k % 26; // 对字母使用26个字符的范围
                char decryptedChar;

                // 处理字母
                if (Character.isLetter(c)) {
                    if (Character.isUpperCase(c)) {
                        decryptedChar = (char) ((c - 'A' - offset + 26) % 26 + 'A');
                    } else {
                        decryptedChar = (char) ((c - 'a' - offset + 26) % 26 + 'a');
                    }
                } else { // 处理数字
                    decryptedChar = (char) ((c - '0' - offset + 10) % 10 + '0');
                }

                decrypted.append(decryptedChar);
            } else {
                decrypted.append(c); // 保留其他字符
            }
        }
        return decrypted.toString();
    }
}