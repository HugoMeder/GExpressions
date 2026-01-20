package de.bitsnarts.g_expressions;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class GExpression {
	Vector<GTerm> terms = new Vector<GTerm> () ;
	TreeSet<Integer> upperInds;
	TreeSet<Integer> lowerInds;
	
	public void add ( GTerm t ) {
		if ( terms.isEmpty() ) {
			terms.add(t) ;
			upperInds = t.upperExternIndices ;
			lowerInds = t.lowerExternIndices ;
		} else {
			if ( !t.upperExternIndices.equals( upperInds ) ) {
				throw new Error ( "!t.upperExternIndices.equals( upperInds )" ) ;
			}
			if ( !t.lowerExternIndices.equals( lowerInds ) ) {
				throw new Error ( "!t.lowerExternIndices.equals( lowerInds )" ) ;
			}
			terms.add( t ) ;
		}
	}
	
@Override
public String toString ( ) {
	return Printer.toString( this ) ;
}

public GExpression derivate(int i) {
	GExpression rv = new GExpression () ;
	for ( GTerm t : terms ) {
		GExpression e = t.derivate(i) ;
		for ( GTerm t2 : e.terms ) {
			rv.add(t2);
		}
	}
	return rv ;
}

public void add(GExpression e ) {
	for ( GTerm t : e.terms ) {
		add ( t ) ;
	}
}

public GExpression times(GTerm t) {
	GExpression rv = new GExpression () ;
	for ( GTerm t2 : terms ) {
		rv.add( t.times(t2));
	}
	return rv ;
}

public GExpression times(GExpression e) {
	GExpression rv = new GExpression () ;
	for ( GTerm t1 : terms ) {
		for ( GTerm t2 : e.terms ) {
			rv.add( t1.times(t2));
		}
	}
	return rv ;
}

public List<GTerm> getTerms() {
	return terms ;
}

public GExpression canonic() {
	TreeMap<GTerm,Double> ts = new TreeMap<GTerm,Double> () ;
	int index = 0 ;
	for ( GTerm t : terms ) {
		GTerm tc ;
		if ( true ) {
			tc = t.canonic() ;
		} else {
			tc = t.order() ;
		}
		Double f = ts.get ( tc ) ;
		if ( f != null ) {
			ts.put(tc, f+t.factor) ;
		} else {
			ts.put(tc, t.factor ) ;
		}
		index++ ;
	}
	GExpression rv = new GExpression () ;
	for ( Entry<GTerm, Double> e : ts.entrySet() ) {
		Double v = e.getValue() ;
		if ( v != 0 ) {
			rv.add( e.getKey().copy( v ) );
		}
	}
	return rv;
}

public int getLargestDegree() {
	int rv = 0 ;
	for ( GTerm t :terms ) {
		int d = t.getLargestDegree () ;
		if ( d > rv )
			rv = d ;
	}
	return rv ;
}

public GExpression contract(int iup, int ilow ) {
	GExpression rv = new GExpression () ;
	for ( GTerm t : terms ) {
		rv.add( t.contract ( iup, ilow ));
	}
	return rv ;
}

public GExpression times(double f) {
	GExpression rv = new GExpression () ;
	for ( GTerm t : terms ) {
		rv.add( t.times( f ));
	}
	return rv ;
}

public GExpression renameExternalIndices ( int ... pairs ) {
	GExpression rv = new GExpression () ;
	for ( GTerm t : terms ) {
		GTerm l = t.renameExternalIndices ( pairs ) ;
		rv.add( l );
	}
	return rv ;
}

public boolean isNull() {
	return terms.isEmpty() ;
}

public String asLatex ( Vector<Integer> upperInds, Vector<Integer> lowerInds, String name ) {
	return Printer.asLatex(this, upperInds, lowerInds, name ) ;
}

public String asLatex(boolean canonicTerms, Vector<Integer> upperInds, Vector<Integer> lowerInds, String name) {
	return Printer.asLatex(canonicTerms, this, upperInds, lowerInds, name ) ;
}

public GExpression cov ( int deriveBy ) {
	GExpression rv = new GExpression () ;
	for ( GTerm t : this.terms ) {
		rv.add ( t.cov(deriveBy)) ;
	}
	return rv ;
}
}
