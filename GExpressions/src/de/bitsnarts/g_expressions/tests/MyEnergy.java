package de.bitsnarts.g_expressions.tests;

import java.util.Vector;

import de.bitsnarts.g_expressions.GExpression;
import de.bitsnarts.g_expressions.GTerm;
import de.bitsnarts.g_expressions.Tools;

public class MyEnergy extends Test {

	@Override
	String getName() {
		return "MyEnergy" ;
	}

	GExpression innerTerm () {
		GExpression rv = new GExpression () ;
		rv.add(Tools.g_upper(3, 0).times(Tools.GammaUpper(3, 4, 4).derivate(2) ));
		rv.add( Tools.g_upper( -1, 4, 5).times( Tools.GammaUpper(4, 5, 0).derivate(2)));
		System.out.println ( rv.asLatex(true, getName())) ;
		rv = rv.canonic() ;
		rv = rv.derivate(0) ;
		rv = rv.canonic() ;
		return rv ;
	} 
	
	GExpression myTensor () {
		GExpression rv = gravitationalPart () ;
		rv.add( Tools.Einstein().times( Tools.g_low(1, 2)));
		//System.out.println ( rv.asLatex(true, getName())) ;
		rv = rv.canonic() ;
		return rv ;
	}
	
	private GExpression gravitationalPart() {
		GExpression rv = new GExpression () ;
		rv.add(Tools.g_upper(3, 0).times(Tools.GammaUpper(3, 4, 4).derivate(2) ));
		rv.add( Tools.g_upper( -1, 4, 5).times( Tools.GammaUpper(4, 5, 0).derivate(2)));
		GExpression R = Tools.RicciHigh ( 0, 1 ).times(Tools.g_low(0, 1)) ;
		GTerm delta = Tools.g_upper(0, 1).times( Tools.g_low(1, 2));
		rv.add( R.times( delta));
		rv = rv.times( 0.5 ) ;
		return rv ;
	}

	GExpression myTensor2 () {
		GExpression rv = new GExpression () ;
		rv.add(Tools.g_upper(3, 0).times(Tools.GammaUpper(3, 4, 4).derivate(2) ));
		rv.add( Tools.g_upper( -1, 4, 5).times( Tools.GammaUpper(4, 5, 0).derivate(2)));
		rv = rv.times( 0.5 ) ;
		rv.add( Tools.RicciHigh(0,1).times( Tools.g_low(1, 2)));
		//System.out.println ( rv.asLatex(true, getName())) ;
		rv = rv.canonic() ;
		return rv ;
	}
	
	GExpression myTensorUpper () {
		return myTensor2().times ( Tools.g_upper(2, 1) ).canonic() ;
	}
	

	class DivTest extends Test {

		@Override
		String getName() {
			return "DivTest";
		}

		@Override
		boolean passed() {
			//innerTerm () ;
			//rCov () ;
			sym () ;
			if ( !cmp2 () ) {
				return false ;
			}
			if ( !cmp3 () ) {
				return false ;
			}
			if ( !cmp () )
				return false ;
			if ( symDiv ( ) )
				return false ;
			System.out.println ( gravitationalPart().times( Tools.g_low (0, 1)).canonic().asLatex( "A"));
			GExpression mt = myTensor () ;
			GExpression d = new GExpression () ;
			d.add( mt.derivate(0 ));
			GExpression gd = Tools.g_upper(0.5, 1, 2).times( Tools.g_low(1, 2).derivate(0)) ;
			d.add( gd.times(mt));
			d = d.canonic() ;
			return d.isNull();
		}

		@Override
		Vector<Test> getSubTests() {
			return null;
		}
		
	}
	@Override
	boolean passed() {
		return false;
	}

	public boolean symDiv() {
		GExpression mt = myTensorUpper () ;
		mt = mkAsym ( mt, 0, 1, 1, 0 ) ;
		GExpression d = new GExpression () ;
		d.add( mt.derivate(0 ));
		GExpression gd = Tools.g_upper(0.5, 4, 5).times( Tools.g_low(4, 5).derivate(0)) ;
		d.add( gd.times(mt));
		d = d.canonic() ;
		return d.isNull();
	}

	public boolean cmp() {
		GExpression e = new GExpression () ;
		e.add( myTensor () );
		e.add( myTensor2 ().times( -1 ) );
		e = e.canonic() ;
		return e.isNull() ;
	}

	private boolean cmp2() {
		GExpression R = Tools.RicciHigh ( 0, 1 ).times(Tools.g_low(0, 1)) ;
		GExpression e2 = Tools.RicciHigh(0, 1) ;
		e2.add( Tools.g_upper(-0.5, 0, 1).times(R));
		e2.add( Tools.Einstein().times( -1 ) );
		e2 = e2.canonic() ;
		return e2.isNull() ;
	}

	public boolean cmp3() {
		GExpression rv = new GExpression () ;
		GExpression R = Tools.RicciHigh ( 0, 1 ).times(Tools.g_low(0, 1)) ;
		GTerm delta = Tools.g_upper(0, 1).times( Tools.g_low(1, 2));
		rv.add( R.times( delta));
		rv = rv.times( 0.5 ) ;
		rv.add( Tools.Einstein().times( Tools.g_low(1, 2)));
		
		rv.add( Tools.RicciHigh(0, 1).times( Tools.g_low (-1, 1, 2)));
		rv = rv.canonic() ;
		return rv.isNull() ;
	}


	public boolean sym() {
		GExpression mt = myTensor ().times(Tools.g_upper(2, 1)) ;
		GExpression as = mkAsym ( mt, 0,1,1,0 ) ;
		System.out.println ( as.asLatex( "A" ) ) ;
		mt = mkSym ( mt, 0, 1, 1, 0 ) ;
		return mt.isNull() ;
	}

	private GExpression mkSym(GExpression e, int ... pairs ) {
		GExpression rv = new GExpression () ;
		rv.add( e ) ;
		rv.add( e.renameExternalIndices(pairs));
		return rv.canonic() ;
	}

	private GExpression mkAsym(GExpression e, int ... pairs ) {
		GExpression rv = new GExpression () ;
		rv.add( e ) ;
		rv.add( e.renameExternalIndices(pairs).times(-1 ));
		return rv.canonic() ;
	}

	public void rCov() {
		GExpression R = Tools.RicciHigh ( 0, 1 ).times(Tools.g_low(0, 1)) ;
		GExpression RC = R.cov( 0 ).canonic() ;
		GExpression RD = R.derivate(0).canonic() ;
		RD.add( RC.times( -1 ) ) ;
		RD = RD.canonic() ;
	}

	@Override
	Vector<Test> getSubTests() {
		Vector<Test> rv = new  Vector<Test> () ;
		rv.add( new DivTest() ) ;
		return rv;
	}

	public static void main ( String[] args ) {
		new MyEnergy().run(0) ;
	}
}
