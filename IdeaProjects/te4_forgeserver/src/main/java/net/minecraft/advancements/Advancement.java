package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;


public class Advancement {
    private final Advancement parent;
    private final DisplayInfo display;
    private final AdvancementRewards rewards;
    private final ResourceLocation id;
    private final Map<String, Criterion> criteria;
    private final String[][] requirements;
    private final Set<Advancement> children = Sets.newLinkedHashSet();
    private ITextComponent displayText;

    public Advancement(ResourceLocation id, @Nullable Advancement parent, @Nullable DisplayInfo display, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements) {
        this.id = id;
        this.display = display;
        this.criteria = ImmutableMap.copyOf(criteria);
        this.parent = parent;
        this.rewards = rewards;
        this.requirements = requirements;
        if (parent != null) {
            parent.addChild(this);
        }
    }


    public Builder copy() {
        return new Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
    }


    @Nullable
    public Advancement getParent() {
        return this.parent;
    }


    @Nullable
    public DisplayInfo getDisplay() {
        return this.display;
    }


    public AdvancementRewards getRewards() {
        return this.rewards;
    }


    @Override
    public String toString() {
        return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
    }


    public Iterable<Advancement> getChildren() {
        return this.children;
    }


    public Map<String, Criterion> getCriteria() {
        return this.criteria;
    }


    @SideOnly(Side.CLIENT)
    public int getRequirementCount() {
        return this.requirements.length;
    }


    public void addChild(Advancement child) {
        this.children.add(child);
    }


    public ResourceLocation getId() {
        return this.id;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Advancement)) {
            return false;
        } else {
            Advancement otherAdv = (Advancement)other;
            return this.id.equals(otherAdv.id);
        }
    }


    @Override
    public int hashCode() {
        return this.id.hashCode();
    }


    public String[][] getRequirements() {
        return this.requirements;
    }


    public ITextComponent getDisplayText() {
        ITextComponent displayText = this.displayText;
        if(displayText != null)return displayText;

        DisplayInfo display;
        if ((display = this.display) == null) {
            this.displayText = displayText = new TextComponentString(id.toString());
        } else {
            this.displayText = displayText = new TextComponentString("[");
            displayText.getStyle().setColor(display.getFrame().getFormat());
            ITextComponent title = display.getTitle().createCopy();
            ITextComponent hover = new TextComponentString("");
            ITextComponent hovTitle = title.createCopy();
            hovTitle.getStyle().setColor(display.getFrame().getFormat());
            hover.appendSibling(hovTitle);
            hover.appendText("\n");
            hover.appendSibling(display.getDescription());
            title.getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
            displayText.appendSibling(title);
            displayText.appendText("]");
        }
        return displayText;
    }


    public static class Builder {
        private final ResourceLocation parentId;
        private Advancement parent;
        private final DisplayInfo display;
        private final AdvancementRewards rewards;
        private final Map<String, Criterion> criteria;
        private final String[][] requirements;

        Builder(@Nullable ResourceLocation parent, @Nullable DisplayInfo display, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements) {
            this.parentId = parent;
            this.display = display;
            this.rewards = rewards;
            this.criteria = criteria;
            this.requirements = requirements;
        }


        public boolean resolveParent(Function<ResourceLocation, Advancement> func) {
            if (this.parentId == null) {
                return true;
            } else {
                this.parent = func.apply(this.parentId);
                return this.parent != null;
            }
        }


        public Advancement build(ResourceLocation loc) {
            return new Advancement(loc, this.parent, this.display, this.rewards, this.criteria, this.requirements);
        }


        public void writeTo(PacketBuffer buf) {
            if (this.parentId == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeResourceLocation(this.parentId);
            }

            if (this.display == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                this.display.write(buf);
            }

            Criterion.serializeToNetwork(this.criteria, buf);
            buf.writeVarInt(this.requirements.length);
            String[][] var2 = this.requirements;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String[] lvt_5_1_ = var2[var4];
                buf.writeVarInt(lvt_5_1_.length);
                String[] var6 = lvt_5_1_;
                int var7 = lvt_5_1_.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    String lvt_9_1_ = var6[var8];
                    buf.writeString(lvt_9_1_);
                }
            }
        }


        @Override
        public String toString() {
            return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
        }


        public static Builder deserialize(JsonObject jObj, JsonDeserializationContext ctx) {
            ResourceLocation parent = jObj.has("parent") ? new ResourceLocation(JsonUtils.getString(jObj, "parent")) : null;
            DisplayInfo lvt_3_1_ = jObj.has("display") ? DisplayInfo.deserialize(JsonUtils.getJsonObject(jObj, "display"), ctx) : null;
            AdvancementRewards lvt_4_1_ = JsonUtils.deserializeClass(jObj, "rewards", AdvancementRewards.EMPTY, ctx, AdvancementRewards.class);
            Map<String, Criterion> lvt_5_1_ = Criterion.criteriaFromJson(JsonUtils.getJsonObject(jObj, "criteria"), ctx);
            if (lvt_5_1_.isEmpty()) {
                throw new JsonSyntaxException("Advancement criteria cannot be empty");
            } else {
                JsonArray lvt_6_1_ = JsonUtils.getJsonArray(jObj, "requirements", new JsonArray());
                String[][] lvt_7_1_ = new String[lvt_6_1_.size()][];

                int lvt_8_1_;
                int lvt_10_1_;
                for(lvt_8_1_ = 0; lvt_8_1_ < lvt_6_1_.size(); ++lvt_8_1_) {
                    JsonArray lvt_9_1_ = JsonUtils.getJsonArray(lvt_6_1_.get(lvt_8_1_), "requirements[" + lvt_8_1_ + "]");
                    lvt_7_1_[lvt_8_1_] = new String[lvt_9_1_.size()];

                    for(lvt_10_1_ = 0; lvt_10_1_ < lvt_9_1_.size(); ++lvt_10_1_) {
                        lvt_7_1_[lvt_8_1_][lvt_10_1_] = JsonUtils.getString(lvt_9_1_.get(lvt_10_1_), "requirements[" + lvt_8_1_ + "][" + lvt_10_1_ + "]");
                    }
                }

                if (lvt_7_1_.length == 0) {
                    lvt_7_1_ = new String[lvt_5_1_.size()][];
                    lvt_8_1_ = 0;

                    String lvt_10_2_;
                    for(Iterator var16 = lvt_5_1_.keySet().iterator(); var16.hasNext(); lvt_7_1_[lvt_8_1_++] = new String[]{lvt_10_2_}) {
                        lvt_10_2_ = (String)var16.next();
                    }
                }

                String[][] var17 = lvt_7_1_;
                int var18 = lvt_7_1_.length;

                int var13;
                for(lvt_10_1_ = 0; lvt_10_1_ < var18; ++lvt_10_1_) {
                    String[] lvt_11_1_ = var17[lvt_10_1_];
                    if (lvt_11_1_.length == 0 && lvt_5_1_.isEmpty()) {
                        throw new JsonSyntaxException("Requirement entry cannot be empty");
                    }

                    String[] var12 = lvt_11_1_;
                    var13 = lvt_11_1_.length;

                    for(int var14 = 0; var14 < var13; ++var14) {
                        String lvt_15_1_ = var12[var14];
                        if (!lvt_5_1_.containsKey(lvt_15_1_)) {
                            throw new JsonSyntaxException("Unknown required criterion '" + lvt_15_1_ + "'");
                        }
                    }
                }

                Iterator var19 = lvt_5_1_.keySet().iterator();

                String lvt_9_2_;
                boolean lvt_10_3_;
                do {
                    if (!var19.hasNext()) {
                        return new Builder(parent, lvt_3_1_, lvt_4_1_, lvt_5_1_, lvt_7_1_);
                    }

                    lvt_9_2_ = (String)var19.next();
                    lvt_10_3_ = false;
                    String[][] var22 = lvt_7_1_;
                    int var24 = lvt_7_1_.length;

                    for(var13 = 0; var13 < var24; ++var13) {
                        String[] lvt_14_1_ = var22[var13];
                        if (ArrayUtils.contains(lvt_14_1_, lvt_9_2_)) {
                            lvt_10_3_ = true;
                            break;
                        }
                    }
                } while(lvt_10_3_);

                throw new JsonSyntaxException("Criterion '" + lvt_9_2_ + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
            }
        }


        public static Builder readFrom(PacketBuffer buf) throws IOException {
            ResourceLocation lvt_1_1_ = buf.readBoolean() ? buf.readResourceLocation() : null;
            DisplayInfo lvt_2_1_ = buf.readBoolean() ? DisplayInfo.read(buf) : null;
            Map<String, Criterion> lvt_3_1_ = Criterion.criteriaFromNetwork(buf);
            String[][] lvt_4_1_ = new String[buf.readVarInt()][];

            for(int lvt_5_1_ = 0; lvt_5_1_ < lvt_4_1_.length; ++lvt_5_1_) {
                lvt_4_1_[lvt_5_1_] = new String[buf.readVarInt()];

                for(int lvt_6_1_ = 0; lvt_6_1_ < lvt_4_1_[lvt_5_1_].length; ++lvt_6_1_) {
                    lvt_4_1_[lvt_5_1_][lvt_6_1_] = buf.readString(32767);
                }
            }

            return new Builder(lvt_1_1_, lvt_2_1_, AdvancementRewards.EMPTY, lvt_3_1_, lvt_4_1_);
        }
    }
}
