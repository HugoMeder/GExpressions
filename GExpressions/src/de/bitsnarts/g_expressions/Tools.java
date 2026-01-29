package de.bitsnarts.g_expressions;

import java.util.TreeSet;

public class Tools {
	
	public static GTerm g_low ( double factor, int e1, int e2 ) {
		int[] d = new int[1] ;
		GTerm rv = new GTerm ( factor, d ) ;
		rv.add( new LowerExtern ( new Index ( e1 ), new Index ( 0, false) ) );
		rv.add( new LowerExtern ( new Index ( e2 ), new Index ( 0, false) ) );
		rv.finish();
		return rv ;
	}
	
	public static GTerm g_low ( int e1, int e2 ) {
		return g_low( 1.0, e1, e2 ) ;
	}
	
	public static GTerm g_upper ( double factor, int e1, int e2 ) {
		GTerm rv = new GTerm ( factor, new int[0] ) ;
		rv.add( new GUpper ( e1, e2 ) );
		rv.finish();
		return rv ;
	}
	
	public static GTerm g_upper ( int e1, int e2 ) {
		return g_upper ( 1.0, e1, e2 ) ;
	}
	

	public static GExpression GammaLower ( double factor, int e1, int e2, int e3 ) {
		GExpression rv = new GExpression () ;
		rv.add( g_low(0.5*factor, e3, e2).derivate( e1 ) ) ;
		rv.add( g_low(0.5*factor, e1, e3).derivate( e2 ) ) ;
		rv.add( g_low(-0.5*factor, e1, e2).derivate( e3 ) ) ;
		return rv ;
	}
	
	public static GExpression GammaUpper (  double factor, int e1, int e2, int e3 ) {
		int i = 0 ;
		while ( i == e1 || i == e2 || i == e3 )
			i++ ;
		GExpression gl = GammaLower ( factor, e1, e2, i ) ;
		GExpression rv = gl.times ( g_upper ( e3, i ) ) ;
		return rv ;
	}

	public static GExpression GammaLower ( int e1, int e2, int e3 ) {
		return GammaLower (1, e1, e2, e3 ) ;
	}
	
	public static GExpression GammaUpper ( int e1, int e2, int e3 ) {
		return GammaUpper (1, e1, e2, e3 ) ;
	}


	public static GExpression Riemann_upper ( int e1, int e2, int e3, int e4 ) {
		int i = 0 ;
		while ( i == e1 || i == e2 || i == e3  || i == e4 )
			i++ ;
		GExpression rv = new GExpression () ;
		rv.add( GammaUpper ( -1, e2, e3, e1 ).derivate(e4) );
		rv.add( GammaUpper ( e2, e4, e1 ).derivate(e3) );
		rv.add( GammaUpper ( i, e3, e1).times (GammaUpper(e2, e4, i )));
		rv.add( GammaUpper ( -1, i, e4, e1).times (GammaUpper(e2, e3, i )));
		//rv.add( Tools.GammaUpper ( e2, e4, i).times (Tools.GammaUpper(i, e3, e1 )));
		//rv.add( Tools.GammaUpper ( -1, e2, e3, i).times (Tools.GammaUpper(i, e4, e1 )));
		return rv ;
	}
	
	public static GExpression Riemann_lower ( int e1, int e2, int e3, int e4 ) {
		int i = 0 ;
		while ( i == e1 || i == e2 || i == e3 || i == e4 ) {
			i++ ;
		}
		GExpression rv = Riemann_upper ( i, e2, e3, e4 ) ;
		return rv.times(g_low ( i, e1 ) ) ;
	}

	public static GExpression Einstein () {
		return Einstein ( 0, 1 ) ;
/*		GExpression rv = RicciHigh ( 0, 1 ) ;
		GTerm t = g_low ( -0.5, 0, 1 ) ;
		GExpression R_half = rv.times( t ) ;
		GExpression e = R_half.times( g_upper(0,1));
		rv.add( e ) ;
		return rv ;*/
	}
	
	
	public static GExpression Einstein ( int i1, int i2 ) {
		GExpression rv = RicciHigh ( i1, i2 ) ;
		GTerm t = g_low ( -0.5, i1, i2 ) ;
		GExpression R_half = rv.times( t ) ;
		GExpression e = R_half.times( g_upper(1,i1,i2));
		rv.add( e ) ;
		return rv ;
	}
	
	public static GExpression RicciHigh ( int e1, int e2 ) {
		int i = 0 ;
		while ( i == e1 || i==e2 )
			i++;
		int j = i+1 ;
		while ( j == e1 || j==e2 )
			j++;
		return RicciLow(i,j).times ( g_upper ( e1, i )).times ( g_upper ( e2, j )) ;
	}
	
	public static GExpression RicciLow ( int e1, int e2 ) {
		int i = 0 ;
		while ( i == e1 || i == e2 )
			i++ ;
		int j = i+1 ;
		while ( j == e1 || j == e2 )
			j++ ;
		GExpression r = Riemann_upper ( i, e1, j, e2 ) ;
		return r.contract ( i, j ) ;
	}
	
	public static GExpression cov ( GTerm tensor, int deriveBy ) {
		TreeSet<Integer> lo = tensor.lowerExternIndices ;
		TreeSet<Integer> hi = tensor.upperExternIndices ;
		@SuppressWarnings("unchecked")
		TreeSet<Integer> s = (TreeSet<Integer>) lo.clone () ;
		s.addAll( hi ) ;
		int i =  0 ;
		while ( s.contains( i ) )
			i++ ;
		if ( i == deriveBy ) {
			i++ ;
		}
		/*
		if ( s.contains( deriveBy ) ) {
			int j = i+1 ;
			while ( s.contains( j ) )
				j++ ;
			GExpression c = cov ( tensor, j ) ;
			return c.renameExternalIndices( j, deriveBy ) ;
		}*/
		int j = i+1 ;
		while ( s.contains( j ) )
			j++ ;
		GExpression rv = new GExpression () ;
		rv.add(tensor.derivate(deriveBy)) ;
		// derivaton of g^{\mu\nu},\alpha = - g^{\mu\beta}g^{\sigma\nu}g_{\beta\sigma,alpha}
		for ( int k : hi ) {
			GTerm tr = tensor.renameExternalIndices ( k,i ) ;
			GExpression gamma = GammaUpper ( i, deriveBy, k ) ;
			rv.add(tr.times(gamma));
		}
		for ( int k : lo ) {
			GTerm tr = tensor.renameExternalIndices ( k,i ) ;
			GExpression gamma = GammaUpper ( -1, k, deriveBy, i ) ;
			rv.add(tr.times(gamma));
		}
		return rv ;
	}
	
	public static GExpression cov ( GExpression tensor, int deriveBy ) {
		TreeSet<Integer> lo = tensor.lowerInds ;
		TreeSet<Integer> hi = tensor.upperInds ;
		@SuppressWarnings("unchecked")
		TreeSet<Integer> s = (TreeSet<Integer>) lo.clone () ;
		s.addAll( hi ) ;
		int i =  0 ;
		while ( s.contains( i ) )
			i++ ;
		if ( i == deriveBy ) {
			i++ ;
		}
		/*
		if ( s.contains( deriveBy ) ) {
			int j = i+1 ;
			while ( s.contains( j ) )
				j++ ;
			GExpression c = cov ( tensor, j ) ;
			return c.renameExternalIndices( j, deriveBy ) ;
		}*/
		int j = i+1 ;
		while ( s.contains( j ) )
			j++ ;
		GExpression rv = new GExpression () ;
		rv.add(tensor.derivate(deriveBy)) ;
		// derivaton of g^{\mu\nu},\alpha = - g^{\mu\beta}g^{\sigma\nu}g_{\beta\sigma,alpha}
		for ( int k : hi ) {
			GExpression er = tensor.renameExternalIndices(k,i ) ;
			GExpression gamma = GammaUpper ( i, deriveBy, k ) ;
			rv.add(er.times(gamma));
		}
		for ( int k : lo ) {
			GExpression er = tensor.renameExternalIndices( k,i ) ;
			GExpression gamma = GammaUpper ( -1, k, deriveBy, i ) ;
			rv.add(er.times(gamma));
		}
		return rv ;
	}
	


	public static boolean testSymmetry ( GExpression e , int ... pairs ) {
		GExpression e2 = new GExpression () ;
		e2.add( e );
		GExpression r = e.renameExternalIndices(pairs).times(-1)  ;
		e2.add( r);
		e2 = e2.canonic();
		return e2.isNull () ;
	}
	
	public static boolean testAntisymmetry ( GExpression e , int ... pairs ) {
		GExpression e2 = new GExpression () ;
		e2.add( e );
		GExpression e3 = e.renameExternalIndices(pairs) ;
		e2.add( e3 );
		e2 = e2.canonic();
		return e2.isNull () ;
	}
}
