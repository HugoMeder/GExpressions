package de.bitsnarts.g_expressions.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import de.bitsnarts.g_expressions.GExpression;
import de.bitsnarts.g_expressions.GTerm;
import de.bitsnarts.g_expressions.MultiMap;
import de.bitsnarts.g_expressions.Printer;
import de.bitsnarts.g_expressions.Tools;

import java.util.TreeMap;
import java.util.Vector;

public class ToolsTest extends Test {
	
	ToolsTest () {
	}
	
	static class Pair {
		private int r;
		private int c;

		Pair ( int r, int c ) {
			this.r = r ;
			this.c = c ;
		}
	}
	

	static class TablePermutation extends Printer.LatexTable<String> {

		public TablePermutation(List<GTerm> list) {
			super(createTable ( list) );
		}

		private static String[][] createTable(List<GTerm> list) {
			int nc = 3 ;
			int ne = list.size() ;
			if ( (ne % nc) != 0 )
				throw new Error ( "(list.size() & nc) != 0" ) ;
			int nr = ne /nc ;
			GTerm[][] gtab = new GTerm[nr][nc] ;
			for ( int i = 0 ; i < ne ; i++ ) {
				gtab[i/nc][i%nc] = list.get(i) ;
			}
			MultiMap<GTerm, Pair> mm = new MultiMap<GTerm,Pair> () ;
			TreeMap<GTerm, Double> mmd = new TreeMap<GTerm,Double> () ;
			int index = 0 ;
			for ( GTerm t : list ) {
				int r = index/nc ;
				int c = index%nc ;
				t = t.canonic() ;
				mm.put ( t, new Pair(r,c) ) ;
				Double old = mmd.get( t ) ;
				if ( old != null ) {
					mmd.put(t, old+t.getFactor  () ) ;
				} else {
					mmd.put(t, t.getFactor() ) ;
				}
				index ++ ;
			}
			String[][] rv = new String [nr+1][nc+1] ;
			index = 0 ;
			for ( Entry<GTerm, Vector<Pair>> e : mm.entrySet() ) {
				Vector<Pair> ps = e.getValue() ;
				String expr = e.getKey().toString() ;
				if ( ps.size() != 2 )
					throw new Error ( "ps.size() != 2" ) ;
				Pair p1 = ps.get(0) ;
				Pair p2 = ps.get(1) ;
				boolean cancelled = mmd.get( e.getKey() ) == 0 ;
				String mark = !cancelled ? "*" : "\\ " ;
				if ( false ) {
					rv[p1.r+1][p1.c+1] = mark+p2.r+","+p2.c ;
					rv[p2.r+1][p2.c+1] = mark+p1.r+","+p1.c ;
				} else {
					rv[p1.r+1][p1.c+1] = mark+p2.r+","+p2.c+" $"+gtab[p1.r][p1.c]+"$" ;
					rv[p2.r+1][p2.c+1] = mark+p1.r+","+p1.c+" $"+gtab[p2.r][p2.c]+"$" ;
				}
			}
			for ( int i = 0 ;i < nr ; i++ ) {
				rv[i+1][0] = ""+i ;
			}
			for ( int i = 0 ;i < nc ; i++ ) {
				rv[0][i+1] = ""+i ;
			}
			rv[0][0] = "" ;

			return rv;
		}

		@Override
		protected String toLatex(int row, int col, String t) {
			return t ;
		}
		
	}
	class GammaTest extends Test {

		@Override
		String getName() {
			return "gamma Test" ;
		}

		@Override
		boolean passed() {
			GExpression e = Tools.GammaLower(0, 1, 2) ;
			if ( !Tools.testSymmetry ( e, 0, 1, 1, 0) ) {
				return false ;
			}
			e = Tools.GammaUpper(0, 1, 2) ;
			if ( !Tools.testSymmetry ( e, 0, 1, 1, 0) ) {
				return false ;
			}
			return true ;
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
	}
	
	class GTermTable extends Printer.LatexTable<GTerm> {

		public GTermTable(GTerm[][] table) {
			super(table);
		}

		@Override
		protected String toLatex(int row, int col, GTerm t) {
			return "$"+t.toString ( )+"$" ;
		}
	}
	
	class RiemannTest extends Test {

		@Override
		String getName() {
			return "Riemann Test";
		}

		class RiemannSymmetryTest extends Test {

			@Override
			String getName() {
				return "RiemannSymmetryTest";
			}

			@Override
			boolean passed() {
				GExpression ri = Tools.Riemann_lower(0, 1, 2, 3) ;
				if ( !Tools.testAntisymmetry(ri, 2, 3, 3, 2 )) {
					return  false ;
				}
				/*
				GExpression ec = new GExpression () ;
				ec.add( ri );
				
				ec.add( ri.renameExternalIndices( 0, 1, 1, 0));
				ec = ec.canonic() ;
				List<GTerm> ts = ec.getTerms() ;
				int index = 0 ;
				for ( GTerm t : ts ) {
					try {
						t.writeTo( new File ( "t"+index+".bin" ));
					} catch (IOException e) {
						e.printStackTrace();
					}
					index++ ;
				}
				
				try {
					GTerm gt = GTerm.readFromFile( new File ( "t0.bin") ) ;
					gt = gt.contractAllIndices () ;
				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
				if ( !Tools.testAntisymmetry(ri, 0, 1, 1, 0 )) {
					return  false ;
				}
				if ( !Tools.testSymmetry(ri, 
						0, 2, 
						2, 0,
						1, 3, 
						3, 1
							)) {
					return  false ;
				}
				return true;
			}

			@Override
			Vector<Test> getSubTests() {
				// TODO Auto-generated method stub
				return null;
			}
		}
		
		class EinsteinTensorRicci2 extends Test {

			@Override
			String getName() {
				return "EinsteinTensorRicci2";
			}

			@Override
			boolean passed() {
				GTerm t = Tools.g_upper(0,1).times(Tools.g_low( 0,2));
				t = t.times(Tools.g_low(1, 3));
				t = t.canonic() ;
				GExpression rid = Tools.cov( Tools.Riemann_lower(0, 1, 2, 3).times(-1), 4 ) ;
				GExpression ricci2 = new GExpression () ;
				ricci2.add(rid);
				rid = rid.renameExternalIndices(
						2, 3,
						3, 4,
						4, 2 ) ;
				ricci2.add(rid);
				rid = rid.renameExternalIndices(
						2, 3,
						3, 4,
						4, 2 ) ;
				ricci2.add(rid);
				if ( !ricci2.canonic().isNull() )
					return false ;
				GExpression e = ricci2.times( Tools.g_upper(1, 3) ) ;
				e = e.times( Tools.g_upper(0, 4)) ;
				e = e.times( Tools.g_upper(1,2)) ;
				if ( !e.canonic().isNull() )
					return false ;
				GExpression e2 = Tools.cov(Tools.Einstein(),0).times(2) ;
				GExpression diff = new GExpression() ;
				diff.add( e );
				diff.add( e2 );
				diff = diff.canonic() ;
				GExpression ricci = Tools.Einstein().cov ( 0 ) ;
				GExpression extr = extracThirdDerivatives (ricci) ;
				extr = extr.canonic() ;
				if ( !diff.isNull() )
					return false ;
				return true ;
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}
		

		class EinsteinTensorDivergence extends Test {

			@Override
			String getName() {
				return "EinsteinTensorDivergence";
			}

			@Override
			boolean passed() {
				GExpression e = Tools.Einstein() ;
				e = Tools.cov(e, 3);
				e = e.renameExternalIndices(3,0) ;
				e = e.canonic() ;
				return e.isNull() ;
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}
		
		class RicciSymmetry extends Test {

			@Override
			String getName() {
				return "RicciSymmetry";
			}

			@Override
			boolean passed() {
				GExpression rl = Tools.RicciLow(0, 1) ;
				return Tools.testSymmetry(rl, 0, 1, 1, 0 ) ;
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}
		
		class EinsteinSymmetry extends Test {

			@Override
			String getName() {
				return "EinsteinSymmetry";
			}

			@Override
			boolean passed() {
				GExpression rl = Tools.Einstein() ;
				return Tools.testSymmetry(rl, 0, 1, 1, 0 ) ;
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}
		

		class BianchiIdent1 extends Test {

			@Override
			String getName() {
				return "BianchiIdent1";
			}

			@Override
			boolean passed() {
				GExpression e = Tools.Riemann_upper(0, 1, 2, 3 ) ;
				GExpression e2 = new GExpression () ;
				e2.add( e ) ;
				e = e.renameExternalIndices(
						1, 2,
						2, 3,
						3, 1
						) ;
				e2.add( e );
				e = e.renameExternalIndices(
						1, 2,
						2, 3,
						3, 1
						) ;
				e2.add( e );
				e2 = e2.canonic();
				return e2.isNull();
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}
		
		class TestCovG extends Test {

			@Override
			String getName() {
				return "TestCovG";
			}

			@Override
			boolean passed() {
				GTerm gu = Tools.g_upper(0, 1) ;
				GExpression gud = Tools.cov(gu, 2 ) ;
				gud = gud.canonic() ;
				if ( !gud.isNull() )
					return false ;
				GTerm gl = Tools.g_low(0, 1) ;
				GExpression gld = Tools.cov(gl, 2 ) ;
				gld = gld.canonic() ;
				if ( !gld.isNull() )
					return false ;
				return true ;
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}


		class BianchiIdent2 extends Test {

			@Override
			String getName() {
				return "BianchiIdent2";
			}

			@Override
			boolean passed() {
				GExpression e = Tools.Riemann_upper(0, 1, 2, 3 ) ;
				e = Tools.cov(e, 4 ) ;
				GExpression e2 = new GExpression () ;
				e2.add( e ) ;
				e = e.renameExternalIndices(
						2, 3,
						3, 4,
						4, 2
						) ;
				e2.add( e );
				e = e.renameExternalIndices(
						2, 3,
						3, 4,
						4, 2
						) ;
				e2.add( e );
				e2 = e2.canonic();
				return e2.isNull();
			}

			@Override
			Vector<Test> getSubTests() {
				return null;
			}
			
		}

		/*
		GExpression RiemannLowNormalCoords ( int e1, int e2, int e3, int e4 ) {
			GExpression rv = new GExpression () ;
			rv.add( Tools.GammaLower ( -1, e2, e3, e1 ).derivate(e4) );
			rv.add( Tools.GammaLower ( e2, e4, e1 ).derivate(e3) );
			return rv.canonic() ;
		}
		*/
		
		/*
		GExpression RiemannLowGammaProds ( int e1, int e2, int e3, int e4 ) {
			int i = 0 ;
			while ( i == e1 || i == e2 || i == e3  || i == e4 )
				i++ ;
			int j = i+1 ;
			while ( j == e1 || j == e2 || j == e3  || j == e4 )
				j++ ;
			GExpression rv = new GExpression () ;
			//rv.add( Tools.GammaLower ( i, e3, e1).times (Tools.GammaUpper(e2, e4, i )));
			//rv.add( Tools.GammaLower ( -1, i, e4, e1).times (Tools.GammaUpper(e2, e3, i )));
			//rv.add( Tools.GammaLower ( i, e1, e3 ).times (Tools.GammaLower( e2, e4, j ) ) );
			//rv.add( Tools.GammaLower ( -1, i, e1, e4 ).times (Tools.GammaLower( e2, e3, j ) ) );
			GExpression f1 = Tools.GammaLower     ( e2, e4, i ) ;
			GExpression f2 = Tools.GammaLower( j, e3, e1 ) ;
			rv.add( f1.times ( f2 ) );
			f1 = Tools.GammaLower ( -1, e2, e3, i ) ;
			f2 = Tools.GammaLower( j, e4, e1 ) ;
			rv.add( f1.times ( f2 ) );
			//rv.add( Tools.GammaUpper ( e2, e4, i).times (Tools.GammaUpper(i, e3, j )));
			//rv.add( Tools.GammaUpper ( -1, e2, e3, i).times (Tools.GammaUpper(i, e4, j )));
			rv = rv.times( Tools.g_upper( i, j )) ;
			return rv ;
		}
		*/
		/*
		void anomalyTest () {
			GExpression r1 = Tools.Riemann_lower(0, 1, 2, 3) ;
			GExpression r2 = r1.renameExternalIndices(0,1,1,0 ) ;
			GExpression s = new GExpression () ;
			s.add( r1 ) ;
			s.add( r2 ) ;
			s.canonic() ;
			if ( s.isNull() ) {
				return ;
			}
			r1 = r1.canonic() ;
			r2 = r1.renameExternalIndices(0,1,1,0 ) ;
			s = new GExpression () ;
			s.add( r1 ) ;
			s.add( r2 ) ;
			s.canonic() ;
			if ( s.isNull() ) {
				return ;
			}

//			r = r.canonic() ;
//			if ( !Tools.testAntisymmetry( r, 2,3,3,2 ) )
//				return false ;
//			if ( !Tools.testAntisymmetry( r, 0,1,1,0 ) )
//				return false ;
		}*/
		
		/*
		Vector<Integer> iv ( int ...els ) {
			Vector<Integer> rv = new Vector<Integer> () ;
			for  ( int i : els ) {
				rv.add( i ) ;
			}
			return rv ;
		}
		*/
		
		boolean contractionTest () {
			GTerm delta = Tools.g_low(0, 1).times(Tools.g_upper(0, 2)) ;
			GExpression gld = Tools.g_low(1, 2).derivate( 0 ) ;
			GExpression gud = gld.times( Tools.g_upper(0, 3)) ;
			GExpression gudd = gud.derivate(4) ;
			String str = gudd.toString() ;
			gud = gld.times( Tools.g_upper(1, 3)) ;
			gudd = gud.derivate(4) ;
			GTerm _4 = Tools.g_low(0, 1).times(Tools.g_upper(0, 1)) ;
			return true ;
		}
		
public GExpression extracThirdDerivatives(GExpression e) {
	GExpression rv = new GExpression () ;
	for ( GTerm t : e.getTerms() ) {
		t = t.canonic() ;
		if ( t.getLargestDegree() == 3 ) {
			rv.add(t);
		}
	}
	return rv;
}
/*
		@Override
		boolean passed() {
			if ( !contractionTest () ) {
				return false ;
			}
			GExpression ri = Tools.Riemann_lower(0, 1, 2, 3) ;
			List<GTerm> ts = ri.getTerms() ;
			GTerm[][] gtab = new GTerm[ts.size()/3][3] ;
			int index = 0 ;
			GExpression seconds = extractSecondDerivations ( ri ).canonic() ;
			GExpression secondsPerm = seconds.renameExternalIndices( 0,1,1,0).canonic() ;
			GExpression diff = new GExpression () ;
			diff.add(secondsPerm);
			diff.add(secondsPerm);
			diff = diff.canonic() ;
			if ( !Tools.testAntisymmetry( diff, 0, 1, 1, 0 )) {
				return false ;
			}
			if ( !Tools.testAntisymmetry( ri, 0, 1, 1, 0 )) {
				return false ;
			}

			diff = new GExpression () ;
			GExpression gld = Tools.GammaLower(1,2,0).derivate( 3 ).times( -1 )  ;
			GExpression gldPerm = Tools.GammaLower(1,3,0).derivate(2);
			diff.add(gld);
			diff.add(gldPerm);
			diff = diff.canonic() ;
			
			if ( !Tools.testAntisymmetry( diff, 0, 1, 1, 0 )) {
				return false ;
			}

			diff = new GExpression () ;
			gld = Tools.GammaUpper(1,2,4).derivate( 3 ).times( -1 )  ;
			gldPerm = Tools.GammaUpper(1,3,4).derivate(2);
			diff.add(gld);
			diff.add(gldPerm);
			GExpression diffd = diff.times( Tools.g_low(4, 0)) ;
			System.out.println ( "diffd.canonic");
			for ( GTerm t : diffd.getTerms() ) {
				System.out.println ( t.canonic().toString() ) ;
			}

			GExpression second = extractSecondDerivations(diffd) ;
			System.out.println ("second") ;
			for ( GTerm t : second.getTerms() ) {
				System.out.println ( t.canonic().toString() ) ;
			}
			System.out.println ("second.canonic") ;
			second = second.canonic() ;
			for ( GTerm t : second.getTerms() ) {
				System.out.println ( t.canonic().toString() ) ;
			}

			if ( !Tools.testAntisymmetry( second, 0, 1, 1, 0 )) {
				return false ;
			}
			return true ;
		}
*/
		/*
		private GExpression extractSecondDerivations(GExpression e) {
			List<GTerm> ts = e.getTerms() ;
			GExpression rv = new GExpression () ;
			for ( GTerm t : ts ) {
				if ( t.getLargestDegree() > 1 ) {
					rv.add ( t ) ;
				}
			}
			return rv;
		}
		*/
		
		@Override
		Vector<Test> getSubTests() {
			Vector<Test> rv = new Vector<Test> () ;
			rv.add( new RiemannSymmetryTest() ) ;
			rv.add( new TestCovG() ) ;
			rv.add( new BianchiIdent1() ) ;
			rv.add( new BianchiIdent2() ) ;
			rv.add( new RicciSymmetry() ) ;
			rv.add( new EinsteinSymmetry() ) ;
			rv.add( new EinsteinTensorRicci2() ) ;
			rv.add( new EinsteinTensorDivergence() ) ;
			return rv ;
		}

		@Override
		boolean passed() {
			return false;
		}
		
	}
	void testAll () {
		GExpression bianchi1 = new GExpression () ;// Bianchi 1
		bianchi1.add( Tools.Riemann_lower ( 0, 1, 2, 3) );
		bianchi1.add( Tools.Riemann_lower ( 0, 2, 3, 1) );
		bianchi1.add( Tools.Riemann_lower ( 0, 3, 1, 2) );
		bianchi1 = bianchi1.canonic() ;
		GExpression riemannSym1 = new GExpression () ;
		riemannSym1.add(Tools.Riemann_lower ( 0, 1, 2, 3) );
		riemannSym1.add(Tools.Riemann_lower ( 1, 0, 2, 3) );
		riemannSym1 = riemannSym1.canonic() ;
		GExpression riemannSym2 = new GExpression () ;
		riemannSym2.add(Tools.Riemann_lower ( 0, 1, 2, 3) );
		riemannSym2.add(Tools.Riemann_lower ( 2, 3, 0, 1).times( -1) );
		riemannSym2 = riemannSym2.canonic() ;
		GExpression bianchi2 = new GExpression ( ) ;
		bianchi2.add( Tools.cov ( Tools.Riemann_lower(0, 1, 2, 3), 4 ) ) ;
		bianchi2.add( Tools.cov ( Tools.Riemann_lower(0, 1, 3, 4), 2 ) ) ;
		bianchi2.add( Tools.cov ( Tools.Riemann_lower(0, 1, 4, 2), 3 ) ) ;
		bianchi2 = bianchi2.canonic() ;
	}
	
	@Override
	public String getName() {
		return "ToolsTest" ;
	}

	@Override
	boolean passed() {
		return false;
	}

	@Override
	Vector<Test> getSubTests() {
		Vector<Test> rv = new Vector<Test> () ;
		rv.add(new GammaTest()) ;
		rv.add(new RiemannTest() ) ;
		return rv;
	}

	/*
	@Override
	public boolean passed() {
		return testAll () ;
	}*/
	
	public static void main ( String[] args ) {
		new ToolsTest ().run(0) ;
	}


}
