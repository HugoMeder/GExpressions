package de.bitsnarts.g_expressions;

import java.io.Serializable;
import java.util.TreeMap;

public class LowerExtern implements Contraction, Comparable<LowerExtern>, Serializable {

	final Index extern;
	final Index intern;

	LowerExtern ( Index extern, Index intern ) {
		if ( !extern.extern )
			throw new Error ( "!extern.extern" ) ;
		if ( intern.extern )
			throw new Error ( "intern.extern" ) ;
		this.extern = extern ;
		this.intern = intern ;

	}

	@Override
	public boolean isContracting() {
		return true;
	}

	@Override
	public boolean bindsDerivation() {
		return intern.bindDerivation;
	}

	public LowerExtern copy(int i) {
		return new LowerExtern ( extern.copy ( i ), intern.copy(i));
	}

	public LowerExtern copy(TreeMap<Integer, Integer> map) {
		return new LowerExtern ( extern, intern.copy(map));
	}
	
	@Override
	public int compareTo(LowerExtern a ) {
		int cmp = extern.compareTo( a.extern ) ;
		if ( cmp != 0 )
			return cmp ;
		return intern.compareTo( a.intern);
	}
	
	@Override
	public boolean equals ( Object o ) {
		return compareTo ( (LowerExtern) o ) == 0 ;
	}

	public LowerExtern editDerivIndices(int[] p) {
		return new LowerExtern ( extern.copy(0), intern.editDerivIndices(p));
	}
	

	public String toString () {
		return extern.toString()+intern ;
	}


}
