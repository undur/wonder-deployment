package com.webobjects.monitor.application.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.webobjects.appserver.WOActionResults;
/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.monitor.application.MonitorComponent;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXProperties;

public class NavigationPage extends MonitorComponent {

	/**
	 * Bound to by wrapped components to set the actual page <title>
	 */
	public String title;

	/**
	 * FIXME: wat?
	 */
	public int currentPage = APP_PAGE;
	
	/**
	 * FIXME: wat?
	 */
	public String pageId;

	/**
	 * Item currently being iterated over in the top menu
	 */
	public MenuItem currentMenuItem;

	public NavigationPage( WOContext aWocontext ) {
		super( aWocontext );
	}

	/**
	 * FIXME: Don't think we need to include prototype.js everywhere. Figure out use sites and include there // Hugi 2024-10-24
	 */
	@Override
	public void appendToResponse( WOResponse response, WOContext context ) {
		super.appendToResponse( response, context );
		AjaxUtils.addScriptResourceInHead( context, response, "Ajax", "prototype.js" );
	}

	/**
	 * @return Display name for the <title> tag
	 */
	public String pageTitle() {
		return "WOMonitor: " + title;
	}

	/**
	 * @return true if logout is possible in the given context
	 */
	public boolean logoutRequired() {
		return siteConfig() != null && (session().isLoggedIn() && siteConfig().isPasswordRequired());
	}

	/**
	 * FIXME: I have no idea why we'd ever like to keep this tab secret // Hugi 2024-10-24 
	 */
	public boolean showModProxyTab() {
		return ERXProperties.booleanForKeyWithDefault( "er.javamonitor.showModProxyTab", false );
	}

	/**
	 * @return The action result of clicking the current menuitem
	 * 
	 * FIXME: Preferable we'd just use the supplied value in the link's action attribute. Unfortunately, KVC throws a fit when it sees the lambda. We'd like to try to fix that (but that must happen at the KVC level) // Hugi 2024-10-24
	 */
	public WOActionResults currentMenuItemClicked() {
		return currentMenuItem.supplier.get();
	}

	/**
	 * Items in the top menubar 
	 */
	public List<MenuItem> menuItems() {
		final ArrayList<MenuItem> items = new ArrayList<>();
		items.add( new MenuItem( "Applications", Icon.Cube, this::ApplicationsPageClicked ) );
		items.add( new MenuItem( "Hosts", Icon.Server, this::HostsPageClicked ) );
		items.add( new MenuItem( "Site", Icon.Home, this::ConfigurePageClicked ) );
		items.add( new MenuItem( "Preferences", Icon.Adjustments, this::PrefsPageClicked ) );
		items.add( new MenuItem( "Help", Icon.Help, this::HelpPageClicked ) );

		if( showModProxyTab() ) {
			items.add( new MenuItem( "mod_proxy", Icon.Polygon, this::ModProxyPageClicked) );
		}

		if( logoutRequired() ) {
			items.add( new MenuItem( "Logout", Icon.Home, this::logoutClicked ) );
		}

		return items;
	}

	/**
	 * Represents a menuitem in the top menubar
	 */
	public record MenuItem( String name, Icon icon, Supplier<WOComponent> supplier ) {}

	/**
	 * SVG icons we can use for our menuitems. hijacked from https://tabler.io/icons
	 */
	public enum Icon {
		Cube( """
				<svg  xmlns="http://www.w3.org/2000/svg"  width="24"  height="24"  viewBox="0 0 24 24"  fill="none"  stroke="currentColor"  stroke-width="2"  stroke-linecap="round"  stroke-linejoin="round"  class="icon icon-tabler icons-tabler-outline icon-tabler-cube"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M21 16.008v-8.018a1.98 1.98 0 0 0 -1 -1.717l-7 -4.008a2.016 2.016 0 0 0 -2 0l-7 4.008c-.619 .355 -1 1.01 -1 1.718v8.018c0 .709 .381 1.363 1 1.717l7 4.008a2.016 2.016 0 0 0 2 0l7 -4.008c.619 -.355 1 -1.01 1 -1.718z" /><path d="M12 22v-10" /><path d="M12 12l8.73 -5.04" /><path d="M3.27 6.96l8.73 5.04" /></svg>
				"""),
		Polygon("""
				<svg  xmlns="http://www.w3.org/2000/svg"  width="24"  height="24"  viewBox="0 0 24 24"  fill="none"  stroke="currentColor"  stroke-width="2"  stroke-linecap="round"  stroke-linejoin="round"  class="icon icon-tabler icons-tabler-outline icon-tabler-polygon"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M12 5m-2 0a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" /><path d="M19 8m-2 0a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" /><path d="M5 11m-2 0a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" /><path d="M15 19m-2 0a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" /><path d="M6.5 9.5l3.5 -3" /><path d="M14 5.5l3 1.5" /><path d="M18.5 10l-2.5 7" /><path d="M13.5 17.5l-7 -5" /></svg>
				"""),
		Help("""
				<svg  xmlns="http://www.w3.org/2000/svg"  width="24"  height="24"  viewBox="0 0 24 24"  fill="none"  stroke="currentColor"  stroke-width="2"  stroke-linecap="round"  stroke-linejoin="round"  class="icon icon-tabler icons-tabler-outline icon-tabler-help"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0" /><path d="M12 17l0 .01" /><path d="M12 13.5a1.5 1.5 0 0 1 1 -1.5a2.6 2.6 0 1 0 -3 -4" /></svg>
				"""),
		Home( """
				<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 12l-2 0l9 -9l9 9l-2 0" /><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7" /><path d="M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6" /></svg>
			""" ),
		Adjustments("""
				<svg  xmlns="http://www.w3.org/2000/svg"  width="24"  height="24"  viewBox="0 0 24 24"  fill="none"  stroke="currentColor"  stroke-width="2"  stroke-linecap="round"  stroke-linejoin="round"  class="icon icon-tabler icons-tabler-outline icon-tabler-adjustments"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M4 10a2 2 0 1 0 4 0a2 2 0 0 0 -4 0" /><path d="M6 4v4" /><path d="M6 12v8" /><path d="M10 16a2 2 0 1 0 4 0a2 2 0 0 0 -4 0" /><path d="M12 4v10" /><path d="M12 18v2" /><path d="M16 7a2 2 0 1 0 4 0a2 2 0 0 0 -4 0" /><path d="M18 4v1" /><path d="M18 9v11" /></svg>
				"""),
		Server("""
				<svg  xmlns="http://www.w3.org/2000/svg"  width="24"  height="24"  viewBox="0 0 24 24"  fill="none"  stroke="currentColor"  stroke-width="2"  stroke-linecap="round"  stroke-linejoin="round"  class="icon icon-tabler icons-tabler-outline icon-tabler-server"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M3 4m0 3a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v2a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3z" /><path d="M3 12m0 3a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v2a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3z" /><path d="M7 8l0 .01" /><path d="M7 16l0 .01" /></svg>
				""");

		// SVG path for the icon
		String _svg;

		Icon( String svg ) {
			_svg = svg;
		}

		public String svg() {
			return _svg;
		}
	}
	
	@Deprecated
	public WOComponent ApplicationsPageClicked() {
		return ApplicationsPage.create( context() );
	}

	@Deprecated
	public WOComponent HostsPageClicked() {
		return HostsPage.create( context() );
	}

	@Deprecated
	public WOComponent ConfigurePageClicked() {
		return ConfigurePage.create( context() );
	}

	@Deprecated
	public WOComponent PrefsPageClicked() {
		return PrefsPage.create( context() );
	}

	@Deprecated
	public WOComponent HelpPageClicked() {
		return pageWithName( HelpPage.class );
	}

	@Deprecated
	public WOComponent ModProxyPageClicked() {
		return pageWithName( ModProxyPage.class );
	}

	@Deprecated
	public WOComponent logoutClicked() {
		session().setIsLoggedIn( false );
		return pageWithName( Main.class );
	}
}