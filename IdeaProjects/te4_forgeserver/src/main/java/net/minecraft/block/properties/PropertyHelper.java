package net.minecraft.block.properties;

import com.google.common.base.MoreObjects;


public abstract class PropertyHelper<T extends Comparable<T>> implements IProperty<T> {
    private final Class<T> valueClass;
    private final String name;
    private final int hash;

    protected PropertyHelper(String name, Class<T> valueClass, Object... init) {
        this.valueClass = valueClass;
        this.name = name;
        hash = onCalchash(31 * this.valueClass.hashCode() + this.name.hashCode(), init);
        //hash = 31 * this.valueClass.hashCode() + this.name.hashCode();
    }


    protected int onCalchash(int src, Object... init){
        return src;
    }


    public String getName() {
        return this.name;
    }


    public Class<T> getValueClass() {
        return this.valueClass;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.valueClass).add("values", this.getAllowedValues()).toString();
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof PropertyHelper)) {
            return false;
        } else {
            PropertyHelper<?> propertyhelper = (PropertyHelper)other;
            return this.valueClass == propertyhelper.valueClass && this.name.equals(propertyhelper.name);
        }
    }


    @Override
    public int hashCode() {
        return hash;
    }
}