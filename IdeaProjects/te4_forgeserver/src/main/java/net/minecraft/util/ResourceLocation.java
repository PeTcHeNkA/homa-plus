package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;


public class ResourceLocation implements Comparable<ResourceLocation> {
    protected final String namespace;
    protected final String path;

    protected ResourceLocation(int unused, String... args) {
        this.namespace = StringUtils.isEmpty(args[0]) ? "minecraft" : args[0].toLowerCase(Locale.ROOT);
        this.path = args[1].toLowerCase(Locale.ROOT);
        Validate.notNull(this.path);
    }


    public ResourceLocation(String path) {
        path = path.toLowerCase();
        String rNms = "minecraft", rPath;

        int index = path.indexOf(58);
        if (index >= 0) {
            rPath = path.substring(index + 1, path.length());
            if (index > 1) {
                rNms = path.substring(0, index);
                if(rNms.equals("minecraft"))rNms = "minecraft";
            }
        } else {
            rPath = path;
        }
        this.namespace = rNms;
        this.path = rPath;
    }


    public ResourceLocation(String nms, String path) {
        this.namespace = StringUtils.isEmpty(nms) ? "minecraft" : nms.toLowerCase(Locale.ROOT);
        this.path = path.toLowerCase(Locale.ROOT);
        Validate.notNull(this.path);
    }


    public static String[] splitObjectName(String path) {
        String[] result = new String[]{"minecraft", path};
        int index = path.indexOf(58);
        if (index >= 0) {
            result[1] = path.substring(index + 1, path.length());
            if (index > 1) {
                result[0] = path.substring(0, index);
            }
        }
        return result;
    }


    public String getPath() {
        return this.path;
    }


    public String getNamespace() {
        return this.namespace;
    }


    @Override
    public String toString() {
        return this.namespace + ':' + this.path;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ResourceLocation)) {
            return false;
        } else {
            ResourceLocation loc = (ResourceLocation)obj;
            return this.namespace.equals(loc.namespace) && this.path.equals(loc.path);
        }
    }


    @Override
    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }


    @Override
    public int compareTo(ResourceLocation loc) {
        int num = this.namespace.compareTo(loc.namespace);
        if (num == 0) return this.path.compareTo(loc.path);
        return num;
    }


    public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
        public Serializer() {}

        public ResourceLocation deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            return new ResourceLocation(JsonUtils.getString(p_deserialize_1_, "location"));
        }

        public JsonElement serialize(ResourceLocation p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
            return new JsonPrimitive(p_serialize_1_.toString());
        }
    }
}