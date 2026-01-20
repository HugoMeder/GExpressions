package de.bitsnarts.g_expressions;

import java.io.Serializable;
import java.util.TreeMap;

public class Index implements Comparable<Index>, Contraction, Serializable {
	public final boolean extern ;
	public final int index ;// extern or derivation factor index.
	public final boolean bindDerivation ;
	
	Index ( int index ) {
		extern = true ;
		this.index = index ;
		bindDerivation = false ;
	}
	
	Index ( int index, boolean bindDerivation ) {
		extern = false ;
		this.index = index ;
		this.bindDerivation = bindDerivation ;
	}
	

	@Override
	public int compareTo(Index arg0) {
		int cmp = ((Boolean)extern).compareTo ( arg0.extern) ;
		if ( cmp != 0 )
			return -cmp ;
		cmp = ((Integer)index).compareTo ( arg0.index) ;
		if ( cmp != 0 )
			return cmp ;
		if (!extern)
			return ((Boolean)bindDerivation).compareTo ( arg0.bindDerivation) ;
		return 0;
	}
	
	@Override
	public boolean equals ( Object obj ) {
		return compareTo ( (Index) obj ) == 0 ;
	}

	@Override
	public boolean isContracting() {
		return !extern ;
	}

	@Override
	public boolean bindsDerivation() {
		return bindDerivation;
	}

	Index copy(int internalOffset) {
		if ( extern )
			return this ;
		else {
			return new Index ( index+internalOffset, bindDerivation ) ;
		}
	}

	Index copy(TreeMap<Integer, Integer> map) {
		if ( extern )
			return this ;
		Integer ni = map.get( index ) ;
		if ( ni == null )
			return this ;
		return new Index ( ni, bindDerivation );
	}
	
	Index contract(TreeMap<Integer, LowerExtern> contr) {
		if ( !extern )
			return this ;
		LowerExtern e = contr.get(index) ;
		if ( e == null ) {
			return this ;
		}
		return new Index ( e.intern.index, e.intern.bindDerivation );
	}

	Index editDerivIndices(int[] p) {
		if ( extern )
			return this.copy( 0 ) ;
		return new Index ( p[index], bindDerivation );
	}
	
	public String toString () {
		if ( extern )
			return "["+index+"]" ;
		else {
			if ( bindDerivation )
				return "[>"+index+",]" ;
			else
				return "[>"+index+"]" ;
		}
	}


}
