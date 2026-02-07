package zxc.mrdrag0nxyt;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.tson.utl.uns.UnsafeUtils;


public final class TextAlignerPlaceholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "textaligner";
    }


    @Override
    public @NotNull String getAuthor() {
        return "MrDrag0nXYT, HomaPlus";
    }


    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }


    @Override
    public boolean persist() {
        return true;
    }


    /**
     * %textaligner_center;length;Text with {placeholder}%
     * <br>
     * %textaligner_right;length;Text with {placeholder}%
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String paramsString) {
        String[] params;
        if ((params = paramsString.split(";", 3)).length < 3) return null;

        return switch (params[0]) {
            case "center" -> handleTextAlign(player, params, true);
            case "right" -> handleTextAlign(player, params, false);
            default -> null;
        };
    }


    /**
     * @return указанную длину строки, либо длину самой строки
     * <br>
     * Позволяет использовать заглушки типа `#####` для указания длины.
     * <br>
     * Так же данный способ будет работать значительно быстрее, тк не выделяет память в куче + нет исключений
     */
    private static int getMaxLen(String str) {
        byte[] bytes;
        int sum = 0;
        for (byte b : bytes = UnsafeUtils.getBytes(str)) {
            if (b >= '0' && b <= '9')
                sum = sum * 10 + b - '0';
            else
                return bytes.length;
        }
        return bytes.length;
    }


    private static String handleTextAlign(OfflinePlayer player, String @NotNull [] params, boolean center) {
        String result = PlaceholderAPI.setBracketPlaceholders(player, params[2]);

        int spaces;
        if ((spaces = getMaxLen(params[1]) - result.length()) <= 0) return result;

        if(center) spaces >>= 1;

        byte[] bResultPre = UnsafeUtils.getBytes(result),
               bResult    = new byte[bResultPre.length + spaces];

        //заполняю пробелами
        for (int i = 0; i < bResultPre.length; i++)
            bResult[i] = ' ';

        //Переиспользую экземпляр строки из параметров
        //строку-результат PAPI использовать небезопасно, тк может быть константой
        result = params[0];

        //Собираю результирующую строку
        System.arraycopy(bResultPre, 0, bResult, spaces, bResultPre.length);
        UnsafeUtils.set(result, UnsafeUtils.STR_OFFSET, bResult);

        return result;
    }
}