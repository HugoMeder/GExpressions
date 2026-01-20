package de.bitsnarts.g_expressions;

import java.io.Serializable;
import java.util.TreeMap;

public class GUpper implements Comparable<GUpper>, Serializable {

	final Index i1;
	final Index i2;

	GUpper ( Index i1, Index i2 ){
		if ( i1.compareTo(i2) < 0 ) {
			this.i1 = i2 ;
			this.i2 = i1 ;
		} else {
			this.i1 = i1 ;
			this.i2 = i2 ;
		}
	}

	public GUpper ( int i1, int i2 ) {
		this ( new Index ( i1 ), new Index ( i2 ));
	}
	
	GUpper ( int i1, int i2, boolean bindDerivation ) {
		this ( new Index ( i1 ), new Index ( i2, bindDerivation ));
	}

	GUpper ( int i1, boolean bindDerivation1, int i2, boolean bindDerivation2 ) {
		this ( new Index ( i1, bindDerivation1 ), new Index ( i2, bindDerivation2 ));
	}

	@Override
	public int compareTo(GUpper o) {
		int cmp = i1.compareTo(o.i1) ;
		if ( cmp != 0 )
			return cmp ;
		return i2.compareTo(o.i2) ;
	}
	
	public GUpper contract(TreeMap<Integer, LowerExtern> contr) {
		return new GUpper ( i1.contract ( contr ), i2.contract ( contr ) );
	}

	@Override
	public boolean equals ( Object obj ) {
		return compareTo ( (GUpper) obj ) == 0 ;
	}

	GUpper copy(int internalOffset ) {
		Index i1_ = i1.copy ( internalOffset ) ;
		Index i2_ = i2.copy ( internalOffset ) ;
		return new GUpper ( i1_, i2_ ) ;
	}

	GUpper copy(TreeMap<Integer, Integer> map) {
		return new GUpper ( i1.copy(map), i2.copy(map));
	}
	
	public GUpper editDerivIndices(int[] p) {
		return new GUpper ( i1.editDerivIndices (p), i2.editDerivIndices (p) ) ;
	}

	public String toString () {
		return "g"+i1+i2 ;
	}

	public Index getOtherIndex(Index index ) {
		if ( i1.compareTo(index) == 0 )
			return i2 ;
		else {
			if ( i2.compareTo(index) != 0 )
				throw new Error ( "index not present" ) ;
			return i1 ;
		}
	}


}
