package com.webobjects.monitor.application;

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
import com.webobjects.monitor.util.Util;

public class PathWizardPage2 extends MonitorComponent {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 4910748680027936695L;

	public MHost host;

	public String callbackKeypath;

	public String callbackExpand;

	public WOComponent callbackPage;

	public String aPath = null;

	public boolean showFiles = true;

	public PathWizardPage2( WOContext aWocontext ) {
		super( aWocontext );
	}

	public void setHost( MHost aHost ) {
		host = aHost;
	}

	public void setCallbackKeypath( String aValue ) {
		callbackKeypath = aValue;
		if( myApplication() != null ) {
			String key = Util.lastPropertyKeyInKeyPath( callbackKeypath );
			aPath = (String)myApplication().valueForKey( key );
		}
	}

	public void setCallbackExpand( String aValue ) {
		callbackExpand = aValue;
	}

	public void setCallbackPage( WOComponent aValue ) {
		callbackPage = aValue;
	}

	public void setShowFiles( boolean aValue ) {
		showFiles = aValue;
	}

	public WOComponent updateClicked() {
		return this;
	}

	public WOComponent selectionClicked() {
		// May have to do explicit update here!
		WOComponent aPage = callbackPage;
		aPage.takeValueForKeyPath( aPath, callbackKeypath );

		if( callbackExpand != null ) {
			aPage.takeValueForKey( Boolean.TRUE, callbackExpand );
		}
		return aPage;
	}

	public static PathWizardPage2 create( WOContext context, MApplication application ) {
		PathWizardPage2 aPage = (PathWizardPage2)context.page().pageWithName( PathWizardPage2.class.getName() );
		aPage.setMyApplication( application );
		return aPage;
	}
}