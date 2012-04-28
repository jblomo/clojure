/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package clojure.lang;

import java.io.Serializable;
import java.util.*;

public abstract class APersistentMap extends AFn implements IPersistentMap, Map, Iterable, Serializable, MapEquivalence, IHashEq {
int _hash = -1;

public String toString(){
	return RT.printString(this);
}

public IPersistentCollection cons(Object o){
	if(o instanceof Map.Entry)
		{
		Map.Entry e = (Map.Entry) o;

		return assoc(e.getKey(), e.getValue());
		}
	else if(o instanceof IPersistentVector)
		{
		IPersistentVector v = (IPersistentVector) o;
		if(v.count() != 2)
			throw new IllegalArgumentException("Vector arg to map conj must be a pair");
		return assoc(v.nth(0), v.nth(1));
		}

	IPersistentMap ret = this;
	for(ISeq es = RT.seq(o); es != null; es = es.next())
		{
		Map.Entry e = (Map.Entry) es.first();
		ret = ret.assoc(e.getKey(), e.getValue());
		}
	return ret;
}

public boolean equals(Object obj){
	return mapEquals(this, obj);
}

static public boolean mapEquals(IPersistentMap m1, Object obj){
	if(m1 == obj) return true;
	if(!(obj instanceof Map))
		return false;
	Map m = (Map) obj;

	if(m.size() != m1.count())
		return false;

	for(ISeq s = m1.seq(); s != null; s = s.next())
		{
		Map.Entry e = (Map.Entry) s.first();
		boolean found = m.containsKey(e.getKey());

		if(!found || !Util.equals(e.getValue(), m.get(e.getKey())))
			return false;
		}

	return true;
}

public boolean equiv(Object obj){
	if(!(obj instanceof Map))
		return false;
	if(obj instanceof IPersistentMap && !(obj instanceof MapEquivalence))
		return false;
	
	Map m = (Map) obj;

	if(m.size() != size())
		return false;

	for(ISeq s = seq(); s != null; s = s.next())
		{
		Map.Entry e = (Map.Entry) s.first();
		boolean found = m.containsKey(e.getKey());

		if(!found || !Util.equiv(e.getValue(), m.get(e.getKey())))
			return false;
		}

	return true;
}
public int hashCode(){
	if(_hash == -1)
		{
		this._hash = mapHash(this);
		}
	return _hash;
}

static public int mapHash(IPersistentMap m){
	int hash = 0;
	for(ISeq s = m.seq(); s != null; s = s.next())
		{
		Map.Entry e = (Map.Entry) s.first();
		hash += (e.getKey() == null ? 0 : e.getKey().hashCode()) ^
				(e.getValue() == null ? 0 : e.getValue().hashCode());
		}
	return hash;
}

public int hasheq(){
	int hash = 0;
	for(ISeq s = this.seq(); s != null; s = s.next())
		{
		Map.Entry e = (Map.Entry) s.first();
		hash += Util.hasheq(e.getKey()) ^
				Util.hasheq(e.getValue());
		}
	return hash;
}

static public class KeySeq extends ASeq{
	ISeq seq;

	static public KeySeq create(ISeq seq){
		if(seq == null)
			return null;
		return new KeySeq(seq);
	}

	private KeySeq(ISeq seq){
		this.seq = seq;
	}

	private KeySeq(IPersistentMap meta, ISeq seq){
		super(meta);
		this.seq = seq;
	}

	public Object first(){
		return ((Map.Entry) seq.first()).getKey();
	}

	public ISeq next(){
		return create(seq.next());
	}

	public KeySeq withMeta(IPersistentMap meta){
		return new KeySeq(meta, seq);
	}
}

static public class ValSeq extends ASeq{
	ISeq seq;

	static public ValSeq create(ISeq seq){
		if(seq == null)
			return null;
		return new ValSeq(seq);
	}

	private ValSeq(ISeq seq){
		this.seq = seq;
	}

	private ValSeq(IPersistentMap meta, ISeq seq){
		super(meta);
		this.seq = seq;
	}

	public Object first(){
		return ((Map.Entry) seq.first()).getValue();
	}

	public ISeq next(){
		return create(seq.next());
	}

	public ValSeq withMeta(IPersistentMap meta){
		return new ValSeq(meta, seq);
	}
}


public Object invoke(Object arg1) {
	return valAt(arg1);
}

public Object invoke(Object arg1, Object notFound) {
	return valAt(arg1, notFound);
}

// java.util.Map implementation

public void clear(){
	throw new UnsupportedOperationException();
}

public boolean containsValue(Object value){
	return values().contains(value);
}

public Set entrySet(){
	return new AbstractSet(){

		public Iterator iterator(){
			return APersistentMap.this.iterator();
		}

		public int size(){
			return count();
		}

		public int hashCode(){
			return APersistentMap.this.hashCode();
		}

		public boolean contains(Object o){
			if(o instanceof Entry)
				{
				Entry e = (Entry) o;
				Entry found = entryAt(e.getKey());
				if(found != null && Util.equals(found.getValue(), e.getValue()))
					return true;
				}
			return false;
		}
	};
}

public Object get(Object key){
	return valAt(key);
}

public boolean isEmpty(){
	return count() == 0;
}

public Set keySet(){
	return new AbstractSet(){

		public Iterator iterator(){
			final Iterator mi = APersistentMap.this.iterator();

			return new Iterator(){


				public boolean hasNext(){
					return mi.hasNext();
				}

				public Object next(){
					Entry e = (Entry) mi.next();
					return e.getKey();
				}

				public void remove(){
					throw new UnsupportedOperationException();
				}
			};
		}

		public int size(){
			return count();
		}

		public boolean contains(Object o){
			return APersistentMap.this.containsKey(o);
		}
	};
}

public Object put(Object key, Object value){
	throw new UnsupportedOperationException();
}

public void putAll(Map t){
	throw new UnsupportedOperationException();
}

public Object remove(Object key){
	throw new UnsupportedOperationException();
}

public int size(){
	return count();
}

public Collection values(){
	return new AbstractCollection(){

		public Iterator iterator(){
			final Iterator mi = APersistentMap.this.iterator();

			return new Iterator(){


				public boolean hasNext(){
					return mi.hasNext();
				}

				public Object next(){
					Entry e = (Entry) mi.next();
					return e.getValue();
				}

				public void remove(){
					throw new UnsupportedOperationException();
				}
			};
		}

		public int size(){
			return count();
		}
	};
}

/*
// java.util.Collection implementation

public Object[] toArray(){
	return RT.seqToArray(seq());
}

public boolean add(Object o){
	throw new UnsupportedOperationException();
}

public boolean remove(Object o){
	throw new UnsupportedOperationException();
}

public boolean addAll(Collection c){
	throw new UnsupportedOperationException();
}

public void clear(){
	throw new UnsupportedOperationException();
}

public boolean retainAll(Collection c){
	throw new UnsupportedOperationException();
}

public boolean removeAll(Collection c){
	throw new UnsupportedOperationException();
}

public boolean containsAll(Collection c){
	for(Object o : c)
		{
		if(!contains(o))
			return false;
		}
	return true;
}

public Object[] toArray(Object[] a){
	if(a.length >= count())
		{
		ISeq s = seq();
		for(int i = 0; s != null; ++i, s = s.rest())
			{
			a[i] = s.first();
			}
		if(a.length > count())
			a[count()] = null;
		return a;
		}
	else
		return toArray();
}

public int size(){
	return count();
}

public boolean isEmpty(){
	return count() == 0;
}

public boolean contains(Object o){
	if(o instanceof Map.Entry)
		{
		Map.Entry e = (Map.Entry) o;
		Map.Entry v = entryAt(e.getKey());
		return (v != null && Util.equal(v.getValue(), e.getValue()));
		}
	return false;
}
*/
public class SubPersistentTreeMap extends APersistentMap implements IObj, Reversible, Sorted, SortedMap{
	final IPersistentMap m; // must be Sorted
	final Object start;
	final Object end;
	final IPersistentMap _meta;

	public SubPersistentTreeMap(IPersistentMap meta, IPersistentMap m, Object start, Object end){
		this._meta = meta;

		if(!(m implements Sorted)) {
			throw new IllegalArgumentException("IPersistentMap arg to SubPersistentTreeMap must be Sorted");
		}

		if(m instanceof APersistentMap.SubPersistentTreeMap) {
			APersistentMap.SubPersistentTreeMap sm = (APersistentMap.SubPersistentTreeMap) m;
			m = sm.m;
		}
		this.m = m;
		this.start = start;
		this.end = end;
	}

	private withinSubRange(Object key){
		return (m.doCompare(key, start) >= 0) &&
			((end == null) || (m.doCompare(key, end) < 0));
	}

	public boolean containsKey(Object key){
		return entryAt(key) != null;
	}

	public Node entryAt(Object key){
		return withinSubRange(key) ? m.entryAt(key) : null;
	}

	public IPersistentMap assocEx(Object key, Object val) {
		if(withinSubRange(key))
			return new SubPersistentTreeMap(meta(), m.assocEx(key, val), start, end);

		// else we're not inserting between the range, so we no longer have a submap
		return PersistentTreeMap.create(comparator(), seq(true)).assocEx(key, val);
	}

	public PersistentTreeMap assoc(Object key, Object val){
		if(withinSubRange(key))
			return new SubPersistentTreeMap(meta(), m.assoc(key, val), start, end);

		// else we're not inserting between the range, so we no longer have a submap
		return PersistentTreeMap.create(comparator(), seq(true)).assocEx(key, val);
	}

	public PersistentTreeMap without(Object key){
		if(withinSubRange(key))
			return new SubPersistentTreeMap(meta(), m.without(key), start, end);

		return this;
	}

	public IPersistentCollection empty(){
		return m.empty();
	}

	public ISeq rseq() {
		return seqFrom(start, false);
	}

	public Comparator comparator(){
		return m.comp;
	}

	public Object firstKey(){
		// TODO binary search
		return RT.first(seqFrom(start, true)).key();
	}

	public SortedMap headMap(Object toKey){
		if((end == null) || (m.doCompare(toKey, end) >= 0))
			return this;

		return new SubPersistentTreeMap(meta(), m, start, toKey);
	}

	public Object lastKey(){
		if(end)
			// TODO binary search
			return RT.first(rseq()).key();

		return m.lastKey();
	}

	public SortedMap subMap(Object fromKey, Object toKey){
		Object newStart = m.doCompare(fromKey, start) > 0 ? fromKey : start;
		Object newEnd = ((end == null) || (m.doCompare(toKey, end))) < 0 ? toKey : end;

		if(m.doCompare(newEnd, newStart) <= 0)
			return m.empty();

		return new SubPersistentTreeMap(meta(), this, newStart, newEnd);
	}

	public SortedMap tailMap(Object fromKey){
		if(m.doCompare(fromKey, start) <= 0)
			return this;

		return new SubPersistentTreeMap(meta(), this, fromKey, end);
	}

	public ISeq seq(boolean ascending){
		return seqFrom(start, ascending);
	}

	public ISeq seqFrom(Object key, boolean ascending){
		if(ascending && (end != null) && (m.doCompare(key, end) >= 0))
			return null;

		if(!ascending && m.doCompare(key, start) < 0)
			return null;

		if((m._count > 0)) {

			ISeq stack = null;
			Node t = m.tree;
			while(t != null) {
				int c = m.doCompare(key, t.key);
				if(c == 0) {
					stack = RT.cons(t, stack);
					return new Seq(stack, ascending);
				}
				else if(ascending) {
					if((end != null) && (m.doCompare(t.key, end) >= 0)) {
						stack = RT.cons(t, stack);
						return new Seq(stack, ascending);
					} else if(c < 0) {
						stack = RT.cons(t, stack);
						t = t.left();
					}
					else
						t = t.right();

				} else { // descending
					if(m.doCompare(start, t.key) < 0) {
						stack = RT.cons(t, stack);
						return new Seq(stack, ascending);
					} else {
						if(c > 0) {
							stack = RT.cons(t, stack);
							t = t.right();
						} else
							t = t.left();
					}
				}
			}
			if(stack != null)
				return new Seq(stack, ascending);
		}
		return null;
	}

	public Iterator iterator(){
		return seq(true).iterator();
	}

	public NodeIterator reverseIterator(){
		return rseq(true).iterator();
	}

	public Iterator keys(){
		return keys(iterator());
	}

	public Iterator vals(){
		return vals(iterator());
	}

	public Object valAt(Object key, Object notFound){
		return withinSubRange(key) ? m.valAt(key, notFound) : notFound;
	}

	public Object valAt(Object key){
		return withinSubRange(key) ? m.valAt(key) : null;
	}

	public int count(){
		// TODO memoize?
		return seq(true).count();
	}
}
}
// TODO enclose SubPersistentTreeMap in scope
