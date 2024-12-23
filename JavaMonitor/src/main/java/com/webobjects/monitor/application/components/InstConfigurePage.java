package com.webobjects.monitor.application.components;

import java.util.List;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor.application.MonitorComponent.InstComponent;

public class InstConfigurePage extends InstComponent {

	public InstConfigurePage( WOContext aWocontext ) {
		super( aWocontext );
	}

	public boolean isWindowsHost() {
		return myInstance().host().osType().equals( "WINDOWS" );
	}

	public WOComponent returnClicked() {
		return AppDetailPage.create( context(), myInstance().application() );
	}

	public WOComponent appConfigLinkClicked() {
		AppConfigurePage aPage = AppConfigurePage.create( context(), myInstance().application() );
		aPage.isNewInstanceSectionVisible = true;
		return aPage;
	}

	private WOComponent _pathPickerWizardClicked( String callbackKeyPath ) {
		PathWizardPage1 aPage = PathWizardPage1.create( context() );
		aPage.setCallbackKeypath( callbackKeyPath );
		aPage.setCallbackPage( this );
		aPage.setShowFiles( true );
		return aPage;
	}

	public WOComponent pathPickerWizardClicked() {
		return _pathPickerWizardClicked( "myInstance.path" );
	}

	public WOComponent pathPickerWizardClickedOutput() {
		return _pathPickerWizardClicked( "myInstance.outputPath" );
	}

	public Integer port() {
		return myInstance().port();
	}

	public void setPort( Integer value ) {
		if( value != null ) {
			if( !value.equals( myInstance().port() ) ) {
				if( myInstance().state != MObject.DEAD ) {
					session().addErrorIfAbsent( "This instance is still running; unable to change port" );
					return;
				}

				if( !myInstance().host().isPortInUse( value ) ) {
					myInstance().setPort( value );
				}
				else {
					session().addErrorIfAbsent( "This port is in use" );
				}
			}
		}
	}

	public Integer id() {
		return myInstance().id();
	}

	public void setId( Integer value ) {
		if( value != null ) {
			if( !value.equals( myInstance().id() ) ) {
				if( !myInstance().application().isIDInUse( value ) ) {
					myInstance().setId( value );
				}
				else {
					session().addErrorIfAbsent( "This ID is in use" );
				}
			}
		}
	}

	public String displayName() {
		return myInstance().displayName();
	}

	public void setDisplayName( Object foo ) {
		// ak: should switch to non-sync
	}

	public WOComponent startupUpdateClicked() {
		handler().startReading();
		try {
			handler().sendUpdateInstancesToWotaskds( List.of( myInstance() ), siteConfig().hostArray() );
		}
		finally {
			handler().endReading();
		}
		return null;
	}

	public WOComponent adaptorSettingsUpdateClicked() {
		handler().startReading();
		try {
			handler().sendUpdateInstancesToWotaskds( List.of( myInstance() ), siteConfig().hostArray() );
		}
		finally {
			handler().endReading();
		}

		return null;
	}

	/**
	 * Settings differing from application defaults are marked with this HTML string
	 */
	private static String DIFF = "<span class=\"Warning\">**</span>";

	/**
	 * FIXME: OK. This is actually required it treats an empty string and null as equals. That's... bad and should really be fixed at the configuration side // Hugi 2024-10-31
	 */
	private static boolean safeEquals( Object a, Object b ) {
		if( (a == null) && (b == null) ) {
			return true;
		}
		else if( (a != null) && (b != null) ) {
			return a.equals( b );
		}
		// only 1 of the 2 is null
		if( (a instanceof String) || (b instanceof String) ) {
			if( (a == null && b != null && ((String)b).length() == 0) || (b == null && a != null && ((String)a).length() == 0) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The difference indicator HTML marker string, if the two objects are not considered "equal" (in Monitor's sense)
	 */
	private static String diffString( Object a, Object b ) {
		return !safeEquals( a, b ) ? DIFF : "";
	}

	/**
	 * FIXME: Introduce an enum in MHost for the type, so we can use an exhaustive switch for those things // Hugi 2024-10-31
	 */
	public String pathDiff() {
		
		final MInstance anInstance = myInstance();
		final MApplication anApplication = anInstance.application();
		final MHost aHost = anInstance.host();
		
		String appPath = null;

		if( aHost.osType().equals( "UNIX" ) ) {
			appPath = anApplication.unixPath();
		}
		else if( aHost.osType().equals( "WINDOWS" ) ) {
			appPath = anApplication.winPath();
		}
		else if( aHost.osType().equals( "MACOSX" ) ) {
			appPath = anApplication.macPath();
		}

		return diffString( anInstance.path(), appPath );
	}

	/**
	 * FIXME: Introduce an enum in MHost for the type, so we can use an exhaustive switch for those things // Hugi 2024-10-31
	 */
	public String outputDiff() {

		final MInstance anInstance = myInstance();
		final MApplication anApplication = anInstance.application();
		final MHost aHost = anInstance.host();

		String appOutputPath = null;

		if( aHost.osType().equals( "UNIX" ) ) {
			appOutputPath = anInstance.generateOutputPath( anApplication.unixOutputPath() );
		}
		else if( aHost.osType().equals( "WINDOWS" ) ) {
			appOutputPath = anInstance.generateOutputPath( anApplication.winOutputPath() );
		}
		else if( aHost.osType().equals( "MACOSX" ) ) {
			appOutputPath = anInstance.generateOutputPath( anApplication.macOutputPath() );
		}

		return diffString( anInstance.outputPath(), appOutputPath );
	}

	public String minDiff() {
		return diffString( myInstance().minimumActiveSessionsCount(), myInstance().application().minimumActiveSessionsCount() );
	}

	public String cachingDiff() {
		return diffString( myInstance().cachingEnabled(), myInstance().application().cachingEnabled() );
	}

	public String browserDiff() {
		return diffString( myInstance().autoOpenInBrowser(), myInstance().application().autoOpenInBrowser() );
	}

	public String debugDiff() {
		return diffString( myInstance().debuggingEnabled(), myInstance().application().debuggingEnabled() );
	}

	public String lifebeatDiff() {
		return diffString( myInstance().lifebeatInterval(), myInstance().application().lifebeatInterval() );
	}

	public String argsDiff() {
		return diffString( myInstance().additionalArgs(), myInstance().application().additionalArgs() );
	}

	public WOComponent forceQuitClicked() {
		handler().sendQuitInstancesToWotaskds( List.of( myInstance() ), List.of( myInstance().host() ) );
		return null;
	}

	public String instanceLifebeatInterval() {
		return myInstance().lifebeatInterval().toString();
	}

	public static InstConfigurePage create( WOContext context, MInstance instance ) {
		InstConfigurePage page = (InstConfigurePage)context.page().pageWithName( InstConfigurePage.class.getName() );
		page.setMyInstance( instance );
		return page;
	}
}