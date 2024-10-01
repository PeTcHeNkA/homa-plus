package plus.fserver.utls.collections;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.world.chunk.Chunk;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;

/**
 * A type-specific hash map with a fast, small-footprint implementation.
 * @see Hash
 * @see HashCommon
 */
public class Long2ChunkMap extends AbstractLong2ObjectMap<Chunk> implements java.io.Serializable, Cloneable, Hash {
	private static final long serialVersionUID = 11507L;
	private static final boolean ASSERTS = false;
	/** The array of keys. */
	protected transient long[] key;
	/** The array of values. */
	protected transient Chunk[] value;
	/** The mask for wrapping a position counter. */
	protected transient int mask;
	/** Whether this set contains the key zero. */
	protected transient boolean containsNullKey;
	/** The current table size. */
	protected transient int n;
	/**
	 * Threshold after which we rehash. It must be the table size times
	 * {@link #f}.
	 */
	protected transient int maxFill;
	/** Number of entries in the set (including the key zero, if present). */
	protected int size;
	/** The acceptable load factor. */
	protected final float f;
	/** Cached set of entries. */
	protected transient FastEntrySet<Chunk> entries;
	/** Cached set of keys. */
	protected transient LongSet keys;
	/** Cached collection of values. */
	protected transient ObjectCollection<Chunk> values;

	/**
	 * Creates a new hash map.
	 *
	 * <p>
	 * The actual table size will be the least power of two greater than
	 * <code>expected</code>/<code>f</code>.
	 *
	 * @param expected
	 *            the expected number of elements in the hash set.
	 * @param f
	 *            the load factor.
	 */
	@SuppressWarnings("unchecked")
	public Long2ChunkMap(final int expected, final float f) {
		if (f <= 0 || f > 1) throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
		if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		this.f = f;
		n = arraySize(expected, f);
		mask = n - 1;
		maxFill = maxFill(n, f);
		key = new long[n + 1];
		value = new Chunk[n + 1];
	}


	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor.
	 *
	 * @param expected
	 *            the expected number of elements in the hash map.
	 */
	public Long2ChunkMap(final int expected) {
		this(expected, DEFAULT_LOAD_FACTOR);
	}


	/**
	 * Creates a new hash map with initial expected
	 * {@link Hash#DEFAULT_INITIAL_SIZE} entries and
	 * {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 */
	public Long2ChunkMap() {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map copying a given one.
	 *
	 * @param m
	 *            a {@link Map} to be copied into the new hash map.
	 * @param f
	 *            the load factor.
	 */
	public Long2ChunkMap(final Map<? extends Long, ? extends Chunk> m, final float f) {
		this(m.size(), f);
		putAll(m);
	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor copying a given one.
	 *
	 * @param m
	 *            a {@link Map} to be copied into the new hash map.
	 */
	public Long2ChunkMap(final Map<? extends Long, ? extends Chunk> m) {
		this(m, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map copying a given type-specific one.
	 *
	 * @param m
	 *            a type-specific map to be copied into the new hash map.
	 * @param f
	 *            the load factor.
	 */
	public Long2ChunkMap(final Long2ObjectMap<Chunk> m, final float f) {
		this(m.size(), f);
		putAll(m);
	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor copying a given type-specific one.
	 *
	 * @param m
	 *            a type-specific map to be copied into the new hash map.
	 */
	public Long2ChunkMap(final Long2ObjectMap<Chunk> m) {
		this(m, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map using the elements of two parallel arrays.
	 *
	 * @param k
	 *            the array of keys of the new hash map.
	 * @param v
	 *            the array of corresponding values in the new hash map.
	 * @param f
	 *            the load factor.
	 * @throws IllegalArgumentException
	 *             if <code>k</code> and <code>v</code> have different lengths.
	 */
	public Long2ChunkMap(final long[] k, final Chunk[] v, final float f) {
		this(k.length, f);
		if (k.length != v.length) throw new IllegalArgumentException("The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
		for (int i = 0; i < k.length; i++)
			this.put(k[i], v[i]);
	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor using the elements of two parallel arrays.
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
	public Long2ChunkMap(final long[] k, final Chunk[] v) {
		this(k, v, DEFAULT_LOAD_FACTOR);
	}


	private int realSize() {
		return containsNullKey ? size - 1 : size;
	}


	private void ensureCapacity(final int capacity) {
		final int needed = arraySize(capacity, f);
		if (needed > n) rehash(needed);
	}

	private void tryCapacity(final long capacity) {
		final int needed = (int) Math.min(1 << 30, Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / f))));
		if (needed > n) rehash(needed);
	}

	private Chunk removeEntry(final int pos) {
		final Chunk oldValue = value[pos];
		value[pos] = null;
		size--;
		shiftKeys(pos);
		if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
		return oldValue;
	}

	private Chunk removeNullEntry() {
		containsNullKey = false;
		final Chunk oldValue = value[n];
		value[n] = null;
		size--;
		if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
		return oldValue;
	}

	/** {@inheritDoc} */
	public void putAll(Map<? extends Long, ? extends Chunk> m) {
		if (f <= .5) ensureCapacity(m.size()); // The resulting map will be
												// sized for m.size() elements
		else tryCapacity(size() + m.size()); // The resulting map will be
												// tentatively sized for size()
												// + m.size() elements
		super.putAll(m);
	}


	private int insert(final long k, final Chunk v) {
		int pos;
		if (((k) == (0))) {
			if (containsNullKey) return n;
			containsNullKey = true;
			pos = n;
		} else {
			long curr;
			final long[] key = this.key;
			// The starting point.
			if (!((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) {
				if (((curr) == (k))) return pos;
				while (!((curr = key[pos = (pos + 1) & mask]) == (0)))
					if (((curr) == (k))) return pos;
			}
		}
		key[pos] = k;
		value[pos] = v;
		if (size++ >= maxFill) rehash(arraySize(size + 1, f));
		if (ASSERTS) checkTable();
		return -1;
	}


	public Chunk put(final long k, final Chunk v) {
		final int pos = insert(k, v);
		if (pos < 0) return defRetValue;
		final Chunk oldValue = value[pos];
		value[pos] = v;
		return oldValue;
	}


	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Chunk put(final Long ok, final Chunk ov) {
		final Chunk v = (ov);
		final int pos = insert(((ok).longValue()), v);
		if (pos < 0) return (this.defRetValue);
		final Chunk oldValue = value[pos];
		value[pos] = v;
		return (oldValue);
	}


	/**
	 * Shifts left entries with the specified hash code, starting at the
	 * specified position, and empties the resulting free entry.
	 *
	 * @param pos
	 *            a starting position.
	 */
	protected final void shiftKeys(int pos) {
		// Shift entries with the same hash.
		int last, slot;
		long curr;
		final long[] key = this.key;
		for (;;) {
			pos = ((last = pos) + 1) & mask;
			for (;;) {
				if (((curr = key[pos]) == (0))) {
					key[last] = (0);
					value[last] = null;
					return;
				}
				slot = (int) HashCommon.mix((curr)) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
				pos = (pos + 1) & mask;
			}
			key[last] = curr;
			value[last] = value[pos];
		}
	}


	public Chunk remove(final long k) {
		if (((k) == (0))) {
			if (containsNullKey) return removeNullEntry();
			return defRetValue;
		}
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return defRetValue;
		if (((k) == (curr))) return removeEntry(pos);
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == (0))) return defRetValue;
			if (((k) == (curr))) return removeEntry(pos);
		}
	}


	@Deprecated
	@Override
	public Chunk remove(final Object ok) {
		final long k = ((((Long) (ok)).longValue()));
		if (((k) == (0))) {
			if (containsNullKey) return (removeNullEntry());
			return (this.defRetValue);
		}
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return (this.defRetValue);
		if (((curr) == (k))) return (removeEntry(pos));
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == (0))) return (this.defRetValue);
			if (((curr) == (k))) return (removeEntry(pos));
		}
	}

	/**
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	public Chunk get(final Long ok) {
		if (ok == null) return null;
		final long k = ((ok).longValue());
		if (((k) == (0))) return containsNullKey ? (value[n]) : (this.defRetValue);
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return (this.defRetValue);
		if (((k) == (curr))) return (value[pos]);
		// There's always an unused entry.
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == (0))) return (this.defRetValue);
			if (((k) == (curr))) return (value[pos]);
		}
	}


	public Chunk get(final long k) {
		if (k == 0) return containsNullKey ? value[n] : defRetValue;
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if ((curr = key[pos = (int) HashCommon.mix(k) & mask]) == 0) return defRetValue;
		if (k == curr) return value[pos];
		// There's always an unused entry.
		while (true) {
			if ((curr = key[pos = (pos + 1) & mask]) == 0) return defRetValue;
			if (k == curr) return value[pos];
		}
	}


	public boolean containsKey(final long k) {
		if (((k) == (0))) return containsNullKey;
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return false;
		if (((k) == (curr))) return true;
		// There's always an unused entry.
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == (0))) return false;
			if (((k) == (curr))) return true;
		}
	}


	@Override
	public boolean containsValue(final Object v) {
		final Chunk[] value = this.value;
		final long[] key = this.key;
		if (containsNullKey && ((value[n]) == null ? (v) == null : (value[n]).equals(v))) return true;
		for (int i = n; i-- != 0;)
			if (!((key[i]) == (0)) && ((value[i]) == null ? (v) == null : (value[i]).equals(v))) return true;
		return false;
	}


	@Override
	public void clear() {
		if (size == 0) return;
		size = 0;
		containsNullKey = false;
		Arrays.fill(key, 0);
		Arrays.fill(value, null);
	}


	@Override
	public int size() {
		return size;
	}


	@Override
	public boolean isEmpty() {
		return size == 0;
	}


	@Deprecated
	public void growthFactor(int growthFactor) {}


	@Deprecated
	public int growthFactor() {
		return 16;
	}


	final class MapEntry implements Entry<Chunk>, Map.Entry<Long, Chunk> {
		int index;
		MapEntry(final int index) {
			this.index = index;
		}
		MapEntry() {}

		@Deprecated
		public Long getKey() {
			return (Long.valueOf(key[index]));
		}
		public long getLongKey() {
			return key[index];
		}
		public Chunk getValue() {
			return (value[index]);
		}
		public Chunk setValue(final Chunk v) {
			final Chunk oldValue = value[index];
			value[index] = v;
			return oldValue;
		}

		@SuppressWarnings("unchecked")
		public boolean equals(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<Long, Chunk> e = (Map.Entry<Long, Chunk>) o;
			return ((key[index]) == (((e.getKey()).longValue()))) && ((value[index]) == null ? ((e.getValue())) == null : (value[index]).equals((e.getValue())));
		}
		@Override
		public int hashCode() {
			return HashCommon.long2int(key[index]) ^ ((value[index]) == null ? 0 : (value[index]).hashCode());
		}
		@Override
		public String toString() {
			return key[index] + "=>" + value[index];
		}
	}


	/** An iterator over a hash map. */
	private class MapIterator {
		/**
		 * The index of the last entry returned, if positive or zero; initially,
		 * {@link #n}. If negative, the last entry returned was that of the key
		 * of index {@code - pos - 1} from the {@link #wrapped} list.
		 */
		int pos = n;
		/**
		 * The index of the last entry that has been returned (more precisely,
		 * the value of {@link #pos} if {@link #pos} is positive, or
		 * {@link Integer#MIN_VALUE} if {@link #pos} is negative). It is -1 if
		 * either we did not return an entry yet, or the last returned entry has
		 * been removed.
		 */
		int last = -1;
		/**
		 * A downward counter measuring how many entries must still be returned.
		 */
		int c = size;
		/**
		 * A boolean telling us whether we should return the entry with the null
		 * key.
		 */
		boolean mustReturnNullKey = Long2ChunkMap.this.containsNullKey;
		/**
		 * A lazily allocated list containing keys of entries that have wrapped
		 * around the table because of removals.
		 */
		LongArrayList wrapped;

		public boolean hasNext() {
			return c != 0;
		}

		public int nextEntry() {
			if (!hasNext()) throw new NoSuchElementException();
			c--;
			if (mustReturnNullKey) {
				mustReturnNullKey = false;
				return last = n;
			}
			final long key[] = Long2ChunkMap.this.key;
			for (;;) {
				if (--pos < 0) {
					// We are just enumerating elements from the wrapped list.
					last = Integer.MIN_VALUE;
					final long k = wrapped.getLong(-pos - 1);
					int p = (int) HashCommon.mix((k)) & mask;
					while (!((k) == (key[p])))
						p = (p + 1) & mask;
					return p;
				}
				if (!((key[pos]) == (0))) return last = pos;
			}
		}

		/**
		 * Shifts left entries with the specified hash code, starting at the
		 * specified position, and empties the resulting free entry.
		 *
		 * @param pos
		 *            a starting position.
		 */
		private final void shiftKeys(int pos) {
			// Shift entries with the same hash.
			int last, slot;
			long curr;
			final long[] key = Long2ChunkMap.this.key;
			for (;;) {
				pos = ((last = pos) + 1) & mask;
				for (;;) {
					if (((curr = key[pos]) == (0))) {
						key[last] = (0);
						value[last] = null;
						return;
					}
					slot = (int) HashCommon.mix((curr)) & mask;
					if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
					pos = (pos + 1) & mask;
				}
				if (pos < last) { // Wrapped entry.
					if (wrapped == null) wrapped = new LongArrayList(2);
					wrapped.add(key[pos]);
				}
				key[last] = curr;
				value[last] = value[pos];
			}
		}

		public void remove() {
			if (last == -1) throw new IllegalStateException();
			if (last == n) {
				containsNullKey = false;
				value[n] = null;
			} else if (pos >= 0) shiftKeys(last);
			else {
				// We're removing wrapped entries.
				Long2ChunkMap.this.remove(wrapped.getLong(-pos - 1));
				last = -1; // Note that we must not decrement size
				return;
			}
			size--;
			last = -1; // You can no longer remove this entry.
			if (ASSERTS) checkTable();
		}

		public int skip(final int n) {
			int i = n;
			while (i-- != 0 && hasNext())
				nextEntry();
			return n - i - 1;
		}
	}

	private class EntryIterator extends MapIterator implements ObjectIterator<Entry<Chunk>> {
		private MapEntry entry;

		public Entry<Chunk> next() {
			return entry = new MapEntry(nextEntry());
		}

		@Override
		public void remove() {
			super.remove();
			entry.index = -1; // You cannot use a deleted entry.
		}
	}

	private class FastEntryIterator extends MapIterator implements ObjectIterator<Entry<Chunk>> {
		private final MapEntry entry = new MapEntry();

		public MapEntry next() {
			entry.index = nextEntry();
			return entry;
		}
	}

	private final class MapEntrySet extends AbstractObjectSet<Entry<Chunk>> implements FastEntrySet<Chunk> {
		public ObjectIterator<Entry<Chunk>> iterator() {
			return new EntryIterator();
		}

		public ObjectIterator<Entry<Chunk>> fastIterator() {
			return new FastEntryIterator();
		}

		@SuppressWarnings("unchecked")
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			if (e.getKey() == null || !(e.getKey() instanceof Long)) return false;
			final long k = ((((Long) (e.getKey())).longValue()));
			final Chunk v = ((Chunk) e.getValue());
			if (((k) == (0))) return Long2ChunkMap.this.containsNullKey && ((value[n]) == null ? (v) == null : (value[n]).equals(v));
			long curr;
			final long[] key = Long2ChunkMap.this.key;
			int pos;
			// The starting point.
			if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return false;
			if (((k) == (curr))) return ((value[pos]) == null ? (v) == null : (value[pos]).equals(v));
			// There's always an unused entry.
			while (true) {
				if (((curr = key[pos = (pos + 1) & mask]) == (0))) return false;
				if (((k) == (curr))) return ((value[pos]) == null ? (v) == null : (value[pos]).equals(v));
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean rem(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			if (e.getKey() == null || !(e.getKey() instanceof Long)) return false;
			final long k = ((((Long) (e.getKey())).longValue()));
			final Chunk v = ((Chunk) e.getValue());
			if (((k) == (0))) {
				if (containsNullKey && ((value[n]) == null ? (v) == null : (value[n]).equals(v))) {
					removeNullEntry();
					return true;
				}
				return false;
			}
			long curr;
			final long[] key = Long2ChunkMap.this.key;
			int pos;
			// The starting point.
			if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return false;
			if (((curr) == (k))) {
				if (((value[pos]) == null ? (v) == null : (value[pos]).equals(v))) {
					removeEntry(pos);
					return true;
				}
				return false;
			}
			while (true) {
				if (((curr = key[pos = (pos + 1) & mask]) == (0))) return false;
				if (((curr) == (k))) {
					if (((value[pos]) == null ? (v) == null : (value[pos]).equals(v))) {
						removeEntry(pos);
						return true;
					}
				}
			}
		}

		public int size() {
			return size;
		}

		public void clear() {
			Long2ChunkMap.this.clear();
		}
	}

	public FastEntrySet<Chunk> long2ObjectEntrySet() {
		if (entries == null) entries = new MapEntrySet();
		return entries;
	}

	/**
	 * An iterator on keys.
	 *
	 * <P>
	 * We simply override the
	 * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()}
	 * methods (and possibly their type-specific counterparts) so that they
	 * return keys instead of entries.
	 */
	private final class KeyIterator extends MapIterator implements LongIterator {
		public KeyIterator() {
			super();
		}

		public long nextLong() {
			return key[nextEntry()];
		}

		public Long next() {
			return (Long.valueOf(key[nextEntry()]));
		}
	}

	private final class KeySet extends AbstractLongSet {
		public LongIterator iterator() {
			return new KeyIterator();
		}

		public int size() {
			return size;
		}

		public boolean contains(long k) {
			return containsKey(k);
		}

		public boolean rem(long k) {
			final int oldSize = size;
			Long2ChunkMap.this.remove(k);
			return size != oldSize;
		}

		public void clear() {
			Long2ChunkMap.this.clear();
		}
	}

	public LongSet keySet() {
		if (keys == null) keys = new KeySet();
		return keys;
	}

	/**
	 * An iterator on values.
	 *
	 * <P>
	 * We simply override the
	 * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()}
	 * methods (and possibly their type-specific counterparts) so that they
	 * return values instead of entries.
	 */
	private final class ValueIterator extends MapIterator implements ObjectIterator<Chunk> {
		public ValueIterator() {
			super();
		}

		public Chunk next() {
			return value[nextEntry()];
		}
	}

	public ObjectCollection<Chunk> values() {
		if (values == null) values = new AbstractObjectCollection<Chunk>() {
			public ObjectIterator<Chunk> iterator() {
				return new ValueIterator();
			}

			public int size() {
				return size;
			}

			public boolean contains(Object v) {
				return containsValue(v);
			}

			public void clear() {
				Long2ChunkMap.this.clear();
			}
		};
		return values;
	}

	/**
	 * A no-op for backward compatibility. The kind of tables implemented by
	 * this class never need rehashing.
	 *
	 * <P>
	 * If you need to reduce the table size to fit exactly this set, use
	 * {@link #trim()}.
	 *
	 * @return true.
	 * @see #trim()
	 * @deprecated A no-op.
	 */
	@Deprecated
	public boolean rehash() {
		return true;
	}

	/**
	 * Rehashes the map, making the table as small as possible.
	 * 
	 * <P>
	 * This method rehashes the table to the smallest size satisfying the load
	 * factor. It can be used when the set will not be changed anymore, so to
	 * optimize access speed and size.
	 *
	 * <P>
	 * If the table size is already the minimum possible, this method does
	 * nothing.
	 *
	 * @return true if there was enough memory to trim the map.
	 * @see #trim(int)
	 */
	public boolean trim() {
		final int l = arraySize(size, f);
		if (l >= n || size > maxFill(l, f)) return true;
		try {
			rehash(l);
		} catch (OutOfMemoryError cantDoIt) {
			return false;
		}
		return true;
	}

	/**
	 * Rehashes this map if the table is too large.
	 * 
	 * <P>
	 * Let <var>N</var> be the smallest table size that can hold
	 * <code>max(n,{@link #size()})</code> entries, still satisfying the load
	 * factor. If the current table size is smaller than or equal to
	 * <var>N</var>, this method does nothing. Otherwise, it rehashes this map
	 * in a table of size <var>N</var>.
	 *
	 * <P>
	 * This method is useful when reusing maps. {@linkplain #clear() Clearing a
	 * map} leaves the table size untouched. If you are reusing a map many
	 * times, you can call this method with a typical size to avoid keeping
	 * around a very large table just because of a few large transient maps.
	 *
	 * @param n
	 *            the threshold for the trimming.
	 * @return true if there was enough memory to trim the map.
	 * @see #trim()
	 */
	public boolean trim(final int n) {
		final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / f));
		if (l >= n || size > maxFill(l, f)) return true;
		try {
			rehash(l);
		} catch (OutOfMemoryError cantDoIt) {
			return false;
		}
		return true;
	}

	/**
	 * Rehashes the map.
	 *
	 * <P>
	 * This method implements the basic rehashing strategy, and may be overriden
	 * by subclasses implementing different rehashing strategies (e.g.,
	 * disk-based rehashing). However, you should not override this method
	 * unless you understand the internal workings of this class.
	 *
	 * @param newN  the new size
	 */
	@SuppressWarnings("unchecked")
	protected void rehash(final int newN) {
		final long[] key = this.key;
		final Chunk[] value = this.value;
		final int mask = newN - 1; // Note that this is used by the hashing
									// macro
		final long[] newKey = new long[newN + 1];
		final Chunk[] newValue = new Chunk[newN + 1];
		int i = n, pos;
		for (int j = realSize(); j-- != 0;) {
			while (((key[--i]) == (0)))
				;
			if (!((newKey[pos = (int) HashCommon.mix((key[i])) & mask]) == (0))) while (!((newKey[pos = (pos + 1) & mask]) == (0)))
				;
			newKey[pos] = key[i];
			newValue[pos] = value[i];
		}
		newValue[newN] = value[n];
		n = newN;
		this.mask = mask;
		maxFill = maxFill(n, f);
		this.key = newKey;
		this.value = newValue;
	}

	/**
	 * Returns a deep copy of this map.
	 *
	 * <P>
	 * This method performs a deep copy of this hash map; the data stored in the
	 * map, however, is not cloned. Note that this makes a difference only for
	 * object keys.
	 *
	 * @return a deep copy of this map.
	 */
	@SuppressWarnings("unchecked")
	public Long2ChunkMap clone() {
		Long2ChunkMap c;
		try {
			c = (Long2ChunkMap) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.keys = null;
		c.values = null;
		c.entries = null;
		c.containsNullKey = containsNullKey;
		c.key = key.clone();
		c.value = value.clone();
		return c;
	}

	/**
	 * Returns a hash code for this map.
	 *
	 * This method overrides the generic method provided by the superclass.
	 * Since <code>equals()</code> is not overriden, it is important that the
	 * value returned by this method is the same value as the one returned by
	 * the overriden method.
	 *
	 * @return a hash code for this map.
	 */
	public int hashCode() {
		int h = 0;
		for (int j = realSize(), i = 0, t = 0; j-- != 0;) {
			while (((key[i]) == (0)))
				i++;
			t = HashCommon.long2int(key[i]);
			t ^= ((value[i]) == null ? 0 : (value[i]).hashCode());
			h += t;
			i++;
		}
		// Zero / null keys have hash zero.
		if (containsNullKey) h += ((value[n]) == null ? 0 : (value[n]).hashCode());
		return h;
	}


	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		final long[] key = this.key;
		final Chunk[] value = this.value;
		final MapIterator i = new MapIterator();
		s.defaultWriteObject();
		for (int j = size, e; j-- != 0;) {
			e = i.nextEntry();
			s.writeLong(key[e]);
			s.writeObject(value[e]);
		}
	}


	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		n = arraySize(size, f);
		maxFill = maxFill(n, f);
		mask = n - 1;
		final long[] key = this.key = new long[n + 1];
		final Chunk[] value = this.value = new Chunk[n + 1];
		long k;
		Chunk v;
		for (int i = size, pos; i-- != 0;) {
			k = s.readLong();
			v = (Chunk) s.readObject();
			if (((k) == (0))) {
				pos = n;
				containsNullKey = true;
			} else {
				pos = (int) HashCommon.mix((k)) & mask;
				while (!((key[pos]) == (0)))
					pos = (pos + 1) & mask;
			}
			key[pos] = k;
			value[pos] = v;
		}
		if (ASSERTS) checkTable();
	}

	private void checkTable() {
	}
}
