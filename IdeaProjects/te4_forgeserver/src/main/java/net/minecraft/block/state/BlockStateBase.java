package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;


public abstract class BlockStateBase implements IBlockState {
    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private static final Function < Entry < IProperty<?>, Comparable<? >> , String > MAP_ENTRY_TO_STRING = new Function < Entry < IProperty<?>, Comparable<? >> , String > () {
        @Nullable
        public String apply(@Nullable Entry < IProperty<?>, Comparable<? >> entry) {
            if (entry == null) {
                return "<NULL>";
            } else {
                IProperty<?> prop = entry.getKey();
                return prop.getName() + "=" + this.getPropertyName(prop, entry.getValue());
            }
        }
        private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> entry) {
            return property.getName((T)entry);
        }
    };


    public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {
        return this.withProperty(property, cyclePropertyValue(property.getAllowedValues(), this.getValue(property)));
    }


    protected static <T> T cyclePropertyValue(Collection<T> values, T currentValue) {
        Iterator<T> iterator = values.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(currentValue)) {
                if (iterator.hasNext())
                    return iterator.next();
                return values.iterator().next();
            }
        }
        return iterator.next();
    }


    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Block.REGISTRY.getNameForObject(this.getBlock()));

        if (!this.getProperties().isEmpty()) {
            stringbuilder.append("[");
            COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(this.getProperties().entrySet(), MAP_ENTRY_TO_STRING));
            stringbuilder.append("]");
        }

        return stringbuilder.toString();
    }


    @Nullable
    public ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> getPropertyValueTable() {
        return null;
    }

    private int tid;

    @Override
    public final int getId() {
        return tid;
    }


    @Override
    public final void setId(int hs) {
        this.tid = hs;
    }
}