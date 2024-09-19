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
import javax.annotation.Nullable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class AdvancementProgress extends HashMap<String, CriterionProgress> implements Comparable<AdvancementProgress> {
    private static final String[][] EMPTY = new String[][]{{}};
    private String[][] requirements = EMPTY;
    public AdvancementProgress(){
        super(3);
    }

    public void update(Map<String, Criterion> criteriaIn, String[][] requirements) {
        Set<String> set = criteriaIn.keySet();
        Iterator<Entry<String, CriterionProgress>> iterator = super.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, CriterionProgress> entry = iterator.next();

            if (!set.contains(entry.getKey())) {
                iterator.remove();
            }
        }

        for (String s : set) {
            if (!super.containsKey(s)) {
                super.put(s, new CriterionProgress());
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
        for (CriterionProgress criterionprogress : super.values()) {
            if (criterionprogress.isObtained()) {
                return true;
            }
        }
        return false;
    }


    public boolean grantCriterion(String criterionIn) {
        CriterionProgress criterionprogress = super.get(criterionIn);
        if (criterionprogress != null && !criterionprogress.isObtained()) {
            criterionprogress.obtain();
            return true;
        } else {
            return false;
        }
    }


    public boolean revokeCriterion(String criterionIn) {
        CriterionProgress criterionprogress = super.get(criterionIn);
        if (criterionprogress != null && criterionprogress.isObtained()) {
            criterionprogress.reset();
            return true;
        } else {
            return false;
        }
    }


    @Override
    public String toString() {
        return "AdvancementProgress{criteria=" + super.toString() + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
    }


    public void serializeToNetwork(PacketBuffer buffer) {
        buffer.writeVarInt(super.size());

        for (Entry<String, CriterionProgress> entry : super.entrySet()) {
            buffer.writeString(entry.getKey());
            entry.getValue().write(buffer);
        }
    }


    public static AdvancementProgress fromNetwork(PacketBuffer buffer) {
        AdvancementProgress advancementprogress = new AdvancementProgress();
        int i = buffer.readVarInt();

        for (int j = 0; j < i; ++j) {
            advancementprogress.put(buffer.readString(32767), CriterionProgress.read(buffer, advancementprogress));
        }
        return advancementprogress;
    }


    @Nullable
    public CriterionProgress getCriterionProgress(String criterionIn) {
        return super.get(criterionIn);
    }


    @SideOnly(Side.CLIENT)
    public float getPercent() {
        if (super.isEmpty()) {
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
        if (super.isEmpty()) {
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
        for (Entry<String, CriterionProgress> entry : super.entrySet()) {
            if (!entry.getValue().isObtained()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }


    public Iterable<String> getCompletedCriteria() {
        ArrayList<String> list = new ArrayList<>();
        for (Entry<String, CriterionProgress> entry : super.entrySet()) {
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
        for (CriterionProgress progress : super.values()) {
            long temp = progress.getObtainedL();
            if(temp>result)result = temp;
        }
        return result == 0?null:new Date(result);
    }


    @Nullable
    public long getFirstProgressDateL() {
        long result = 0;
        for (CriterionProgress progress : super.values()) {
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

            for (Entry<String, CriterionProgress> entry : progress.entrySet()) {
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
                advancementprogress.put(s, CriterionProgress.fromDateTime(advancementprogress, JsonUtils.getString(entry.getValue(), s)));
            }

            return advancementprogress;
        }
    }
}