import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Shows that "Authorization: Basic ..." is NOT encryption - anyone can decode it.
 *
 * Run:  java Base64Peek                      (uses a sample header)
 *       java Base64Peek "Basic dGVzdDoxMjM="  (decode your own header)
 *       java Base64Peek encode login:password
 */
public class Base64Peek {

    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("encode")) {
            String encoded = Base64.getEncoder().encodeToString(args[1].getBytes(StandardCharsets.UTF_8));
            System.out.println("Authorization: Basic " + encoded);
            return;
        }

        String header = args.length > 0 ? args[0] : "Basic c3R1ZGVudDpzaWVjMTIz";
        String base64 = header.startsWith("Basic ") ? header.substring("Basic ".length()) : header;

        byte[] raw = Base64.getDecoder().decode(base64.trim());
        String text = new String(raw, StandardCharsets.UTF_8);   // "login:password"

        int colon = text.indexOf(':');
        System.out.println("Base64   : " + base64);
        System.out.println("Decoded  : " + text);
        if (colon >= 0) {
            System.out.println("Login    : " + text.substring(0, colon));
            System.out.println("Password : " + text.substring(colon + 1));
        }
    }
}
