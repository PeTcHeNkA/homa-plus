package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;


public class MinMaxBounds {
    public static final MinMaxBounds UNBOUNDED = new MinMaxBounds((Float)null, (Float)null);
    private final float min;
    private final float max;

    public MinMaxBounds(@Nullable Float min, @Nullable Float max) {
        this.min = min == null?Float.NaN:min;
        this.max = max == null?Float.NaN:max;
    }

    public boolean test(float num) {
        float min, max;
        if (!Float.isNaN((min = this.min)) && min > num) {
            return false;
        } else {
            return !Float.isNaN((max = this.max)) || !(max < num);
        }
    }


    public boolean testSquare(double num) {
        float min, max;
        if (!Float.isNaN(min = this.min) && (double)(min * min) > num) {
            return false;
        } else {
            return Float.isNaN(max = this.max) || !((double)(max * max) < num);
        }
    }


    public static MinMaxBounds deserialize(@Nullable JsonElement jEl) {
        if (jEl != null && !jEl.isJsonNull()) {
            if (JsonUtils.isNumber(jEl)) {
                float value = JsonUtils.getFloat(jEl, "value");
                return new MinMaxBounds(value, value);
            } else {
                JsonObject jObj = JsonUtils.getJsonObject(jEl, "value");
                Float min = jObj.has("min") ? JsonUtils.getFloat(jObj, "min") : null;
                Float max = jObj.has("max") ? JsonUtils.getFloat(jObj, "max") : null;
                return new MinMaxBounds(min, max);
            }
        } else {
            return UNBOUNDED;
        }
    }
}