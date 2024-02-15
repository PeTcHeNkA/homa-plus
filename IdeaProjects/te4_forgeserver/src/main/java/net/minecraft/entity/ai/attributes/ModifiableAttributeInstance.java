package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.tson.utl.Te4HashSet;


public class ModifiableAttributeInstance implements IAttributeInstance {
    private final AbstractAttributeMap attributeMap;
    private final IAttribute genericAttribute;
    private final Set<AttributeModifier>[] mapByOperation = new Set[3];
    private final Map<String, Set<AttributeModifier>> mapByName = Maps.newHashMap();
    private final Map<UUID, AttributeModifier> mapByUUID = Maps.newHashMap();
    private double baseValue;
    private boolean needsUpdate = true;
    private double cachedValue;

    public ModifiableAttributeInstance(AbstractAttributeMap attributeMapIn, IAttribute genericAttributeIn) {
        this.attributeMap = attributeMapIn;
        this.genericAttribute = genericAttributeIn;
        this.baseValue = genericAttributeIn.getDefaultValue();
        for (int i = 0; i < mapByOperation.length; i++) {
            this.mapByOperation[i] = new Te4HashSet<>();
        }
    }


    @Override
    public IAttribute getAttribute() {
        return this.genericAttribute;
    }


    @Override
    public double getBaseValue() {
        return this.baseValue;
    }


    @Override
    public void setBaseValue(double baseValue) {
        if (baseValue != this.getBaseValue()) {
            this.baseValue = baseValue;
            this.flagForUpdate();
        }
    }


    @Override
    public Collection<AttributeModifier> getModifiersByOperation(int operation) {
        if(operation>2)return Collections.EMPTY_LIST;
        return this.mapByOperation[operation];
    }


    @Override
    public Collection<AttributeModifier> getModifiers() {
        Set<AttributeModifier> set = new Te4HashSet<>();
        for (int i = 0; i < 3; ++i) {
            set.addAll(this.getModifiersByOperation(i));
        }
        return set;
    }


    @Nullable
    public AttributeModifier getModifier(UUID uuid)
    {
        return this.mapByUUID.get(uuid);
    }

    public boolean hasModifier(AttributeModifier modifier)
    {
        return this.mapByUUID.get(modifier.getID()) != null;
    }


    @Override
    public void applyModifier(AttributeModifier modifier) {
        if (this.getModifier(modifier.getID()) != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            Set<AttributeModifier> set = this.mapByName.get(modifier.getName());

            if (set == null) {
                set = new Te4HashSet<>();
                this.mapByName.put(modifier.getName(), set);
            }
            (this.mapByOperation[modifier.getOperation()]).add(modifier);
            set.add(modifier);
            this.mapByUUID.put(modifier.getID(), modifier);
            this.flagForUpdate();
        }
    }


    protected void flagForUpdate() {
        this.needsUpdate = true;
        this.attributeMap.onAttributeModified(this);
    }


    @Override
    public void removeModifier(AttributeModifier modifier) {
        for (int i = 0; i < 3; i++) {
            Set<AttributeModifier> set = this.mapByOperation[i];
            set.remove(modifier);
        }
        Set<AttributeModifier> set1 = this.mapByName.get(modifier.getName());

        if (set1 != null) {
            set1.remove(modifier);
            if (set1.isEmpty()) {
                this.mapByName.remove(modifier.getName());
            }
        }
        this.mapByUUID.remove(modifier.getID());
        this.flagForUpdate();
    }


    @Override
    public void removeModifier(UUID p_188479_1_) {
        AttributeModifier attributemodifier = this.getModifier(p_188479_1_);
        if (attributemodifier != null) {
            this.removeModifier(attributemodifier);
        }
    }


    @SideOnly(Side.CLIENT)
    public void removeAllModifiers() {
        Collection<AttributeModifier> collection = this.getModifiers();
        if (collection != null) {
            for (AttributeModifier attributemodifier : Lists.newArrayList(collection)) {
                this.removeModifier(attributemodifier);
            }
        }
    }


    @Override
    public double getAttributeValue() {
        if (this.needsUpdate) {
            this.cachedValue = this.computeValue();
            this.needsUpdate = false;
        }
        return this.cachedValue;
    }


    private double computeValue() {
        double d0 = this.getBaseValue();
        for (AttributeModifier attributemodifier : this.getAppliedModifiers(0)) {
            d0 += attributemodifier.getAmount();
        }

        double d1 = d0;

        for (AttributeModifier attributemodifier1 : this.getAppliedModifiers(1)) {
            d1 += d0 * attributemodifier1.getAmount();
        }

        for (AttributeModifier attributemodifier2 : this.getAppliedModifiers(2)) {
            d1 *= 1.0D + attributemodifier2.getAmount();
        }
        return this.genericAttribute.clampValue(d1);
    }


    private Collection<AttributeModifier> getAppliedModifiers(int operation) {
        Set<AttributeModifier> set = new Te4HashSet<>();
        set.addAll(this.getModifiersByOperation(operation));

        for (IAttribute iattribute = this.genericAttribute.getParent(); iattribute != null; iattribute = iattribute.getParent()) {
            IAttributeInstance iattributeinstance = this.attributeMap.getAttributeInstance(iattribute);

            if (iattributeinstance != null) {
                set.addAll(iattributeinstance.getModifiersByOperation(operation));
            }
        }
        return set;
    }
}