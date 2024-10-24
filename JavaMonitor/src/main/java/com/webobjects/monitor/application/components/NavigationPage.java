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
		return siteConfig() != null && (mySession().isLoggedIn() && siteConfig().isPasswordRequired());
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
		items.add( new MenuItem( "Applications", Icon.Home, this::ApplicationsPageClicked ) );
		items.add( new MenuItem( "Hosts", Icon.Home, this::HostsPageClicked ) );
		items.add( new MenuItem( "Site", Icon.Home, this::ConfigurePageClicked ) );
		items.add( new MenuItem( "Preferences", Icon.Home, this::PrefsPageClicked ) );

		if( showModProxyTab() ) {
			items.add( new MenuItem( "mod_proxy", Icon.Home, this::ModProxyPageClicked) );
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
	 * SVG icons we can use for our menuitems
	 */
	public enum Icon {
		Home( """
				<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 12l-2 0l9 -9l9 9l-2 0" /><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7" /><path d="M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6" /></svg>
			""" );

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
		mySession().setIsLoggedIn( false );
		return pageWithName( Main.class );
	}
}