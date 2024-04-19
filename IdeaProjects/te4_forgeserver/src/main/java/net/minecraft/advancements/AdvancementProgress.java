package net.minecraft.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class AdvancementProgress implements Comparable<AdvancementProgress> {
    private static final String[][] EMPTY = new String[][]{{}};
    private final Map<String, CriterionProgress> criteria = new HashMap<>(4);
    private String[][] requirements = EMPTY;

    public void update(Map<String, Criterion> criteriaIn, String[][] requirements) {


        Set<String> set = criteriaIn.keySet();
        Iterator<Entry<String, CriterionProgress>> iterator = this.criteria.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, CriterionProgress> entry = iterator.next();

            if (!set.contains(entry.getKey())) {
                iterator.remove();
            }
        }

        for (String s : set) {
            if (!this.criteria.containsKey(s)) {
                this.criteria.put(s, new CriterionProgress());
            }
        }
        this.requirements = requirements;
    }


    public boolean isDone() {
        if (this.requirements.length == 0) {
            return false;
        } else {
            for (String[] astring : this.requirements) {
                boolean flag = false;

                for (String s : astring) {
                    CriterionProgress criterionprogress = this.getCriterionProgress(s);

                    if (criterionprogress != null && criterionprogress.isObtained()) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    return false;
                }
            }
            return true;
        }
    }


    public boolean hasProgress() {
        for (CriterionProgress criterionprogress : this.criteria.values()) {
            if (criterionprogress.isObtained()) {
                return true;
            }
        }
        return false;
    }


    public boolean grantCriterion(String criterionIn) {
        CriterionProgress criterionprogress = this.criteria.get(criterionIn);
        if (criterionprogress != null && !criterionprogress.isObtained()) {
            criterionprogress.obtain();
            return true;
        } else {
            return false;
        }
    }


    public boolean revokeCriterion(String criterionIn) {
        CriterionProgress criterionprogress = this.criteria.get(criterionIn);
        if (criterionprogress != null && criterionprogress.isObtained()) {
            criterionprogress.reset();
            return true;
        } else {
            return false;
        }
    }


    public String toString() {
        return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
    }


    public void serializeToNetwork(PacketBuffer buffer) {
        buffer.writeVarInt(this.criteria.size());

        for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            buffer.writeString(entry.getKey());
            entry.getValue().write(buffer);
        }
    }


    public static AdvancementProgress fromNetwork(PacketBuffer buffer) {
        AdvancementProgress advancementprogress = new AdvancementProgress();
        int i = buffer.readVarInt();

        for (int j = 0; j < i; ++j) {
            advancementprogress.criteria.put(buffer.readString(32767), CriterionProgress.read(buffer, advancementprogress));
        }
        return advancementprogress;
    }


    @Nullable
    public CriterionProgress getCriterionProgress(String criterionIn) {
        return this.criteria.get(criterionIn);
    }


    @SideOnly(Side.CLIENT)
    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0F;
        } else {
            float f = (float)this.requirements.length;
            float f1 = (float)this.countCompletedRequirements();
            return f1 / f;
        }
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public String getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        } else {
            int i = this.requirements.length;

            if (i <= 1) {
                return null;
            } else {
                int j = this.countCompletedRequirements();
                return j + "/" + i;
            }
        }
    }


    @SideOnly(Side.CLIENT)
    private int countCompletedRequirements() {
        int i = 0;
        for (String[] astring : this.requirements) {
            boolean flag = false;
            for (String s : astring) {
                CriterionProgress criterionprogress = this.getCriterionProgress(s);

                if (criterionprogress != null && criterionprogress.isObtained()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                ++i;
            }
        }
        return i;
    }


    public Iterable<String> getRemaningCriteria() {
        ArrayList<String> list = new ArrayList<>();
        for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (!entry.getValue().isObtained()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }


    public Iterable<String> getCompletedCriteria() {
        ArrayList<String> list = new ArrayList<>();
        for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (entry.getValue().isObtained()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }


    @Nullable
    @Deprecated
    public Date getFirstProgressDate() {
        long result = 0;
        for (CriterionProgress progress : this.criteria.values()) {
            long temp = progress.getObtainedL();
            if(temp>result)result = temp;
        }
        return result == 0?null:new Date(result);
    }


    @Nullable
    public long getFirstProgressDateL() {
        long result = 0;
        for (CriterionProgress progress : this.criteria.values()) {
            long temp = progress.getObtainedL();
            if(temp>result)result = temp;
        }
        return result;
    }


    @Override
    public int compareTo(AdvancementProgress progress) {
        long thisTime = this.getFirstProgressDateL();
        long anotherTime = progress.getFirstProgressDateL();
        return thisTime<anotherTime ? -1 : (thisTime==anotherTime ? 0 : 1);
    }


    public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
        @Override
        public JsonElement serialize(AdvancementProgress progress, Type type, JsonSerializationContext ctx) {
            JsonObject jsonobject = new JsonObject();
            JsonObject jsonobject1 = new JsonObject();

            for (Entry<String, CriterionProgress> entry : progress.criteria.entrySet()) {
                CriterionProgress criterionprogress = entry.getValue();
                if (criterionprogress.isObtained()) {
                    jsonobject1.add(entry.getKey(), criterionprogress.serialize());
                }
            }
            if (!jsonobject1.entrySet().isEmpty()) {
                jsonobject.add("criteria", jsonobject1);
            }

            jsonobject.addProperty("done", progress.isDone());
            return jsonobject;
        }


        @Override
        public AdvancementProgress deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject jsonobject = JsonUtils.getJsonObject(element, "advancement");
            JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "criteria", new JsonObject());
            AdvancementProgress advancementprogress = new AdvancementProgress();

            for (Entry<String, JsonElement> entry : jsonobject1.entrySet()) {
                String s = entry.getKey();
                advancementprogress.criteria.put(s, CriterionProgress.fromDateTime(advancementprogress, JsonUtils.getString(entry.getValue(), s)));
            }

            return advancementprogress;
        }
    }
}