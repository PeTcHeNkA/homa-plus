package com.google.common.collect;

import plus.tson.utl.Te4HashMap;
import plus.tson.utl.uns.UnsafeUtils;
import java.util.Map;
import static plus.tson.utl.uns.UnsafeUtils.*;


public class ImmutableTrimmer extends Te4HashMap<ImmutableMapEntry<?,?>,ImmutableMapEntry<?,?>> {
    //ImmutableMapEntry<K, V>[] table
    private static final long regImmTableOff    = offset(RegularImmutableMap.class,"table");
    //Entry<K, V>[] entries
    private static final long regImmEntriesOff  = offset(RegularImmutableMap.class,"entries");
    //ImmutableMap<R, Map<C, V>> rowMap
    private static final long tableImmRowOff    = offset(SparseImmutableTable.class, "rowMap");
    //ImmutableMap<C, Map<R, V>> columnMap
    private static final long tableImmColumnOff = offset(SparseImmutableTable.class, "columnMap");


    public void tryTrim(Map<?,?> map){
        if(map == null)return;
        if(map instanceof RegularImmutableMap)
            trim((RegularImmutableMap<?, ?>) map);
    }


    public void tryTrim(ImmutableTable<?,?,?> tab){
        if(tab == null)return;
        if(tab.getClass() == SparseImmutableTable.class)
            trim((SparseImmutableTable<?, ?, ?>) tab);
    }


    public void trim(RegularImmutableMap<?,?> map){
        ImmutableMapEntry<?, ?>[] table = UnsafeUtils.get(map, regImmTableOff);

        for (int i = 0, s = table.length; i < s; i++){
            ImmutableMapEntry<?,?> entry = table[i];
            if(entry != null){
                if(entry.getClass() == ImmutableMapEntry.class)
                    trimEntry(table, i, entry);
            }
        }

        Entry<?, ?>[] entries = UnsafeUtils.get(map, regImmEntriesOff);

        for (int i = 0, s = entries.length; i < s; i++){
            Entry<?,?> entry0 = entries[i];
            if(entry0 != null){
                if(entry0.getClass() == ImmutableMapEntry.class)
                    trimEntry(entries, i, (ImmutableMapEntry) entry0);
            }
        }
    }


    private void trimEntry(Entry<?, ?>[] table, int i, ImmutableMapEntry<?, ?> entry) {
        ImmutableMapEntry<?, ?> prev = super.get(entry);
        if(prev != null)table[i] = prev;
        else {
            super.fput(entry, entry);
            Object value = entry.getValue();
            if(value.getClass() == RegularImmutableMap.class){
                trim((RegularImmutableMap<?, ?>) value);
            }
        }
    }


    public void trim(SparseImmutableTable<?,?,?> tab){
        ImmutableMap<?, Map<?, ?>> rowMap = UnsafeUtils.get(tab, tableImmRowOff);
        tryTrim(rowMap);
        ImmutableMap<?, Map<?, ?>> columnMap = UnsafeUtils.get(tab, tableImmColumnOff);
        tryTrim(columnMap);
    }
}