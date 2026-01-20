package de.bitsnarts.g_expressions;

import java.io.Serializable;

public class Derivation implements Serializable {
	final int degree;
	//Contraction low[] = new Contraction[2] ;
	//Contraction deriv[] ;
	int nLow ;
	int nDeriv ;

	Derivation ( int degree ) {
		this.degree = degree ;
		//deriv = new Contraction[degree] ;
	}
	
	void add ( Contraction index ) {
		if ( !index.isContracting() )
			throw new Error ( "!index.isContracting()" ) ;
		if ( index.bindsDerivation() ) {
			if ( nDeriv == degree )
				throw new Error ("nDeriv == degree") ;
			//deriv[nDeriv++] = index ;
			nDeriv++ ;
		} else {
			if ( nLow == 2 )
				throw new Error ("nDeriv == degree") ;
			//low[nLow++] = index ;
			nLow++ ;
		}
	}
	
	boolean isComplete () {
		return nLow == 2 && nDeriv == degree ;
	}
}
