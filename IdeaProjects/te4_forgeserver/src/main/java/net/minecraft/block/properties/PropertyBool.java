package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;


public class PropertyBool extends PropertyHelper<Boolean> {
    private static final ImmutableSet<Boolean> allowedValues = ImmutableSet.of(Boolean.TRUE, Boolean.FALSE);
    private static final int hash = allowedValues.hashCode();

    protected PropertyBool(String name) {
        super(name, Boolean.class);
    }


    @Override
    protected int onCalchash(int src, Object... init) {
        return 31 * src + hash;
    }


    @Override
    public Collection<Boolean> getAllowedValues() {
        return allowedValues;
    }


    public static PropertyBool create(String name) {
        return new PropertyBool(name);
    }


    @Override
    public Optional<Boolean> parseValue(String value) {
        return !"true".equals(value) && !"false".equals(value) ? Optional.absent() : Optional.of(Boolean.valueOf(value));
    }


    @Override
    public String getName(Boolean value) {
        return value.toString();
    }
}