/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXProperties;

public class MHost extends MObject {

	private static final Logger logger = LoggerFactory.getLogger( MHost.class );

	private final int _receiveTimeout = ERXProperties.intForKeyWithDefault( "JavaMonitor.receiveTimeout", 10000 );

	public String name() {
		return (String)values.valueForKey( "name" );
	}

	public void setName( String value ) {
		values.takeValueForKey( value, "name" );
		_siteConfig.dataHasChanged();
	}

	public String osType() {
		return (String)values.valueForKey( "type" );
	}

	public void setOsType( String value ) {
		values.takeValueForKey( MObject.validatedHostType( value ), "type" );
		_siteConfig.dataHasChanged();
	}

	private NSMutableArray<MInstance> _instanceArray;

	/**
	 * FIXME: It doesn't look like this variable is ever actually read in a meaningful way. Delete? // Hugi 2024-11-02
	 */
	@Deprecated
	private NSMutableArray<MApplication> _applicationArray = new NSMutableArray<>();

	public NSMutableArray<MInstance> instanceArray() {
		return _instanceArray;
	}

	// From the UI
	public MHost( MSiteConfig aConfig, String name, String type ) {
		this( new NSDictionary<Object, Object>( new Object[] { name, type }, new Object[] { "name", "type" } ), aConfig );
	}

	// Unarchiving or Monitor Update
	public MHost( NSDictionary aValuesDict, MSiteConfig aConfig ) {
		values = new NSMutableDictionary( aValuesDict );
		_siteConfig = aConfig;
		_instanceArray = new NSMutableArray<>();

		int tries = 0;
		while( tries++ < 5 ) {
			try {
				_address = InetAddress.getByName( name() );
				break;
			}
			catch( UnknownHostException anException ) {
				// AK: From *my* POV, we should check if this is the localhost and exit if it is,
				// as I had this happen when you set -WOHost something and DNS isn't available.
				// As it stands now, wotaskd will launch, but not really register and app (or get weirdo exceptions)
				logger.error( "Error getting address for Host: {}", name() );
				try {
					Thread.sleep( 2000 );
				}
				catch( InterruptedException e ) {
					logger.error( "Interrupted" );
				}
			}
		}
		// This is just for caching purposes
		errorResponse = new CoderWrapper().encodeRootObjectForKey( new NSDictionary<String, NSArray>( new NSArray( "Failed to contact " + name() + "-" + WOApplication.application().lifebeatDestinationPort() ), "errorResponse" ), "instanceResponse" );
	}

	public void _addInstancePrimitive( MInstance anInstance ) {
		_instanceArray.addObject( anInstance );
		if( !_applicationArray.contains( anInstance._application ) ) {
			_applicationArray.add( anInstance._application );
		}
	}

	public void _removeInstancePrimitive( MInstance anInstance ) {

		_instanceArray.removeObject( anInstance );

		// get the instances's host - check all the other instances that this
		// application has to see if any other ones have that host
		// if not, remove it.
		boolean uniqueApplication = true;

		for( MInstance anInst : _instanceArray ) {
			if( anInstance._application == anInst._application ) {
				uniqueApplication = false;
				break;
			}
		}

		if( uniqueApplication ) {
			_applicationArray.removeObject( anInstance._application );
		}
	}

	private InetAddress _address = null;

	public InetAddress address() {
		return _address;
	}

	public String addressAsString() {
		if( _address != null ) {
			return _address.getHostAddress();
		}
		return "Unknown";
	}

	/** ******* */

	@Override
	public boolean equals( Object other ) {
		return (other instanceof MHost) && (((MHost)other)._address.equals( _address ));
	}

	@Override
	public int hashCode() {
		return _address.hashCode();
	}

	/** ******** Archiving Support ********* */
	public NSDictionary dictionaryForArchive() {
		return values;
	}

	@Override
	public String toString() {
		return "MHost@" + _address;
	}

	public Integer runningInstancesCount_W() {
		int runningInstances = 0;
		int numInstances = _instanceArray.size();

		for( int i = 0; i < numInstances; i++ ) {
			MInstance anInstance = _instanceArray.get( i );
			if( anInstance.isRunning_W() ) {
				runningInstances++;
			}
		}

		return Integer.valueOf( runningInstances );
	}

	public boolean isPortInUse( Integer port ) {
		return instanceWithPort( port ) != null;
	}

	// KH - this is probably slow :)
	public Integer nextAvailablePort( Integer startingPort ) {
		Integer retVal = null;
		do {
			if( isPortInUse( startingPort ) ) {
				startingPort = Integer.valueOf( startingPort.intValue() + 1 );
			}
			else {
				retVal = startingPort;
			}
		}
		while( retVal == null );
		return retVal;
	}

	public MInstance instanceWithPort( Integer port ) {

		for( MInstance anInst : _instanceArray ) {
			if( anInst.port().equals( port ) ) {
				return anInst;
			}
		}

		return null;
	}

	/**
	 * Machine Information and Availability Check (Used by MONITOR)
	 */
	public String runningInstances = "?";

	public String operatingSystem = "?";

	public String processorType = "?";

	public boolean isAvailable = false;

	public void _setHostInfo( Map map ) {
		Object value = null;

		value = map.get( "runningInstances" );
		if( value != null ) {
			runningInstances = value.toString();
		}

		value = map.get( "operatingSystem" );
		if( value != null ) {
			operatingSystem = value.toString();
		}

		value = map.get( "processorType" );
		if( value != null ) {
			processorType = value.toString();
		}
	}

	private String errorResponse = null;

	/**
	 * FIXME: Switch to java http client // Hugi 2024-11-01
	 */
	public WOResponse sendRequestToWotaskd( WORequest aRequest, boolean willChange, boolean isSync ) {

		//		logger.info( "Sending request: {}", aRequest );

		WOResponse aResponse = null;

		try {
			WOHTTPConnection anHTTPConnection = new WOHTTPConnection( name(), WOApplication.application().lifebeatDestinationPort() );
			anHTTPConnection.setReceiveTimeout( _receiveTimeout );

			boolean requestSucceeded = anHTTPConnection.sendRequest( aRequest );

			isAvailable = true;

			if( requestSucceeded ) {
				aResponse = anHTTPConnection.readResponse();
			}
			else {
				isAvailable = false;
			}

			if( aResponse == null ) {
				isAvailable = false;
			}
		}
		catch( Throwable localException ) {
			if( NSLog.debugLoggingAllowedForLevelAndGroups( NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment ) ) {
				NSLog.err.appendln( localException );
			}
			isAvailable = false;
		}

		// For error handling
		if( aResponse == null ) {
			if( willChange ) {
				_siteConfig.hostErrorArray.addObjectIfAbsent( this );
			}
			aResponse = new WOResponse();
			aResponse.setContent( errorResponse );
		}
		else {
			// if we successfully synced, clear the error dictionary
			if( isSync && isAvailable ) {
				if( NSLog.debugLoggingAllowedForLevelAndGroups( NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment ) ) {
					NSLog.debug.appendln( "Cleared sync request for host " + name() );
				}
				_siteConfig.hostErrorArray.removeObject( this );
			}
		}

		return aResponse;
	}
}