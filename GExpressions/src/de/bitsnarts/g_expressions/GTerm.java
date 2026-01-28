package de.bitsnarts.g_expressions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

class ContractAllIndices {

	private GTerm rv;
	private int noDerivIndex;

	class ObjectsWithNoDerivIndex {

		Vector<GUpper> gus = new Vector<GUpper> () ;
		Vector<LowerExtern> les = new Vector<LowerExtern> () ;
		Vector<Integer> gusi = new Vector<Integer> () ;
		Vector<Integer> lesi = new Vector<Integer> () ;

		public void add(GUpper g, int index ) {
			gus.add(g) ;
			gusi.add(index) ;
		}

		public void add(LowerExtern le, int index ) {
			les.add(le) ;
			lesi.add(index) ;
		}
		
		void check () {
			if ( gus.size()+les.size() != 2 ) {
				throw new Error ( "gus.size()+les.size() != 2" ) ;
			}
		}
	}
	
	class NewDerivs {

		int[] derivs ;
		TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer> () ;
		
		NewDerivs ( ) {
			derivs = new int[rv.derivs.length-1] ;
			int dstIndex = 0 ;
			int srcIndex = 0 ;
			for ( int i : rv.derivs_ ) {
				map.put( srcIndex, dstIndex ) ;
				if ( srcIndex != noDerivIndex ) {
					derivs[dstIndex] = i ;
					dstIndex++ ;
				}
				srcIndex++ ;
			}
		}
	}
	
	ContractAllIndices(GTerm gTerm) {
		this.rv = gTerm.contractExternals() ;
		Vector<Integer> noDerivs = getlGlNoDerivs () ;
		while ( !noDerivs.isEmpty() ) {
			boolean changed = false ;
			for ( int i : noDerivs ) {
				if ( handleNoDeriv ( i ) ) {
					noDerivs = getlGlNoDerivs () ;
					changed = true ;
					break ;
				}
			}
			if ( !changed )
				return ;
		}
	}

	private boolean handleNoDeriv( int noDerivIndex ) {
		this.noDerivIndex = noDerivIndex ;
		Index ndi = new Index ( noDerivIndex, false ) ;
		ObjectsWithNoDerivIndex owndi = getObjectsWithNoDerivIndex (noDerivIndex) ;
		if ( owndi.gus.size() == 2 ) {
			if ( owndi.gus.get(0) == owndi.gus.get(1) ) {
				// g^{\mu\nu}g_{\mu\nu} keep it
				return false ;
			}
			// g^{\mu\alpha}g^{\nu\beta}g_{\mu\nu} = g^{\alpha\beta}
			GUpper g1 = owndi.gus.get(0) ;
			GUpper g2 = owndi.gus.get(1) ;
			Index alpha = g1.getOtherIndex(ndi) ;
			Index beta = g2.getOtherIndex(ndi) ;
			rv = handleGGG ( owndi.gusi.get(0), owndi.gusi.get( 1 ), new GUpper ( alpha, beta ) ) ;
		} else if ( owndi.gus.size() == 1 ) {
			// g^{\mu\alpha}g_{\mu\beta}=\delta^\alpha_\beta
			GUpper gu = owndi.gus.get( 0 ) ;
			int gui = owndi.gusi.get(0) ;
			Index alpha = gu.getOtherIndex(ndi) ;
			LowerExtern betaLe = owndi.les.get(0) ;
			int betaLei = owndi.lesi.get( 0 ) ;
			Index beta = betaLe.extern ;
			if ( !rv.upperExternIndices.contains(beta.index) ) {
				if ( !rv.lowerExternIndices.contains( beta.index) )
					return false ;
				if ( alpha.extern ) {
					return false ;
				}
				int lowerBetai = getLE ( beta ) ;
				LowerExtern lowerBeta = rv.lext.get( lowerBetai ) ;
				int removeLe = lowerBetai ;
				LowerExtern delta = new LowerExtern ( beta, alpha ) ;
				rv = handleGL ( gui, removeLe, delta ) ;
			} else {
				int upperBetai = getGU ( beta ) ;
				GUpper upperBeta = rv.gs.get(upperBetai) ;
				Index delta = upperBeta.getOtherIndex ( beta ) ;
				// g^{\beta\delta}g^{\mu\alpha}g_{\mu\beta}=g^{\beta\delta}\delta^\alpha_\beta=g^{\alpha\delta}
				GUpper gAlphaDelta = new GUpper ( alpha, delta ) ;
				rv = handleGGG ( gui, upperBetai, gAlphaDelta) ;
			}
		} else {
			// g_{\mu\nu}, both extern
			LowerExtern mu = owndi.les.get ( 0 ) ;
			int mui = owndi.lesi.get(0 ) ;
			LowerExtern nu = owndi.les.get ( 1 ) ;
			int nui = owndi.lesi.get(1 ) ;
			if ( !rv.upperExternIndices.contains( mui ) &&
					!rv.upperExternIndices.contains( nui ) )
				return false ;
			if ( !rv.upperExternIndices.contains( mui) ) {
				// g^{\nu\alpha}g_{\mu\nu} = \delta^alpha_\mu
				TreeSet<Integer> s = new TreeSet<Integer> () ;
				s.add( nui ) ;
				rv = contract ( s ) ;
			} else if ( !rv.upperExternIndices.contains( nui) ) {
					// g^{\mu\alpha}g_{\mu\nu} = \delta^alpha_\nu
					TreeSet<Integer> s = new TreeSet<Integer> () ;
					s.add( mui ) ;
					rv = contract ( s ) ;
			} else {
				// g^{\mu\alpha}g^{\mu\beta}g_{\mu\nu} = \delta^alpha_\nu
				TreeSet<Integer> s = new TreeSet<Integer> () ;
				s.add( mui ) ;
				s.add( nui ) ;
				rv = contract ( s ) ;
			}
		}
		return true ;
	}
	
	private GTerm handleGL(int remofeG, int removeLe, LowerExtern delta) {
		NewDerivs nds = new NewDerivs () ;
		GTerm newrv = new GTerm ( rv.factor, nds.derivs ) ;
		int index = 0 ;
		for ( GUpper g : rv.gs ) {
			if ( index != remofeG )
				newrv.add( g.copy( nds.map) );
			index++ ;
		}
		index = 0 ;
		for ( LowerExtern le : rv.lext ) {
			if ( index != removeLe ) {
				newrv.add( le.copy( nds.map));
			}
			index++ ;
		}
		newrv.add( delta.copy( nds.map));
		newrv.finish();
		return newrv;
	}

	GTerm contract ( TreeSet<Integer> common ) {
		TreeMap<Integer,LowerExtern> contr = new TreeMap<Integer,LowerExtern> () ;
		for ( LowerExtern l : rv.lext ) {
			if ( common.contains(l.extern.index ) )
				contr.put( l.extern.index, l ) ;
		}
		GTerm rv_ = new GTerm ( rv.factor, rv.derivs_ ) ;
		for ( GUpper g : rv.gs ) {
			rv_.add ( g.contract ( contr ) ) ;
		}
		for ( LowerExtern e : rv.lext ) {
			if ( !common.contains( e.extern.index ) ) {
				rv_.add( e );
			}
		}
		rv_.finish();
		return rv_ ;
	}

	private int getGU(Index extern) {
		int index = 0 ;
		for ( GUpper g : rv.gs ) {
			if ( g.i1.compareTo(extern) == 0 )
				return index ;
			if ( g.i2.compareTo(extern) == 0 )
				return index ;
			index++ ;
		}
		throw new Error ( "gu not found" ) ;
	}

	private int getLE(Index beta) {
		int index = 0 ;
		for ( LowerExtern le : rv.lext ) {
			if ( le.extern.compareTo(beta) == 0 )
				return index ;
			index++ ;
		}
		throw new Error ( "le not found" ) ;
	}


	private GTerm handleGGG(Integer g1Index, Integer g2Index, GUpper gAlphaBeta ) {
		NewDerivs nds = new NewDerivs () ;
		GTerm newrv = new GTerm ( rv.factor, nds.derivs ) ;
		int index = 0 ;
		for ( GUpper g : rv.gs ) {
			if ( index != g1Index && index != g2Index ) {
				newrv.add( g.copy( nds.map) );
			}
			index++ ;
		}
		newrv.add( gAlphaBeta.copy( nds.map) );
		for ( LowerExtern le : rv.lext ) {
			newrv.add( le.copy( nds.map) );
		}
		newrv.finish();
		return newrv;
	}

	private ObjectsWithNoDerivIndex getObjectsWithNoDerivIndex(int noDerivIndex) {
		ObjectsWithNoDerivIndex rv_ = new ObjectsWithNoDerivIndex () ;
		Index ind = new Index ( noDerivIndex, false ) ;
		int index = 0 ;
		for ( GUpper g : rv.gs ) {
			if ( g.i1.compareTo ( ind ) == 0 )
				rv_.add ( g, index ) ;
			if ( g.i2.compareTo( ind ) == 0 )
				rv_.add ( g, index ) ;
			index++ ;
		}
		index = 0 ;
		for ( LowerExtern le : rv.lext ) {
			if ( le.intern.compareTo(ind) == 0 )
				rv_.add( le, index );
			index++ ;
		}
		rv_.check();
		return rv_;
	}

	private Vector<Integer> getlGlNoDerivs() {
		Vector<Integer> v = new Vector<Integer> () ;
		int index = 0 ;
		for ( int i : rv.derivs_ ) {
			if ( i == 0 )
				v.add( index ) ;
			index++ ;
		}
		return v ;
	}

	public GTerm getRV() {
		return rv;
	}
	
}

public class GTerm implements Comparable<GTerm>, Serializable {
	double factor ;
	Vector<GUpper> gs = new Vector<GUpper> () ;
	Vector<LowerExtern> lext = new Vector<LowerExtern> () ;
	Derivation[] derivs ;
	int[] derivs_;
	TreeSet<Integer> upperExternIndices = new TreeSet<Integer> () ;
	TreeSet<Integer> lowerExternIndices = new TreeSet<Integer> () ;
	private boolean ordered;
	private boolean finished;
	private boolean canonic;
	
	GTerm ( double factor, int ... derivs_ ) {
		this.factor = factor ;
		this.derivs_ = derivs_ ;
		int n = derivs_.length ;
		derivs = new Derivation[n] ;
		for ( int i = 0 ; i < n ; i++ ) {
			derivs[i] = new Derivation ( derivs_[i] ) ;
		}
	}
	
	GTerm contractExternals() {
		@SuppressWarnings("unchecked")
		TreeSet<Integer> common = (TreeSet<Integer>) upperExternIndices.clone() ;
		common.retainAll ( lowerExternIndices ) ;
		if ( common.isEmpty() )
			return this ;
		TreeMap<Integer,LowerExtern> contr = new TreeMap<Integer,LowerExtern> () ;
		for ( LowerExtern l : lext ) {
			if ( common.contains(l.extern.index ) )
				contr.put( l.extern.index, l ) ;
		}
		GTerm rv = new GTerm ( factor, derivs_ ) ;
		for ( GUpper g : gs ) {
			rv.add ( g.contract ( contr ) ) ;
		}
		for ( LowerExtern e : lext ) {
			if ( !common.contains( e.extern.index ) ) {
				rv.add( e );
			}
		}
		rv.finish();
		return rv;
	}

	void add ( GUpper g ) {
		if ( finished )
			throw new Error ( "finished") ;
		gs.add(g) ;
		add ( g.i1, upperExternIndices ) ;
		add ( g.i2, upperExternIndices ) ;
	}

	void add (LowerExtern le ) {
		if ( finished )
			throw new Error ( "finished") ;
		lext.add(le) ;
		add ( le.extern, lowerExternIndices ) ;
		add ( le.intern, lowerExternIndices ) ;
	}
	
	private void add(Index i, TreeSet<Integer> externalIndexSet) {
		if (!i.isContracting()) {
			boolean added = externalIndexSet.add( i.index ) ;
			if ( !added ) {
				throw new Error ( "duplicated external index "+i.index ) ;
			}
			return ;
		}
		derivs[i.index].add(i);
	}
	
	public GTerm times ( GTerm t ) {
		int[] c = concat ( derivs_, t.derivs_ ) ;
		GTerm rv = new GTerm ( factor*t.factor, c ) ;
		for ( GUpper g : gs ) {
			rv.add(g.copy( 0 ));
		}
		int offs = derivs.length ;
		for ( GUpper g : t.gs ) {
			rv.add(g.copy( offs ));
		}
		for ( LowerExtern e : lext ) {
			rv.add ( e.copy ( 0 ) ) ;
		}
		for ( LowerExtern e : t.lext ) {
			rv.add ( e.copy ( offs ) ) ;
		}
		rv.finish () ;
		return rv.contractAllIndices () ;
	}

	public GExpression times ( GExpression e ) {
		return e.times(this) ;
	}
	
	void finish() {
		if ( finished )
			return ;
		for ( Derivation d : derivs ) {
			if ( !d.isComplete() )
				throw new Error ( "!d.isComplete()" ) ;
		}
		finished = true ;
	}

	private int[] concat(int[] derivs1, int[] derivs2) {
        int n1 = derivs1.length ;
        int n2 = derivs2.length ;
        int[] rv = new int[n1+n2] ;
        for ( int i = 0 ; i < n1 ; i++ ) {
        	rv[i] = derivs1[i] ;
        }
        for ( int i = 0 ; i < n2 ; i++ ) {
        	rv[i+n1] = derivs2[i] ;
        }
		return rv ;
	}
	
	public GExpression derivate ( int newExternalLowerIndex ) {

		GExpression rv = new GExpression () ;
		int ng = gs.size() ;
		int o = derivs_.length ;
		int[] d2 = new int[o+1] ;
		for ( int i = 0 ; i < o ; i++ ) {
			d2[i] = derivs_[i] ;
		}
		d2[o] = 1 ;
		// derivation of the gs
		for ( int gInd = 0 ; gInd < ng ; gInd++ ) {
			GTerm t = new GTerm ( -factor, d2 ) ;
			for ( int gInd2 = 0 ; gInd2 < ng ; gInd2++ ) {
				GUpper g = gs.elementAt(gInd2) ;
				if ( gInd2 != gInd ) {
					t.add( g.copy ( 0 ) );
				} else {
					t.add( new GUpper ( g.i1.copy(0), new Index ( o, false) ));
					t.add( new GUpper ( g.i2.copy(0), new Index ( o, false) ));
				}
			}
			for ( LowerExtern e : lext ) {
				t.add( e.copy(0));
			}
			t.add( new LowerExtern ( new Index (newExternalLowerIndex), new Index ( o, true ) ) );
			t.finish();
			t = t.contractAllIndices() ;
			rv.add(t);
		}
		// derivations of derivations
		int nd = derivs_.length ;
		for ( int d = 0 ; d < nd ; d++ ) {
			d2 = new int[o] ;
			for ( int i = 0 ; i < nd ; i++ ) {
				if ( i == d ) {
					d2[i] = derivs_[i]+1 ;
				} else {
					d2[i] = derivs_[i] ;
				}
			}
			GTerm t = new GTerm ( factor, d2 ) ;
			for ( GUpper g : gs ) {
				t.add( g.copy ( 0 ) );
			}
			for ( LowerExtern e : lext ) {
				t.add( e.copy ( 0 ));
			}
			t.add( new LowerExtern ( new Index ( newExternalLowerIndex), new Index ( d, true ) ));
			t.finish();
			t = t.contractAllIndices() ;
			rv.add(t);
		}
		return rv ;
	}
	
	GTerm order () {
		if ( ordered )
			return this ;
		int[] p = makeDerivPermutaion () ;
		return applyDerivPermutation ( p ) ;
	}
	
 GTerm applyDerivPermutation(int[] p) {
	int[] pi = new int[derivs_.length] ;
	for ( int i = 0 ; i < derivs_.length ; i++ ) {
		pi[p[i]] = i ;
	}
	int[] ds = new int[derivs_.length] ;
	for ( int i = 0 ; i<derivs_.length ; i++ ) {
		ds[i] = derivs_[p[i]] ;
	}
	MultiMap<GUpper, GUpper> m = new MultiMap<GUpper,GUpper> () ;
	for ( GUpper g : gs ) {
		GUpper g_ = g.editDerivIndices ( pi ) ;
		m.put(g_, g_ );
	}
	GTerm rv = new GTerm ( factor, ds ) ;
	for ( Vector<GUpper> v : m.values() ) {
		for ( GUpper g : v ) {
			rv.add(g);
		}
	}
	MultiMap<LowerExtern, LowerExtern> lem = new MultiMap<LowerExtern,LowerExtern> () ;
	for ( LowerExtern le : lext ) {
		lem.put(le, le.editDerivIndices ( pi ) );
	}
	for ( Vector<LowerExtern> v : lem.values() ) {
		for ( LowerExtern g : v ) {
			rv.add(g);
		}
	}
	rv.finish();
	rv.ordered = true ;
	return rv;
	}

private int[] makeDerivPermutaion() {
	MultiMap<Integer,Integer> m = new MultiMap<Integer,Integer> () ;
	int index = 0 ;
	for ( int d : derivs_ ) {
		m.put ( d, index++ ) ;
	}
	int perm[] = new int[derivs_.length] ;
	//boolean notIdent = false ;
	index = 0 ;
	for ( Integer k : m.keySet() ) {
		List<Integer> v = m.get(k) ;
		for ( Integer j : v ) {
			//if ( j != index )
			//	notIdent = true ;
			perm[index++] = j ;
		}
	}
	//if ( notIdent ) 
	//	return perm ;
	return perm ;
}

@Override
public String toString () {
	return Printer.toString(this ) ;
}

@Override
public int compareTo(GTerm o) {
	int cmp = 0 ;
	if ( ! ordered )
		throw new Error ( "! ordered") ;
	if ( ! o.ordered )
		throw new Error ( "! o.ordered") ;
	cmp = getLargestDegree()-o.getLargestDegree() ;
	if ( cmp != 0 )
		return -cmp ;
	if ( derivs_.length != o.derivs_.length )
		return derivs_.length-o.derivs_.length ;
	if ( gs.size() != o.gs.size() )
		return gs.size()-o.gs.size() ;
	if ( lext.size() != o.lext.size() )
		return lext.size()-o.lext.size() ;
	int n = derivs_.length ;

	for ( int i = 0 ; i < n ; i++ ) {
		cmp = derivs_[i]-o.derivs_[i] ;
		if ( cmp != 0 )
			return cmp ;
	}
	n = gs.size() ;
	for ( int i = 0 ; i < n ; i++ ) {
		cmp = gs.elementAt(i).compareTo( o.gs.elementAt( i ) ) ;
		if ( cmp != 0 )
			return cmp ;
	}
	n = lext.size() ;
	for ( int i = 0 ; i < n ; i++ ) {
		cmp = lext.elementAt(i).compareTo( o.lext.elementAt( i ) ) ;
		if ( cmp != 0 )
			return cmp ;
	}
	return 0;
}

@Override
public boolean equals(Object o) {
	return compareTo ( (GTerm) o )== 0 ;
}

public GTerm canonic() {
	if ( canonic ) {
		return this ;
	}
	//Permutation p = new Permutation () ;
	GTerm rv = order () ;
	GTerm orig = rv ;
	int[][] pp = rv.makeDerivSymmetryPrmutation () ;
	if ( pp == null ) {
		rv.canonic = true ;
		return rv ;
	}
	for ( int p[] : pp ) {
		GTerm t = orig.applyDerivPermutation(p) ;
		if ( t.compareTo(rv) < 0 ) {
			rv = t ;
		}
	}
	rv.canonic = true ;
	return rv ;
}

private int[][] makeDerivSymmetryPrmutation() {
	MultiMap<Integer, Integer> mm = new MultiMap<Integer,Integer> () ;
	int index = 0 ;
	int nd = derivs_.length ;
	for ( int i : derivs_ ) {
		mm.put(i, index );
		index++ ;
	}
	Vector<Integer> perms = new Vector<Integer> () ;
	for ( Vector<Integer> v: mm.values() ) {
		if ( v.size() > 1 )
			perms.add( v.elementAt( 0 ) ) ;
	}
	if ( perms.size() > 1 )
		throw new Error ( "perms.size() > 1" ) ;
	if ( perms.isEmpty() )
		return null ;
	int offs = perms.get(0) ;
	int n = 1 ;
	int val = derivs_[offs] ;
	for ( int i = offs+1 ; i<nd ; i++ ) {
		if ( derivs_[i] == val )
			n++ ;
	}
	int[][] ps = getAllPermtations ( n ) ;
	int psl = ps.length ;
	int[][] rv = new int[psl][nd] ;
	index = 0 ;
	/*
	int[] ident = new int[nd] ;
	for ( int i = 0 ; i < nd ; i++ ) {
		ident[i] = i ;
	}*/
	for ( int[] p : ps ) {
		int [] prv = rv[index] ;
		for ( int i = 0 ; i < offs ; i++  ) {
			prv[i] = i ;
		}
		for ( int i = 0 ; i < n ; i++ ) {
			int pos1 = offs+i ;
			int pos2 = offs+p[i] ;
			prv[pos2] = pos1 ;
		}
		for ( int i = offs+n ; i < nd ; i++  ) {
			prv[i] = i ;
		}
		index++ ;
	}
	return rv ;
}

private int[][] getAllPermtations(int n) {
	if ( n < 2 )
		throw new Error ( "n < 2" ) ;
	if ( n == 2 ) {
		int[][] rv = new int[2][2] ;
		rv[0][1] = 1 ;
		rv[1][0] = 1 ;
		return rv ;
	}
	int[][] ps = getAllPermtations ( n-1 ) ;
	int pl = ps.length ;
	int[][] rv = new int [pl*n][n] ;
	int index = 0 ; 
	for ( int i = 0 ; i < n ; i++ ) {
		for ( int[] p : ps ) {
			int[] prv = rv[index] ;
			for ( int j = 0 ; j < n-1 ; j++ ) {
				prv[j] = p[j] ;
			}
			int k = n-1-i ;
			prv[n-1] = n-1 ;
			int sav = prv[k] ;
			prv[n-1] = sav ;
			prv[k] = n-1 ;
			index++ ;
		}
	}
	return rv;
}

/*
public void addFactor(double add) {
	factor += add ;
}*/

public int getLargestDegree() {
	int rv = -1 ;
	for ( int d : derivs_ ) {
		if ( d > rv )
			rv = d ;
	}
	return rv;

}

public GTerm contract(int iup, int ilow) {
	if ( !upperExternIndices.contains( iup ) ) {
		throw new Error ("!upperExternIndices.contains( iup )" ) ;
	}
	if ( !lowerExternIndices.contains( ilow ) ) {
		throw new Error ("!lowerExternIndices.contains( ilow )" ) ;
	}
	GTerm rv = new GTerm ( factor, derivs_ ) ;
	for ( GUpper g : gs ) {
		rv.add( g );
	}
	for ( LowerExtern le : lext ) {
		if ( le.extern.index == ilow ) {
			rv.add( new LowerExtern ( new Index ( iup ), le.intern ));
		} else {
			rv.add( le );
		}
	}
	rv.finish () ;
	rv = rv.contractAllIndices();
	return rv ;
}

public GTerm times(double f) {
	GTerm rv = new GTerm (factor*f, derivs_ ) ;
	for ( GUpper g : gs ) {
		rv.add(g);
	}
	for ( LowerExtern le : lext ) {
		rv.add( le );
	}
	rv.finish();
	return rv ;
}

public GTerm renameExternalIndices(int ... pairs) {
	TreeMap<Index,Index> upper_map = new TreeMap<Index,Index> () ;
	TreeMap<Index,Index> lower_map = new TreeMap<Index,Index> () ;
	int n = pairs.length ;
	if ( (n & 1)!= 0 ) {
		throw new Error ( "uneven integers for pair definition" ) ;
	}
	n /= 2 ;
	for ( int i = 0 ; i<n ; i++ ) {
		int src = pairs[2*i] ;
		int dst = pairs[2*i+1] ;
		if ( upperExternIndices.contains( src ) ) {
			upper_map.put ( new Index ( src), new Index ( dst ) ) ;
		} else if ( lowerExternIndices.contains( src ) ) {
			lower_map.put ( new Index ( src), new Index ( dst ) ) ;
		} else {
			throw new Error ( "external index "+src+" not found" ) ;
		}
	}
	GTerm rv = new GTerm ( factor, derivs_ ) ;
	for ( GUpper g : gs ) {
		rv.add ( map ( g, upper_map ) ) ;
	}
	for ( LowerExtern le : lext ) {
		LowerExtern i = map ( le, lower_map ) ;
		rv.add ( i ) ;
	}
	rv.finish();
	rv = rv.contractAllIndices();
	return rv ;
}

private LowerExtern map(LowerExtern le, TreeMap<Index, Index> map) {
	Index i1 = map.get( le.extern ) ;
	if ( i1 == null )
		i1 = le.extern ;
	return new LowerExtern ( i1, le.intern );
}

private GUpper map(GUpper g, TreeMap<Index, Index> map) {
	Index i1 = map.get( g.i1 ) ;
	if ( i1 == null )
		i1 = g.i1 ;
	Index i2 = map.get( g.i2 ) ;
	if ( i2 == null )
		i2 = g.i2 ;
	return new GUpper ( i1, i2 );
}


GTerm copy ( double newFactor ) {
	GTerm rv = new GTerm ( newFactor, derivs_ ) ;
	for ( GUpper g : gs ) {
		rv.add(g);
	}
	for ( LowerExtern le : lext ) {
		rv.add( le );
	}
	rv.finish () ;
	rv.ordered = ordered ;
	rv.canonic = canonic ;
	return rv ;
}

public double getFactor() {
	return factor ;
}

public GTerm contractAllIndices() {
	return new ContractAllIndices ( this ).getRV () ;
}

public GExpression cov ( int derivBy ) {
	return Tools.cov(this, derivBy);
}

public void writeTo ( File f ) throws IOException {
	FileOutputStream fout = new FileOutputStream ( f ) ;
	ObjectOutputStream oos = new ObjectOutputStream ( fout ) ;
	oos.writeObject( this );
	oos.close();
}

public static GTerm readFromFile ( File f ) throws IOException {
	FileInputStream fin = new FileInputStream ( f ) ;
	ObjectInputStream oin = new ObjectInputStream ( fin ) ;
	GTerm rv = null ;
	try {
		rv = (GTerm) oin.readObject() ;
		oin.close() ;
	} catch (ClassNotFoundException e) {
		oin.close();
		throw new Error ( e ) ;
	}
	oin.close();
	return rv ;
}
}
