package zxc.mrdrag0nxyt;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextAlignerPlaceholder extends PlaceholderExpansion {
    private static final String SPACE = " ";

    @Override
    public @NotNull String getIdentifier() {
        return "spacer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MrDrag0nXYT (drakoshaslv)";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }


    /*
     * %spacer_center;<length>;<Text with {placeholder}>%
     * %spacer_right;<length>;<Text with {placeholder}>%
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String paramsString) {
        String[] params = paramsString.split(";", 3);
        if (params.length < 3) return null;

        return switch (params[0]) {
            case "center" -> handleTextAlign(player, params, SpaceStyle.CENTER);
            case "right" -> handleTextAlign(player, params, SpaceStyle.RIGHT);
            default -> null;
        };

    }


    private @Nullable String handleTextAlign(OfflinePlayer player, String @NotNull [] params, SpaceStyle spaceStyle) {
        try {
            int maxLength = Integer.parseInt(params[1]);
            String parsed = PlaceholderAPI.setBracketPlaceholders(player, params[2]);

            int diff = maxLength - parsed.length();
            if (diff <= 0) return parsed;

            int spaces = switch (spaceStyle) {
                case CENTER -> diff / 2;
                case RIGHT -> diff;
            };

            return SPACE.repeat(spaces) + parsed;

        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private enum SpaceStyle {
        CENTER, RIGHT
    }
}