package shop.xmz.lol.loratadine.antileak;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class OoOoOoOoooOoo {
    public static Object a;
    public static Object b;
    public static Object c;
    public static Object d;

    public OoOoOoOoooOoo() throws Exception {
        Class<?> base64Class = Class.forName("java.util.Base64");

        Class<?> decoderClass = Class.forName("java.util.Base64$Decoder");

        Method getDecoderMethod = base64Class.getMethod("getDecoder");

        Object decoder = getDecoderMethod.invoke(null);

        Method decodeMethod = decoderClass.getMethod("decode", byte[].class);

        Class<?> stringClass = Class.forName("java.lang.String");

        Constructor<?> stringConstructor = stringClass.getConstructor(byte[].class);

        Object e = "LS1hc3NldEluZGV4"; //--assetIndex
        Object f = "LS1mbWwuZm9yZ2VWZXJzaW9u"; //--fml.forgeVersion
        Object g = "LS1mbWwubWNWZXJzaW9u";  //--fml.mcVersion
        Object h = "LS1mbWw="; //--fml

        byte[] decodedE = (byte[]) decodeMethod.invoke(decoder, ((String) e).getBytes());
        byte[] decodedF = (byte[]) decodeMethod.invoke(decoder, ((String) f).getBytes());
        byte[] decodedG = (byte[]) decodeMethod.invoke(decoder, ((String) g).getBytes());
        byte[] decodedH = (byte[]) decodeMethod.invoke(decoder, ((String) h).getBytes());

        a = stringConstructor.newInstance((Object) decodedE);
        b = stringConstructor.newInstance((Object) decodedF);
        c = stringConstructor.newInstance((Object) decodedG);
        d = stringConstructor.newInstance((Object) decodedH);
    }
}