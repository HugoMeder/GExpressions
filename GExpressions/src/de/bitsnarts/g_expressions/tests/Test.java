package de.bitsnarts.g_expressions.tests;

import java.util.Vector;

public abstract class Test {
	abstract String getName () ;
	abstract boolean passed () ;
	abstract Vector<Test> getSubTests () ;
	boolean run ( int level ) {
		Vector<Test> ts = getSubTests () ;
		boolean b ;
		if  ( ts == null || ts.isEmpty() ) {
			try {
				b = passed () ;
			} catch ( Throwable th ) {
				System.out.println ( th.toString() ) ;
				b = false ;
			}
		} else {
			b = true ;
			ident ( level ) ;
			System.out.println ( getName()+" started" ) ;
			for ( Test t : ts ) {
				boolean b1 ;
				try {
					b1 = t.run( level+1 ) ;
				} catch ( Throwable th ) {
					System.out.println ( th.toString() ) ;
					b1 = false ;
				}
				if ( !b1 ) {
					b = false ;
				}
			}
		}
		ident ( level ) ;
		if ( b ) {
			System.out.println ( getName()+" passed" ) ;
		} else {
			System.out.println ( getName()+" failed" ) ;
		}
		return b ;
	}
	
	private void ident(int level) {
		for ( int l = 0 ; l < level ; l++ ) {
			System.out.print ("\t" ) ;
		}
	}
}
