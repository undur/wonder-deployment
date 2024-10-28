package com.webobjects.monitor.application.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor.application.MonitorComponent;
import com.webobjects.monitor.util.ExperimentalUtilities;

import er.extensions.appserver.ERXApplication;

public class InstDetailPage extends MonitorComponent {

	public InstDetailPage( WOContext context ) {
		super( context );
	}

	public WOActionResults jstack() {
		ExperimentalUtilities.jstack( myInstance() );
		return null;
	}

	public WOComponent returnClicked() {
		return AppDetailPage.create( context(), myInstance().application() );
	}

	public static InstDetailPage create( final WOContext context, final MInstance instance ) {
		final InstDetailPage page = ERXApplication.erxApplication().pageWithName( InstDetailPage.class, context );
		page.setMyInstance( instance );
		return page;
	}
}