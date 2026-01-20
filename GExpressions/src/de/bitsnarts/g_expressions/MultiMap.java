package de.bitsnarts.g_expressions;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class MultiMap <K extends Comparable<K>,T> {

	TreeMap<K,Vector<T>> m = new TreeMap<K,Vector<T>>() ;
	
	public void put ( K key, T value ) {
		Vector<T> v = m.get(key) ;
		if ( v == null ) {
			v = new Vector<> () ;
			m.put(key, v) ;
		}
		v.add(value) ;
	}
	
	public List<T> get ( K key ) {
		return m.get(key );
	}
	
	public Set<K> keySet () {
		return m.keySet() ;
	}
	
	public Set<Entry<K, Vector<T>>> entrySet () {
		return m.entrySet() ;
	}
	
	public Collection<Vector<T>> values () {
		return m.values() ;
	}
}
