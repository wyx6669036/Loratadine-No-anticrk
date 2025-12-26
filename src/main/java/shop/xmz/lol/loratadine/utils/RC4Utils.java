package shop.xmz.lol.loratadine.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * RC4 encryption and decryption utility class.
 * RC4 is a symmetric stream cipher that uses the same algorithm and key for both encryption and decryption.
 */
public class RC4Utils {

    /**
     * Encrypts a string using RC4 algorithm and encodes the result as Base64
     * 
     * @param data The plain text to encrypt
     * @param key The encryption key
     * @return Base64 encoded encrypted string
     */
    public static String encrypt(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = crypt(dataBytes, keyBytes);
        
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    /**
     * Decrypts a Base64 encoded RC4 encrypted string
     * 
     * @param encryptedBase64 Base64 encoded encrypted string
     * @param key The encryption key (must be the same as used for encryption)
     * @return The decrypted plain text
     */
    public static String decrypt(String encryptedBase64, String key) {
        if (encryptedBase64 == null || key == null) {
            return null;
        }
        
        byte[] dataBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = crypt(dataBytes, keyBytes);
        
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * The core RC4 algorithm implementation.
     * RC4 is a symmetric cipher - the same function is used for both encryption and decryption.
     * 
     * @param data The data to encrypt/decrypt
     * @param key The encryption/decryption key
     * @return The encrypted/decrypted data
     */
    private static byte[] crypt(byte[] data, byte[] key) {
        // Initialize S-box
        int[] sBox = initSBox(key);
        
        // Generate keystream and XOR with data
        byte[] result = new byte[data.length];
        int i = 0, j = 0;
        
        for (int k = 0; k < data.length; k++) {
            i = (i + 1) & 0xFF;
            j = (j + sBox[i]) & 0xFF;
            
            // Swap S[i] and S[j]
            int temp = sBox[i];
            sBox[i] = sBox[j];
            sBox[j] = temp;
            
            // Generate keystream byte and XOR with data
            int keyStreamByte = sBox[(sBox[i] + sBox[j]) & 0xFF];
            result[k] = (byte) (data[k] ^ keyStreamByte);
        }
        
        return result;
    }
    
    /**
     * Initialize the S-box using the key
     * 
     * @param key The encryption key
     * @return The initialized S-box
     */
    private static int[] initSBox(byte[] key) {
        int[] sBox = new int[256];
        int[] keyBytes = new int[256];
        
        // Initialize S and K arrays
        for (int i = 0; i < 256; i++) {
            sBox[i] = i;
            keyBytes[i] = key[i % key.length] & 0xFF;
        }
        
        // Initial permutation of S
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + sBox[i] + keyBytes[i]) & 0xFF;
            
            // Swap S[i] and S[j]
            int temp = sBox[i];
            sBox[i] = sBox[j];
            sBox[j] = temp;
        }
        
        return sBox;
    }
}