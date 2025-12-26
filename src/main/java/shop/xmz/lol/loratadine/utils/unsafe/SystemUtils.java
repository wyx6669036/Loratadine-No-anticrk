package shop.xmz.lol.loratadine.utils.unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemUtils {
    public static boolean isServiceExist(String serviceName) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /SVC");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains(serviceName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error executing command.");
        }
        return false;
    }
}
