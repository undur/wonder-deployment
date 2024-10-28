package com.webobjects.monitor.util;

import com.webobjects.monitor._private.MInstance;

/**
 * Home for some experimental functionality (that will probably get a different home later
 */

public class ExperimentalUtilities {

	public static String jstack( final MInstance instance ) {
		System.out.println( instance.host().osType() );
		return "Hahaha";
	}
}