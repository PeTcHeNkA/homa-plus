//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.operation;

import java.util.concurrent.*;
import org.cache2k.integration.*;
import org.cache2k.processor.*;

public class Operations<K, V>
{
    public static final Operations SINGLETON;
    static final Semantic PEEK;
    public static final Semantic GET;
    public final Semantic UNCONDITIONAL_LOAD;
    public static final Semantic REFRESH;
    static final Semantic GET_ENTRY;
    static final Semantic PEEK_ENTRY;
    static final Semantic REMOVE;
    static final Semantic CONTAINS_REMOVE;
    static final Semantic CONTAINS;
    static final Semantic PEEK_REMOVE;
    
    public Operations() {
        this.UNCONDITIONAL_LOAD = new Semantic.Update() {
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                c.load();
            }
        };
    }
    
    public Semantic<K, V, V> peek(final K key) {
        return (Semantic<K, V, V>)Operations.PEEK;
    }
    
    public Semantic<K, V, V> get(final K key) {
        return (Semantic<K, V, V>)Operations.GET;
    }
    
    public Semantic<K, V, ResultEntry<K, V>> getEntry(final K key) {
        return (Semantic<K, V, ResultEntry<K, V>>)Operations.GET_ENTRY;
    }
    
    public Semantic<K, V, ResultEntry<K, V>> peekEntry(final K key) {
        return (Semantic<K, V, ResultEntry<K, V>>)Operations.PEEK_ENTRY;
    }
    
    public Semantic<K, V, V> remove(final K key) {
        return (Semantic<K, V, V>)Operations.REMOVE;
    }
    
    public Semantic<K, V, Boolean> containsAndRemove(final K key) {
        return (Semantic<K, V, Boolean>)Operations.CONTAINS_REMOVE;
    }
    
    public Semantic<K, V, Boolean> contains(final K key) {
        return (Semantic<K, V, Boolean>)Operations.CONTAINS;
    }
    
    public Semantic<K, V, V> peekAndRemove(final K key) {
        return (Semantic<K, V, V>)Operations.PEEK_REMOVE;
    }
    
    public Semantic<K, V, V> peekAndReplace(final K key, final V value) {
        return new Semantic.UpdateExisting<K, V, V>() {
            @Override
            public void update(final Progress<K, V, V> c, final ExaminationEntry<K, V> e) {
                if (c.isPresentOrMiss()) {
                    c.result((V)e.getValueOrException());
                    c.put(value);
                }
            }
        };
    }
    
    public Semantic<K, V, V> peekAndPut(final K key, final V value) {
        return new Semantic.UpdateExisting<K, V, V>() {
            @Override
            public void update(final Progress<K, V, V> c, final ExaminationEntry<K, V> e) {
                if (c.isPresentOrMiss()) {
                    c.result((V)e.getValueOrException());
                }
                c.put(value);
            }
        };
    }
    
    public Semantic<K, V, V> computeIfAbsent(final K key, final Callable<V> _function) {
        return new Semantic.UpdateExisting<K, V, V>() {
            @Override
            public void update(final Progress<K, V, V> c, final ExaminationEntry<K, V> e) {
                if (c.isPresentOrMiss()) {
                    c.result((V)e.getValueOrException());
                }
                else {
                    try {
                        final V _value = _function.call();
                        c.put(_value);
                        c.result(_value);
                    }
                    catch (RuntimeException ex) {
                        c.failure(ex);
                    }
                    catch (Exception ex2) {
                        c.failure((RuntimeException)new CacheLoaderException(ex2));
                    }
                }
            }
        };
    }
    
    public Semantic<K, V, V> put(final K key, final V value) {
        return new Semantic.Update<K, V, V>() {
            @Override
            public void update(final Progress<K, V, V> c, final ExaminationEntry<K, V> e) {
                c.put(value);
            }
        };
    }
    
    public Semantic<K, V, Boolean> putIfAbsent(final K key, final V value) {
        return new Semantic.UpdateExisting<K, V, Boolean>() {
            @Override
            public void update(final Progress<K, V, Boolean> c, final ExaminationEntry<K, V> e) {
                if (!c.isPresentOrMiss()) {
                    c.result(true);
                    c.put(value);
                    return;
                }
                c.result(false);
            }
        };
    }
    
    public Semantic<K, V, Boolean> replace(final K key, final V value) {
        return new Semantic.UpdateExisting<K, V, Boolean>() {
            @Override
            public void update(final Progress<K, V, Boolean> c, final ExaminationEntry<K, V> e) {
                if (c.isPresentOrMiss()) {
                    c.result(true);
                    c.put(value);
                    return;
                }
                c.result(false);
            }
        };
    }
    
    public Semantic<K, V, Boolean> replace(final K key, final V value, final V newValue) {
        return new Semantic.UpdateExisting<K, V, Boolean>() {
            @Override
            public void update(final Progress<K, V, Boolean> c, final ExaminationEntry<K, V> e) {
                if (c.isPresentOrMiss() && (value == e.getValueOrException() || (value != null && value.equals(e.getValueOrException())))) {
                    c.result(true);
                    c.put(newValue);
                    return;
                }
                c.result(false);
            }
        };
    }
    
    public Semantic<K, V, Boolean> remove(final K key, final V value) {
        return new Semantic.UpdateExisting<K, V, Boolean>() {
            @Override
            public void update(final Progress<K, V, Boolean> c, final ExaminationEntry<K, V> e) {
                if (c.isPresentOrMiss() && ((value == null && e.getValueOrException() == null) || value.equals(e.getValueOrException()))) {
                    c.result(true);
                    c.remove();
                    return;
                }
                c.result(false);
            }
        };
    }
    
    public <R> Semantic<K, V, R> invoke(final K key, final boolean _readThrough, final EntryProcessor<K, V, R> _processor) {
        return new Semantic.UpdateExisting<K, V, R>() {
            @Override
            public void update(final Progress<K, V, R> c, final ExaminationEntry<K, V> e) {
                final MutableEntryOnProgress<K, V> _mutableEntryOnProgress = (MutableEntryOnProgress<K, V>)new MutableEntryOnProgress((Progress)c, (ExaminationEntry)e, _readThrough);
                try {
                    final R _result = _processor.process(_mutableEntryOnProgress);
                    c.result(_result);
                }
                catch (NeedsLoadRestartException rs) {
                    c.loadAndMutation();
                    return;
                }
                catch (Throwable t) {
                    c.failure((RuntimeException)new EntryProcessingException(t));
                    return;
                }
                _mutableEntryOnProgress.sendMutationCommandIfNeeded();
            }
            
            @Override
            public void loaded(final Progress<K, V, R> c, final ExaminationEntry<K, V> e) {
            }
        };
    }
    
    public Semantic<K, V, Void> expire(final K key, final long t) {
        return new Semantic.UpdateExisting<K, V, Void>() {
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                if (t == 0L || t == 1L) {
                    if (c.isPresentOrInRefreshProbation()) {
                        c.expire(t);
                    }
                    return;
                }
                if (c.isPresent()) {
                    c.expire(t);
                }
            }
        };
    }
    
    static {
        SINGLETON = new Operations();
        PEEK = new Semantic.Read() {
            @Override
            public void examine(final Progress c, final ExaminationEntry e) {
                if (c.isPresentOrMiss()) {
                    c.result(e.getValueOrException());
                }
            }
        };
        GET = new Semantic.MightUpdateExisting() {
            @Override
            public void examine(final Progress c, final ExaminationEntry e) {
                if (c.isPresentOrMiss()) {
                    c.result(e.getValueOrException());
                }
                else {
                    c.wantMutation();
                }
            }
            
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                c.load();
            }
        };
        REFRESH = new Semantic.Update() {
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                c.refresh();
            }
        };
        GET_ENTRY = new Semantic.MightUpdateExisting() {
            @Override
            public void examine(final Progress c, final ExaminationEntry e) {
                if (c.isPresentOrMiss()) {
                    c.entryResult(e);
                }
                else {
                    c.wantMutation();
                }
            }
            
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                c.load();
            }
            
            @Override
            public void loaded(final Progress c, final ExaminationEntry e) {
                c.entryResult(e);
            }
        };
        PEEK_ENTRY = new Semantic.Read() {
            @Override
            public void examine(final Progress c, final ExaminationEntry e) {
                if (c.isPresentOrMiss()) {
                    c.entryResult(e);
                }
            }
        };
        REMOVE = new Semantic.Update() {
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                c.remove();
            }
        };
        CONTAINS_REMOVE = new Semantic.UpdateExisting() {
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                if (c.isPresent()) {
                    c.result(true);
                    c.remove();
                    return;
                }
                c.result(false);
                c.remove();
            }
        };
        CONTAINS = new Semantic.Read() {
            @Override
            public void examine(final Progress c, final ExaminationEntry e) {
                if (c.isPresent()) {
                    c.result(true);
                    return;
                }
                c.result(false);
            }
        };
        PEEK_REMOVE = new Semantic.UpdateExisting() {
            @Override
            public void update(final Progress c, final ExaminationEntry e) {
                if (c.isPresentOrMiss()) {
                    c.result(e.getValueOrException());
                }
                c.remove();
            }
        };
    }
    
    public static class NeedsLoadRestartException extends RestartException
    {
    }
}
