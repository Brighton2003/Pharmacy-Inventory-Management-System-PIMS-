package pims;

import java.security.MessageDigest;

/** Holds the details of the user who is currently logged in, plus password hashing. */
public class Session {

    public static int userId;
    public static String username;
    public static String fullName;
    public static String role;

    public static void set(int id, String uname, String fname, String r) {
        userId = id;
        username = uname;
        fullName = fname;
        role = r;
    }

    public static void clear() {
        userId = 0;
        username = null;
        fullName = null;
        role = null;
    }

    /**
     * SHA-256 hash, returned as lower-case hex.
     * This matches MySQL's SHA2(password, 256) used in database.sql,
     * so passwords are never stored in plain text.
     */
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
