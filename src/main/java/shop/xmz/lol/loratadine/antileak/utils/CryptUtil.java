package shop.xmz.lol.loratadine.antileak.utils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class CryptUtil {
    public static class Base64Crypt {
        public static byte[] encryptToByteArray(String message) {
            return Base64.getEncoder().encode(message.getBytes(StandardCharsets.UTF_8));
        }

        public static byte[] encrypt(byte[] message) {
            return Base64.getEncoder().encode(message);
        }

        public static byte[] decrypt(byte[] message) {
            return Base64.getDecoder().decode(message);
        }

        public static String decryptByByteArray(byte[] bytes) {
            return new String(Base64.getDecoder().decode(bytes), StandardCharsets.UTF_8);
        }

        public static String decrypt(String message) {
            return new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
        }

        public static String encrypt(String message) {
            return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static class SHA256 {
        private final static MessageDigest digest;

        static {
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public static byte[] encrypt(byte[] message) {
            try {
                return digest.digest(message);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return new byte[]{1, 9, 8, 9, 0, 6, 0, 4};
        }
    }

    public static class Sign {
        private static final byte[] OFFSET = {1, 9, 8, 9, 0, 6, 0, 4};
        private static final int MAX_OFFSET_INDEX = OFFSET.length - 1;

        public static byte[] sign(byte[] message) {
            byte[] encrypted = SHA256.encrypt(message);

            int base = message.length + 32;
            if (base <= 0) base = 1; // 防止除以零
            int hash = Math.abs(Arrays.hashCode(message)) % base % 9;

            for (int cycle = 0; cycle <= hash; cycle++) {
                int safeIndex = Math.min(MAX_OFFSET_INDEX, Math.abs((cycle + 23) / 4));
                byte offset = OFFSET[safeIndex];

                for (int j = 0; j < encrypted.length; j++) {
                    int temp = (encrypted[j] & 0xFF) + (offset * cycle);
                    encrypted[j] = (byte) (temp % 256);
                }
            }

            return encrypted;
        }
    }

    public static class RSA {
        private static final String RSA_KEY_ALGORITHM = "RSA";
        private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
        public static final int MAX_LENGTH = 245;

        private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt3KksuPiX5FuJb9R9SK+zm2XO0J5pqlljYzOO477HIjWW0PCO8Q7RxCcZ0j1/YFzXl5OQS5ZgRN7MycKbVsA+V0FFWnVh0qqVeHhp3KZctDW/U/XwRpc/zrmt+ADQvEzPXqBpGKwHw04rjaf8KDbfUF1EVMp3EMb8i7feBBN+8E4GTpDtqk4Ombl5QUsqz9RCk2GYlhUhU6nbO6JzVNzvGTXjgU18+Q6H2mptQQmyMt/BKG8hZhZMJSE/YxvSnQUjP1nIObWsg2KlOKj4P3HfeXi3Y2aGmznfOK0IDCMkTbxYQqgdtiHEC6pBAQz3MQlny4eV1jHiPHHTsGbJSyeTQIDAQAB";
        private static final PublicKey PUBLIC_KEY;

        private static final String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCCV+jqTaMMALJs9qlfyj9EQZEMLEWazr3fV77DpExvoaqU6M3JbpVBrbIaw5NlwQMcQSKla9RZEH4g9quHm3Zb/WK02cw5QW9yWJVg7DeGSlg5CyvMUvewOgnqlBRPre3iPb4auzHoJO2RipnlzIKABsfaiX8iOYweB+CVXkFGUXG5FggIEbkAJ6olbYLVlCnANALYto8QUuF8+5LyJjbtcSqtbA3AFLxuWCAROEOi+hfH5/oYUYitbkmlMc4fJ1THqzNDN1k5T6asHwec29fkeou7YKRSLimGuKwRjoOb2v18FVx1syoZ1qjZi8dvl9Xqx9b71wuHV3l3+RdqO8L5AgMBAAECggEAKm8kTONpqYawj/jdu2nEnxwZdLUVFAkql4Ohf2AF4nHnbwM7u9Bclz1NNplla0MjGBe8h85LG5Pa/DBSN5vNNLWRZP9jFWDQP5HHT/6XNQkPH8MbRzHboWvDvrLmyen+ACHJujonTUR4c2GEKOQjuDp85hDNav8BNuWpn/dHmPSBHgf+/gUQD6UaVWRr/Y+8gpj7v+RZ0vB8tuKs15dWQepFndPprvyjg8RhTMXcuamWlXklOYnoHio2a/lI+IR6FeFAVXJ6pHTR45j5DTT5C5t3uI9eHlOMaOcgzDY5SbbvHxwA/OtqDpheo1QSslkPQlFgo8kNq9LFZce+YTy0YQKBgQDjUbyAWOhU0Cjxrf6+Gv0C7PxIOjiwhY127NTSYGCegY4S4qC22nIH74oXcRvWczlPGbj0natJZuGq/ZoBgYG6WJkQ8vHKGs+XyienNtvqDbiomSlIMe80bLecNcqu37nSSM02PI0x+Q3X+tDRHxaf7av8UGs0AWKFNTyu9+cdIwKBgQCSyeqB8uFsYs6Cx4Bp5oIa/ZQW2mMRbJpq9F7DtbebYc1PLRFSEBUXvfdX/mGdqoq9XXYFKK+iVZdpTp2YYG/qK+9oZoHWpJoXOodmFWS8yDWDSg/stijAWcsxOOKTwHJEwbDGaoeBXeZRQfMK+Ije4xaipYuxQvOxD/4cH9cHMwKBgQC8E3Fhw62eFof5xdrh0RSK6ialX75tlmIABlzjHqhyHIC+8VDFWSFFnZPkZ0n/+V9uKbsUcKs6VOvbfG2CV3NNCWbDVi5k6B/f13tNZx82nFmu7OHyAJaICnczwHHMlAB7ko5vFFRCB2zDyJoim5Uthwhn/uVjps2rputpXGSUfwKBgG2inCHk+5ONlPq+8V2niiOIpHQRNw9Lk2YxERqR/gnzGXp6icZGNsrd2wEBAX3WY/ud88lUoyHXVdiUnEa0OMKpgA31CiL8HA8fawPHFM+fpcBir9Q4FeXc61PfTfPXOEG8fUElTgJE1QJ6BIJ8MRZwfHaMuPJztDnaXQw6h8tbAoGAHfBdcCSmlBlUvfn91u6mCkM+qnvA7ZrTXvFS+o+H6/p0r8BIdBaHu/E153lYeJ4uZLAWyrkPJY63Cau3Tdz7OuU7lPjJ9kW80ciqDgRc4XqGD5/bD9VDT+C5fcWgAB3heWeu+XxtzwSQSjLl1c3ckCCfYAYkZrDwX02iGe6c0lc=";
        private static final PrivateKey PRIVATE_KEY;

        static {
            try {
                final byte[] pubKey = Base64Crypt.decrypt(publicKey.getBytes(StandardCharsets.UTF_8));
                final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
                KeyFactory keyFactoryPub = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
                PUBLIC_KEY = keyFactoryPub.generatePublic(x509KeySpec);

                final byte[] priKey = Base64Crypt.decrypt(privateKey.getBytes(StandardCharsets.UTF_8));
                final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
                KeyFactory keyFactoryPri = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
                PRIVATE_KEY = keyFactoryPri.generatePrivate(pkcs8KeySpec);
            } catch (Throwable e) {
                throw new RuntimeException("初始化通讯密钥失败，请联系开发者");
            }
        }

        public static byte[] encryptByPublicKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
                cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
                return Base64Crypt.encrypt(cipher.doFinal(data));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return new byte[]{};
        }

        public static byte[] decryptByPrivateKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
                return cipher.doFinal(Base64Crypt.decrypt(data));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return new byte[]{};
        }
    }
}
