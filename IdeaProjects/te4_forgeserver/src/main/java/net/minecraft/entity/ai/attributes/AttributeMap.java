package net.minecraft.entity.ai.attributes;

import java.util.Collection;
import java.util.Set;
import net.minecraft.util.LowerStringMap;
import plus.tson.utl.Te4HashSet;


public class AttributeMap extends AbstractAttributeMap {
    private final Set<IAttributeInstance> dirtyInstances = new Te4HashSet<>(8);
    protected final LowerStringMap<IAttributeInstance> instancesByName = new LowerStringMap<>();

    @Override
    public ModifiableAttributeInstance getAttributeInstance(IAttribute attribute) {
        return (ModifiableAttributeInstance)super.getAttributeInstance(attribute);
    }


    @Override
    public ModifiableAttributeInstance getAttributeInstanceByName(String attributeName) {
        IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);
        if (iattributeinstance == null) {
            iattributeinstance = this.instancesByName.get(attributeName);
        }
        return (ModifiableAttributeInstance)iattributeinstance;
    }


    @Override
    public IAttributeInstance registerAttribute(IAttribute attribute) {
        IAttributeInstance iattributeinstance = super.registerAttribute(attribute);

        if (attribute instanceof RangedAttribute) {
            String dscr = ((RangedAttribute) attribute).getDescription();
            if(dscr != null) this.instancesByName.put(dscr, iattributeinstance);
        }

        return iattributeinstance;
    }


    @Override
    public IAttributeInstance registerAttributeUnsafe(IAttribute attribute) {
        IAttributeInstance iattributeinstance = super.registerAttributeUnsafe(attribute);

        if (attribute instanceof RangedAttribute) {
            String dscr = ((RangedAttribute) attribute).getDescription();
            if(dscr != null) this.instancesByName.put(dscr, iattributeinstance);
        }

        return iattributeinstance;
    }


    @Override
    protected IAttributeInstance createInstance(IAttribute attribute) {
        return new ModifiableAttributeInstance(this, attribute);
    }


    @Override
    public void onAttributeModified(IAttributeInstance instance) {
        if (instance.getAttribute().getShouldWatch()) {
            this.dirtyInstances.add(instance);
        }
        for (IAttribute iattribute : this.descendantsByParent.get(instance.getAttribute())) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttributeInstance(iattribute);

            if (modifiableattributeinstance != null) {
                modifiableattributeinstance.flagForUpdate();
            }
        }
    }


    public Set<IAttributeInstance> getDirtyInstances() {
        return this.dirtyInstances;
    }


    public Collection<IAttributeInstance> getWatchedAttributes() {
        Set<IAttributeInstance> set = new Te4HashSet<>();
        for (IAttributeInstance iattributeinstance : this.getAllAttributes()) {
            if (iattributeinstance.getAttribute().getShouldWatch()) {
                set.add(iattributeinstance);
            }
        }
        return set;
    }
}