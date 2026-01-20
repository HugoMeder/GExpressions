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

	GExpression B () {
		GExpression rv = new GExpression () ;
		rv.add( Tools.g_upper( 0, 1).times ( Tools.g_upper(2, 3) ) );
		rv.add( Tools.g_upper( -1, 0, 2).times ( Tools.g_upper(1, 3) ) );
		return rv ;
	}
	
	GExpression landauLifschitzA () {
		ABCD () ;
		return ( D ) ;
	}
	
	GExpression landauLifschitzTensor () {
		GExpression ll = landauLifschitzA() ;
		ll = ll.times( 0.5 ) ;
		GExpression einst = Tools.Einstein() ;
		ll.add( einst.times( -1 ) );
		ll = ll.canonic() ;
		return ll;
	}
	
	GExpression landauLifschitzADerivation() {
		ABCD () ;
		GExpression rv = new GExpression () ;
		GExpression e = Tools.g_upper(-1, 4, 5).times(Tools.g_low(4,5).derivate(0)) ;
		rv.add(D.times(e));
		rv.add( E );
		return rv.times( 0.5 ) ;
	}
	
	private void ABCD() {
		if ( B!= null )
			return ;
		B = B () ;
		C = d ( B, 2 ) ;
		D = d ( C, 3 ) ;
		E = d ( D, 0) ;
	}

	GExpression d ( GExpression in, int deriveBy ) {
		GTerm t = Tools.g_upper( 4, 5 ) ;
		GExpression e = Tools.g_low( 4, 5 ).derivate( deriveBy ).times( t ).times ( in ) ;
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
			GExpression div = LL.derivate(1);
			div = div.canonic () ;
			return div.isNull();
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
		rv.add( new TestDivergence() ) ;
		return rv;
	}
}
