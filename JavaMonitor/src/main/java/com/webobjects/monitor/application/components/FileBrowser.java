package com.webobjects.monitor.application.components;

import com.webobjects.appserver.WOApplication;
/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.monitor._private.CoderWrapper;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MonitorException;
import com.webobjects.monitor.application.Application;
import com.webobjects.monitor.application.MonitorComponent;
import com.webobjects.monitor.util.WOTaskdHandler;

public class FileBrowser extends MonitorComponent {

	public String startingPath; // passed in
	public String callbackUpdateAction; // passed in
	public String callbackSelectionAction; // passed in
	public MHost host; // passed in
	public boolean showFiles = true;
	public boolean isRoots = false;
	public String errorMsg;
	public NSDictionary aCurrentFile;
	public NSArray _fileList = null;

	public FileBrowser( WOContext aWocontext ) {
		super( aWocontext );
	}

	public boolean hasErrorMsg() {
		return errorMsg != null && errorMsg.length() > 0;
	}

	public NSArray fileList() {
		if( _fileList == null ) {
			retrieveFileList();
		}

		return _fileList;
	}

	private String retrieveFileList() {
		try {
			NSDictionary aDict = RemoteBrowseClient.fileListForStartingPathHost( startingPath, host, showFiles );
			_fileList = (NSArray)aDict.valueForKey( "fileArray" );
			isRoots = (aDict.valueForKey( "isRoots" ) != null) ? true : false;
			startingPath = (String)aDict.valueForKey( "filepath" );
			errorMsg = null;
		}
		catch( MonitorException me ) {
			if( isRoots )
				startingPath = null;
			NSLog.err.appendln( "Path Wizard Error: " + me.getMessage() );
			me.printStackTrace();
			errorMsg = me.getMessage();
		}
		return errorMsg;
	}

	@Override
	public void appendToResponse( WOResponse response, WOContext context ) {
		fileList(); // init variable
		super.appendToResponse( response, context );
	}

	public boolean isCurrentFileDirectory() {
		String aString = (String)aCurrentFile.valueForKey( "fileType" );
		if( aString.equals( "NSFileTypeDirectory" ) ) {
			return true;
		}
		return false;
	}

	public Object backClicked() {
		String originalPath = startingPath;
		startingPath = NSPathUtilities.stringByDeletingLastPathComponent( startingPath );
		startingPath = NSPathUtilities._standardizedPath( startingPath );
		if( startingPath.equals( "" ) || (originalPath.equals( startingPath )) ) {
			startingPath = null;
		}
		if( retrieveFileList() != null ) {
			startingPath = originalPath;
		}
		return performParentAction( callbackUpdateAction );
	}

	public Object directoryClicked() {
		String originalPath = startingPath;
		String aFile = (String)aCurrentFile.valueForKey( "file" );
		startingPath = NSPathUtilities.stringByAppendingPathComponent( startingPath, aFile );
		startingPath = NSPathUtilities._standardizedPath( startingPath );
		retrieveFileList();
		if( retrieveFileList() != null ) {
			startingPath = originalPath;
		}
		return performParentAction( callbackUpdateAction );
	}

	public Object jumpToClicked() {
		String originalPath = startingPath;
		startingPath = NSPathUtilities._standardizedPath( startingPath );
		retrieveFileList();
		if( retrieveFileList() != null ) {
			startingPath = originalPath;
		}
		return performParentAction( callbackUpdateAction );
	}

	public Object selectClicked() {
		String aFile = (String)aCurrentFile.valueForKey( "file" );
		startingPath = NSPathUtilities.stringByAppendingPathComponent( startingPath, aFile );
		startingPath = NSPathUtilities._standardizedPath( startingPath );
		return performParentAction( callbackSelectionAction );
	}

	public Object selectCurrentDirClicked() {
		startingPath = NSPathUtilities._standardizedPath( startingPath );
		return performParentAction( callbackSelectionAction );
	}

	private static class RemoteBrowseClient {

		private static byte[] evilHack = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>".getBytes();

		private static NSDictionary _getFileListOutOfResponse( WOResponse aResponse, String thePath ) throws MonitorException {
			NSData responseContent = aResponse.content();
			NSArray anArray = NSArray.EmptyArray;
			if( responseContent != null ) {
				byte[] responseContentBytes = responseContent.bytes();
				String responseContentString = new String( responseContentBytes );

				if( responseContentString.startsWith( "ERROR" ) ) {
					throw new MonitorException( "Path " + thePath + " does not exist" );
				}

				CoderWrapper aDecoder = new CoderWrapper();

				try {
					byte[] evilHackCombined = new byte[responseContentBytes.length + evilHack.length];
					// System.arraycopy(src, src_pos, dst, dst_pos, length);
					System.arraycopy( evilHack, 0, evilHackCombined, 0, evilHack.length );
					System.arraycopy( responseContentBytes, 0, evilHackCombined, evilHack.length, responseContentBytes.length );
					anArray = (NSArray)aDecoder.decodeRootObject( new NSData( evilHackCombined ) );
				}
				catch( WOXMLException wxe ) {
					NSLog.err.appendln( "RemoteBrowseClient _getFileListOutOfResponse Error decoding response: " + responseContentString );
					throw new MonitorException( "Host returned bad response for path " + thePath );
				}

			}
			else {
				NSLog.err.appendln( "RemoteBrowseClient _getFileListOutOfResponse Error decoding null response" );
				throw new MonitorException( "Host returned null response for path " + thePath );
			}

			String isRoots = aResponse.headerForKey( "isRoots" );
			String filepath = aResponse.headerForKey( "filepath" );

			NSMutableDictionary aDict = new NSMutableDictionary();
			aDict.takeValueForKey( isRoots, "isRoots" );
			aDict.takeValueForKey( filepath, "filepath" );
			aDict.takeValueForKey( anArray, "fileArray" );

			return aDict;
		}

		private static String getPathString = "/cgi-bin/WebObjects/wotaskd.woa/wa/RemoteBrowse/getPath";

		/**
		 * FIXME: Switch to java http client // Hugi 2024-11-01
		 */
		public static NSDictionary fileListForStartingPathHost( String aString, MHost aHost, boolean showFiles ) throws MonitorException {

			NSDictionary aFileListDictionary = null;

			try {
				Application theApplication = (Application)WOApplication.application();
				WOHTTPConnection anHTTPConnection = new WOHTTPConnection( aHost.name(), theApplication.lifebeatDestinationPort() );
				@SuppressWarnings("cast")
				NSMutableDictionary<String, NSMutableArray<String>> aHeadersDict = (NSMutableDictionary<String, NSMutableArray<String>>)WOTaskdHandler.siteConfig().passwordDictionary().mutableClone();
				WORequest request = null;
				WOResponse response = null;
				boolean requestSucceeded = false;

				if( aString != null && aString.length() > 0 ) {
					aHeadersDict.setObjectForKey( new NSMutableArray<>( aString ), "filepath" );
				}
				if( showFiles ) {
					aHeadersDict.setObjectForKey( new NSMutableArray<>( "YES" ), "showFiles" );
				}

				request = new WORequest( MObject._GET, RemoteBrowseClient.getPathString, MObject._HTTP1, aHeadersDict, null, null );
				anHTTPConnection.setReceiveTimeout( 5000 );

				requestSucceeded = anHTTPConnection.sendRequest( request );

				if( requestSucceeded ) {
					response = anHTTPConnection.readResponse();
				}

				if( response == null || !requestSucceeded || response.status() != 200 ) {
					throw new MonitorException( "Error requesting directory listing for " + aString + " from " + aHost.name() );
				}

				try {
					aFileListDictionary = _getFileListOutOfResponse( response, aString );
				}
				catch( MonitorException me ) {
					if( NSLog.debugLoggingAllowedForLevelAndGroups( NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment ) ) {
						NSLog.debug.appendln( "caught exception: " + me );
					}

					throw me;
				}

				aHost.isAvailable = true;
			}
			catch( MonitorException me ) {
				aHost.isAvailable = true;
				throw me;
			}
			catch( Exception localException ) {
				aHost.isAvailable = false;
				NSLog.err.appendln( "Exception requesting directory listing: " );
				localException.printStackTrace();
				throw new MonitorException( "Exception requesting directory listing for " + aString + " from " + aHost.name() + ": " + localException.toString() );
			}

			return aFileListDictionary;
		}
	}
}