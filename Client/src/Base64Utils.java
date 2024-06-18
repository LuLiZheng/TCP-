import java.util.Base64;

public class Base64Utils {
    // Base64编码
    public static String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    // Base64解码
    public static String decode(String data) {
        return new String(Base64.getDecoder().decode(data));
    }
}