package org.xydra.index.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.IMapIndex;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.log.LoggerFactory;


/**
 * Allows only one entry per key. Keys are normalized to lowercase.
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class LowercaseStringKeyMapIndex<E> implements IMapIndex<String,E> {
	
	private static final long serialVersionUID = -5149093549283026560L;
	
	private Map<String,E> map = new HashMap<String,E>();
	
	static {
		LoggerFactory.getLogger(LowercaseStringKeyMapIndex.class).info(
		        "Using locale " + Locale.GERMAN.getDisplayLanguage() + "(iso: "
		                + Locale.GERMAN.getISO3Language()
		                + ") for comparing strings and evaluating search expressions.");
	}
	
	@Override
    public void clear() {
		this.map.clear();
	}
	
	@Override
    public boolean containsKey(String key) {
		return this.map.containsKey(this.normalise(key));
	}
	
	@Override
    public void deIndex(String key) {
		this.map.remove(this.normalise(key));
	}
	
	@Override
    public void index(String key, E entry) {
		this.map.put(this.normalise(key), entry);
	}
	
	@Override
    public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	@Override
    public Iterator<E> iterator() {
		return this.map.values().iterator();
	}
	
	@Override
    public E lookup(String key) {
		return this.map.get(this.normalise(key));
	}
	
	private String normalise(String key) {
		return key.toLowerCase(Locale.GERMAN);
	}
	
	@Override
    public boolean containsKey(Constraint<String> c1) {
		if(c1.isStar())
			return isEmpty();
		else {
			String key = ((EqualsConstraint<String>)c1).getKey();
			return this.map.containsKey(key);
		}
	}
	
	@Override
    public Iterator<KeyEntryTuple<String,E>> tupleIterator(Constraint<String> c1) {
		if(c1.isStar()) {
			return new AbstractTransformingIterator<Map.Entry<String,E>,KeyEntryTuple<String,E>>(
			        this.map.entrySet().iterator()) {
				
				@Override
				public KeyEntryTuple<String,E> transform(Entry<String,E> in) {
					return new KeyEntryTuple<String,E>(in.getKey(), in.getValue());
				}
				
			};
		}
		String key = ((EqualsConstraint<String>)c1).getKey();
		if(this.map.containsKey(key))
			return new SingleValueIterator<KeyEntryTuple<String,E>>(new KeyEntryTuple<String,E>(
			        key, this.map.get(key)));
		else
			return new NoneIterator<KeyEntryTuple<String,E>>();
		
	}
	
	@Override
    public Iterator<String> keyIterator() {
		return this.map.keySet().iterator();
	}
	
}
