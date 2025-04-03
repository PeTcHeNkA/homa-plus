package plus.elch.utl;


public class CfgUtils {
    public static long timeOf(String time) {
        String[] parts = time.split(":");
        return Long.parseLong(parts[0]) * 60 * 60 * 1000 + Long.parseLong(parts[1]) * 60 * 1000 + Long.parseLong(parts[2]) * 1000;
    }
}
