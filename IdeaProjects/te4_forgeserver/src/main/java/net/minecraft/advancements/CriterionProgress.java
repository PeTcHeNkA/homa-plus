package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.network.PacketBuffer;


public class CriterionProgress {
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private long obtained = 0;
    //private final AdvancementProgress advancementProgress;
    //private Date obtained;

    public CriterionProgress(){}


    @Deprecated
    public CriterionProgress(AdvancementProgress advancementProgressIn) {
        //this.advancementProgress = advancementProgressIn;
    }


    public boolean isObtained() {
        return this.obtained != 0;
    }


    public void obtain() {
        this.obtained = System.currentTimeMillis();
    }


    public void reset() {
        this.obtained = 0;
    }


    public Date getObtained() {
        return new Date(this.obtained);
    }


    public long getObtainedL(){
        return obtained;
    }


    public String toString() {
        return "CriterionProgress{obtained=" + (this.obtained == 0 ? "false" : this.obtained) + '}';
    }


    public void write(PacketBuffer buf) {
        buf.writeBoolean(this.obtained != 0);

        if (this.obtained != 0) {
            buf.writeLong(this.obtained);
            //buf.writeTime(this.obtained);
        }
    }


    public JsonElement serialize() {
        return this.obtained != 0 ? new JsonPrimitive(DATE_TIME_FORMATTER.format(new Date(this.obtained))) : JsonNull.INSTANCE;
    }


    public static CriterionProgress read(PacketBuffer buf, AdvancementProgress advancementProgressIn) {
        CriterionProgress criterionprogress = new CriterionProgress();
        if (buf.readBoolean()) {
            criterionprogress.obtained = buf.readLong();
        }
        return criterionprogress;
    }


    public static CriterionProgress fromDateTime(AdvancementProgress advancementProgressIn, String dateTime) {
        CriterionProgress criterionprogress = new CriterionProgress();
        try {
            criterionprogress.obtained = DATE_TIME_FORMATTER.parse(dateTime).getTime();
            return criterionprogress;
        } catch (ParseException parseexception) {
            throw new JsonSyntaxException("Invalid datetime: " + dateTime, parseexception);
        }
    }
}