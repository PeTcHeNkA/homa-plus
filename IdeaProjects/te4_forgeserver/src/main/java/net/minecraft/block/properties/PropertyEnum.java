package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.IStringSerializable;


public class PropertyEnum<T extends Enum<T> & IStringSerializable> extends PropertyHelper<T> {
    private final ImmutableSet<T> allowedValues;
    private final Map<String, T> nameToValue;

    protected PropertyEnum(String name, Class<T> valueClass, Collection<T> allowedValues) {
        this(name, valueClass, ImmutableSet.copyOf(allowedValues), new HashMap<>());
    }


    private PropertyEnum(String name, Class<T> valueClass, ImmutableSet<T> allowedValues, Map<String, T> nameToValue) {
        super(name, valueClass, allowedValues, nameToValue);
        this.nameToValue = nameToValue;
        this.allowedValues = allowedValues;
    }


    @Override
    protected int onCalchash(int src, Object... init) {
        ImmutableSet<T> allowedValues = (ImmutableSet<T>) init[0];
        Map<String, T> nameToValue = (Map<String, T>) init[1];
        int im = allowedValues.hashCode();
        for (T t : allowedValues) {
            String s = t.getName();
            if (nameToValue.containsKey(s)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + s + "'");
            }
            nameToValue.put(s, t);
        }
        return 31 * (31*nameToValue.hashCode()+src) + im;
    }


    @Override
    public Collection<T> getAllowedValues() {
        return this.allowedValues;
    }


    @Override
    public Optional<T> parseValue(String value) {
        return Optional.<T>fromNullable(this.nameToValue.get(value));
    }


    @Override
    public String getName(T value) {
        return value.getName();
    }


    @Override
    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ instanceof PropertyEnum && super.equals(p_equals_1_)) {
            PropertyEnum<?> propertyenum = (PropertyEnum)p_equals_1_;
            return this.allowedValues.equals(propertyenum.allowedValues) && this.nameToValue.equals(propertyenum.nameToValue);
        } else {
            return false;
        }
    }


    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz) {
        return create(name, clazz, Predicates.alwaysTrue());
    }


    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz, Predicate<T> filter) {
        return create(name, clazz, Collections2.filter(Lists.newArrayList(clazz.getEnumConstants()), filter));
    }


    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz, T... values) {
        return create(name, clazz, Lists.newArrayList(values));
    }


    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz, Collection<T> values) {
        return new PropertyEnum<>(name, clazz, values);
    }
}