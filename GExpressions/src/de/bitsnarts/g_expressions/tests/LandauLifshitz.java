package de.bitsnarts.g_expressions.tests;

import java.util.List;
import java.util.Vector;

import de.bitsnarts.g_expressions.GExpression;
import de.bitsnarts.g_expressions.GTerm;
import de.bitsnarts.g_expressions.Tools;

public class LandauLifshitz extends Test {
	private GExpression B;
	private GExpression C;
	private GExpression D;
	private GExpression E;
	private static double g_up_low = 1 ;
	
	GExpression B () {
		GExpression rv = new GExpression () ;
		rv.add( Tools.g_upper( 0, 1).times ( Tools.g_upper(2, 3) ) );
		rv.add( Tools.g_upper( -1, 0, 2).times ( Tools.g_upper(1, 3) ) );
		return rv ;
	}
	
	GExpression landauLifschitzA () {
		BCDE () ;
		return ( D ) ;
	}
	
	GExpression landauLifschitzTensor () {
		GExpression ll = landauLifschitzA() ;
		ll = ll.times( 0.5 ) ;
		GExpression einst = Tools.Einstein() ;
		ll.add( einst.times( -1 ) );
		//ll = ll.canonic() ;
		return ll ;
	}
	
	GExpression landauLifschitzADerivation() {
		BCDE () ;
		GExpression rv = new GExpression () ;
		GExpression e = g_deriv(-1, 0) ;
		rv.add(D.times(e));
		rv.add( E );
		return rv;
	}
	
	private void BCDE() {
		if ( B!= null )
			return ;
		B = B () ;
		C = d ( B, 2 ) ;
		D = d ( C, 3 ) ;
		E = d ( D, 0) ;
	}

	static GExpression g_deriv ( double f, int derivBy ) {
		int i = 0 ;
		while ( i == derivBy )
			i++ ;
		int j = i+1 ;
		while ( j == derivBy )
			j++ ;
		return Tools.g_upper( f*g_up_low, i, j ).times ( Tools.g_low(i, j).derivate( derivBy ) ) ;
	}
	
	static GExpression g_deriv ( int derivBy ) {
		return g_deriv ( 1, derivBy ) ;
	}
	
	GExpression d ( GExpression in, int deriveBy ) {
		GTerm t = Tools.g_upper( 4, 5 ) ;
		GExpression e = g_deriv(deriveBy).times ( in ) ;
		GExpression rv = new GExpression () ;
		rv.add(e);
		rv.add(in.derivate(deriveBy));
		return rv ;
	}
	
	public static void main ( String args[] ) {
		//testDivergence () ;
		//testNoSecond () ;
		new LandauLifshitz().run(0);
	}

	class TestNoSecond extends Test {

		@Override
		String getName() {
			return "TestNoSecond" ;
		}

		@Override
		boolean passed() {
			LandauLifshitz LL = new LandauLifshitz () ;
			GExpression ll = LL.landauLifschitzTensor() ;
			int d0 = ll.getLargestDegree () ;
			ll = ll.canonic() ;
			int d = ll.getLargestDegree () ;
			return d < 2 ;
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
		
	}

	private static void listNonlinearTerms(GExpression e) {
		List<GTerm> ts = e.getTerms() ;
		for ( GTerm t : ts ) {
			if ( t.getLargestDegree() > 1 ) {
				System.out.println ( t ) ;
			}
		}
	}

	class SimpleTest extends Test {

		@Override
		String getName() {
			return "SimpleTest";
		}

		@Override
		boolean passed() {
			GTerm t = Tools.g_low(0, 1) ;
			GExpression e = new GExpression () ;
			e.add( t );
			e = d ( e, 2 ) ;
			System.out.println ( e.asLatex( "X" ) ) ;
			t = Tools.g_upper(0, 1) ;
			e = new GExpression () ;
			e.add( t );
			e = d ( e, 0 ) ;
			System.out.println ( e.asLatex( "Y" ) ) ;
			e = d ( e, 1 ) ;
			System.out.println ( e.asLatex( "Z" ) ) ;
			e = e.canonic() ;
			System.out.println ( e.asLatex( "Z" ) ) ;
			return true ;
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
	}


	class TestDivergence0 extends Test {

		@Override
		String getName() {
			return "TestDivergence0";
		}

		@Override
		boolean passed() {
			LandauLifshitz ll = new LandauLifshitz();
			GExpression LL = ll.landauLifschitzA() ;
			GExpression lld = LL.derivate(0) ;
			lld = lld.canonic() ;
			System.out.println ( lld.asLatex( "Div" ) ) ;
			lld.add( ll.landauLifschitzADerivation().times(-1 ) ) ;
			lld = lld.canonic() ;
			return lld.isNull();
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
	}

	class TestDivergence2 extends Test {

		@Override
		String getName() {
			return "TestDivergence2";
		}

		@Override
		boolean passed() {
			LandauLifshitz ll = new LandauLifshitz();
			GExpression LL = ll.landauLifschitzA() ;
			GExpression lld = LL.derivate(0) ;
			GExpression a = g_deriv(1, 0 ).times(LL) ;
			lld.add( a );
			lld = lld.canonic() ;
			System.out.println ( lld.asLatex( "LLD2") ) ;
			return lld.isNull();
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
	}


	class TestDivergence extends Test {

		@Override
		String getName() {
			return "TestDivergence";
		}

		@Override
		boolean passed() {
			LandauLifshitz ll = new LandauLifshitz();
			GExpression LL = ll.landauLifschitzADerivation() ;
			LL = LL.canonic() ;
			//System.out.println ( LL.asLatex( "A" ) ) ;
			return LL.isNull();
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
	}

	@Override
	String getName() {
		return "LandauLifshitz" ;
	}

	@Override
	boolean passed() {
		return false;
	}

	@Override
	Vector<Test> getSubTests() {
		Vector<Test> rv = new Vector<Test> () ;
		rv.add( new TestNoSecond() ) ;
		rv.add( new SimpleTest() ) ;
		rv.add( new TestDivergence2() ) ;
		rv.add( new TestDivergence0() ) ;
		rv.add( new TestDivergence() ) ;
		return rv;
	}
}
