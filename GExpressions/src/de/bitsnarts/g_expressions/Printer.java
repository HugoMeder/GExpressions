package de.bitsnarts.g_expressions;

import java.util.TreeSet;
import java.util.Vector;

public class Printer {
	static String inds[] = { "mu", "nu", "alpha", "beta", "sigma", "tau", "gamma", "delta" } ;
	
	public static abstract class LatexTable<T> {
		private String txt;
		private T[][] table;

		public LatexTable ( T[][] table ) {
			this.table = table ;
		}
		
/*
\begin{table}
	\centering
		\begin{tabular}{cc}
			a & b \\
			c & d \\
		\end{tabular}
\end{table}
 */
		public String getLatexText () {
			if ( txt != null )
				return txt ;
			StringBuffer sb = new StringBuffer () ;
			int nr = table.length ;
			int nc = table[0].length ;
			sb.append("\\begin{table}\n" ) ;
			sb.append("\t\\centering\n" ) ;
			sb.append("\t\t\\begin{tabular}{" ) ;
			for ( int i =0 ; i < nc ;i++ ) {
				sb.append( 'l' ) ;
			}
			sb.append("}\n\t\t\t" );
			for ( int r = 0 ; r < nr ; r++ ) {
				for ( int c = 0 ; c < nc ;c++ ) {
					String el = toLatex ( r, c, table[r][c] ) ;
					sb.append( el ) ;
					if ( c == nc-1 ) {
						sb.append( " \\\\\n\t\t\t" ) ;
					} else {
						sb.append( " & " ) ;
					}
				}
			}
			sb.append("\\end{tabular}\n" ) ;
			sb.append("\\end{table}\n" );
			txt = sb.toString() ;
			return txt ;
		}
		
		protected abstract String toLatex ( int row, int col, T t ) ;
	}
	
	static class IndexedDerivation {
		
		final int degree;
		int low[] = new int[2] ;
		int deriv[] ;
		int nLow ;
		int nDeriv ;

		IndexedDerivation ( int degree ) {
			this.degree = degree ;
			deriv = new int [degree] ;
		}
		
		void add ( Contraction index, int finalIndex ) {
			if ( !index.isContracting() )
				throw new Error ( "!index.isContracting()" ) ;
			if ( index.bindsDerivation() ) {
				if ( nDeriv == degree )
					throw new Error ("nDeriv == degree") ;
				deriv[nDeriv++] = finalIndex ;
			} else {
				if ( nLow == 2 )
					throw new Error ("nDeriv == degree") ;
				low[nLow++] = finalIndex ;
			}
		}
		
		boolean isComplete () {
			return nLow == 2 && nDeriv == degree ;
		}
	}
	
	public static String toString ( GExpression e ) {
		if ( e.isNull() )
			return "0" ;
		StringBuffer sb = new StringBuffer () ;
		for ( GTerm t : e.terms ) {
			if ( t.factor >= 0 )
				sb.append ( "+" ) ;
			sb.append( t.toString() ) ;
		}
		return sb.toString() ;
	}
	
	public static String asLatex ( GExpression e, Vector<Integer> upperInds, Vector<Integer> lowerInds, String name ) {
		return asLatex ( false, e, upperInds, lowerInds, name ) ;
	}
	
	public static String asLatex ( boolean canonicTerms, GExpression e, Vector<Integer> upperInds, Vector<Integer> lowerInds, String name ) {
		check ( upperInds, e.upperInds ) ;
		check ( lowerInds, e.lowerInds ) ;
		StringBuffer rv = new StringBuffer () ;
		rv.append( "\\begin{eqnarray*}\n") ;
		rv.append( name ) ;
		if ( !upperInds.isEmpty() ) {
			rv.append( "^{") ;
			for ( Integer u : upperInds ) {
				rv.append( "\\"+getIdStr ( u ) ) ;
			}
			rv.append( "}" ) ;
		}
		if ( !lowerInds.isEmpty() ) {
			rv.append( "_{") ;
			for ( Integer l : lowerInds ) {
				rv.append( "\\"+getIdStr ( l ) ) ;
			}
			rv.append( "}" ) ;
		}
		rv.append ( "&=&" ) ;
		StringBuffer line = new StringBuffer () ;
		for ( GTerm t : e.terms ) {
			String ts = canonicTerms ? t.canonic().toString() : t.toString() ;
			if ( t.factor >= 0 ) {
				ts = "+"+ts ;
			}
			if ( line.length() !=0 && line.length()+ts.length() > 200 ) {
				rv.append( line ) ;
				rv.append( "\\\\\n&&" ) ;
				line.setLength(0);
			}
			line.append( ts ) ;
		}
		if ( line.length() > 0 ) {
			rv.append( line ) ;
		}
		rv.append( "\n\\end{eqnarray*}\n") ;
		return rv.toString() ;
	}
	
	private static void check(Vector<Integer> v, TreeSet<Integer> s) {
		TreeSet<Integer> s2 = new TreeSet<Integer> () ;
		s2.addAll( v ) ;
		if ( !s2.equals( s ) ) {
			throw new Error ( "Index sets do not match") ;
		}
	}

	public static String toString ( GTerm t ) {
		@SuppressWarnings("unchecked")
		TreeSet<Integer> indexSet = (TreeSet<Integer>) t.lowerExternIndices.clone() ;
		indexSet.addAll( t.upperExternIndices) ;
		Derivation[] derivs = t.derivs ;
		int nd = derivs.length ;
		IndexedDerivation[] ids = new IndexedDerivation [nd] ;
		int nextIndex = 0 ;
		StringBuffer sb = new StringBuffer () ;
		sb.append( ""+t.factor ) ;
		for ( int i = 0 ; i < nd ; i++ ) {
			ids[i] = new IndexedDerivation ( derivs[i].degree ) ;
		}
		for ( GUpper g: t.gs ) {
			sb.append( "g^{" ) ;
			int ni = add ( g.i1, ids, indexSet, nextIndex ) ;
			if ( ni == nextIndex )
				sb.append( "\\"+getIdStr( g.i1.index)) ;
			else
				sb.append( "\\"+getIdStr(ni-1) ) ;
			ni = add ( g.i2, ids, indexSet, nextIndex ) ;
			if ( ni == nextIndex )
				sb.append( "\\"+getIdStr(g.i2.index) ) ;
			else
				sb.append( "\\"+getIdStr(ni-1) ) ;
			sb.append("}") ;
		}
		for ( LowerExtern e : t.lext ) {
			int ei = e.extern.index ;
			ids[e.intern.index].add(e, ei );
		}
		for ( IndexedDerivation id : ids ) {
			if ( !id.isComplete() ) {
				throw new Error ( "!id.isComplete()" ) ;
			}
			sb.append( "g_{" ) ;
			sb.append( "\\"+getIdStr(id.low[0])) ;
			sb.append( "\\"+getIdStr(id.low[1])) ;
			if ( id.degree > 0 )
				sb.append( ",") ;
			for ( int i = 0 ; i < id.degree ; i++ ) {
				sb.append( "\\"+getIdStr(id.deriv[i])) ;
			}
			sb.append( "}" ) ;
		}
		return sb.toString() ;
	}

	private static String getIdStr(int index) {
		int il = inds.length ;
		if ( index < il )
			return inds[index] ;
		int offs = index-il+1 ;
		return inds[il-1]+"_{"+offs+"}";
	}

	private static int add(Index i, IndexedDerivation[] ids, TreeSet<Integer> indexSet, int nextIndex) {
		if ( i.extern ) {
			return nextIndex ;
		}
		while ( indexSet.contains( nextIndex ) )
			nextIndex++ ;
		indexSet.add( nextIndex ) ;
		IndexedDerivation id = ids[i.index] ;
		id.add(i, nextIndex);
		nextIndex++ ;
		return nextIndex ;
	}
}
