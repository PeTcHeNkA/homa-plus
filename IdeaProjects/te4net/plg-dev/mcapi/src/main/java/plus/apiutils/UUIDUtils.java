package plus.apiutils;

import java.util.UUID;
import java.util.regex.Pattern;


public class UUIDUtils {
    private static final Pattern uuidFrame = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    public static String fromUUID(UUID value) {
        return value.toString().replace("-", "");
    }

    public static UUID fromString(String input) {
        return UUID.fromString(uuidFrame.matcher(input).replaceFirst("$1-$2-$3-$4-$5"));
    }
}