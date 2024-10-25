package com.webobjects.monitor.application.components;

import java.util.HashMap;
import java.util.Map;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class JMTablerLook extends NavigationPage {

	public String searchString;

	public JMTablerLook( WOContext context ) {
		super( context );
	}

	/**
	 * Keeping this around for configurability. "layout-fluid" will give us a full-width layout, while [null] will box us in
	 */
	public String bodyClass() {
		return "layout-fluid";
		//		return null;
	}

	public Map user() {
		return new HashMap<>();
	}

	public String avatarBackgroundStyle() {
		final String url = application().resourceManager().urlForResourceNamed( "images/avatar.png", "app", NSArray.emptyArray(), context().request() );
		return "background-image: url(%s)".formatted( url );
	}
	
	public boolean notSelected() {
		return currentPage != currentMenuItem.id();
	}
}