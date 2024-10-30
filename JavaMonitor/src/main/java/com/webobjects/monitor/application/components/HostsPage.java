package com.webobjects.monitor.application.components;

import java.io.IOException;
/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.StringExtensions;
import com.webobjects.monitor.application.MonitorComponent;
import com.webobjects.monitor.application.components.ConfirmationPage.ConfirmationDelegate;

public class HostsPage extends MonitorComponent {

	public MHost currentHost;
	public String newHostName;
	public String hostTypeSelection = "Unix";
	public NSArray hostTypeList = MObject.hostTypeArray;

	public HostsPage( WOContext aWocontext ) {
		super( aWocontext );
		handler().updateForPage( getClass() );
	}

	public WOComponent addHostClicked() {
		String nullOrError = null;

		if( newHostName != null && (newHostName.length() > 0) && (StringExtensions.isValidXMLString( newHostName )) ) {
			try {
				InetAddress anAddress = InetAddress.getByName( newHostName );

				handler().startWriting();

				try {
					if( newHostName.equalsIgnoreCase( "localhost" ) || newHostName.equals( "127.0.0.1" ) ) {
						// only allow this to happen if we have no other hosts!
						if( !((siteConfig().hostArray() != null) && (siteConfig().hostArray().count() == 0)) ) {
							// we're OK to add localhost.
							nullOrError = "Hosts named localhost or 127.0.0.1 may not be added while other hosts are configured.";
						}
					}
					else {
						// this is for non-localhost hosts
						// only allow this to happen if localhost/127.0.0.1 doesn't already exist!
						if( siteConfig().localhostOrLoopbackHostExists() ) {
							nullOrError = "Additional hosts may not be added while a host named localhost or 127.0.0.1 is configured.";
						}
					}

					if( (nullOrError == null) && (siteConfig().hostWithAddress( anAddress ) == null) ) {
						if( hostMeetsMinimumVersion( anAddress ) ) {

							MHost host = new MHost( siteConfig(), newHostName, hostTypeSelection.toUpperCase() );

							// To avoid overwriting hosts
							NSArray tempHostArray = new NSArray( siteConfig().hostArray() );
							siteConfig().addHost_M( host );

							handler().sendOverwriteToWotaskd( host );

							if( tempHostArray.count() != 0 ) {
								handler().sendAddHostToWotaskds( host, tempHostArray );
							}

						}
						else {
							session().addErrorIfAbsent( "The wotaskd on " + newHostName + " is an older version, please upgrade before adding..." );
						}
					}
					else {
						if( nullOrError != null ) {
							session().addErrorIfAbsent( nullOrError );
						}
						else {
							session().addErrorIfAbsent( "The host " + newHostName + " has already been added" );
						}
					}
				}
				finally {
					handler().endWriting();
				}
			}
			catch( UnknownHostException ex ) {
				session().addErrorIfAbsent( "ERROR: Cannot find IP address for hostname: " + newHostName );
			}
		}
		else {
			session().addErrorIfAbsent( newHostName + " is not a valid hostname" );
		}
		newHostName = null;

		return HostsPage.create( context() );
	}

	public WOComponent removeHostClicked() {

		final MHost host = currentHost;

		return ConfirmationPage.create( context(), new ConfirmationDelegate(
				HOST_PAGE,
				"Are you sure you want to delete the host <I>" + host.name() + "</I>?",
				"Selecting 'Yes' will shutdown any running instances of this host, and remove those instance configurations.",
				() -> {
					handler().startWriting();
					try {
						siteConfig().removeHost_M( host );
						NSMutableArray tempHostArray = new NSMutableArray( siteConfig().hostArray() );
						tempHostArray.addObject( host );

						handler().sendRemoveHostToWotaskds( host, tempHostArray );
					}
					finally {
						handler().endWriting();
					}
					return HostsPage.create( context() );
				},
				() -> HostsPage.create( context() ) ) );
	}

	public WOComponent configureHostClicked() {
		return HostConfigurePage.create( context(), currentHost );
	}

	public WOComponent displayWotaskdInfoClicked() {
		final String hostName = currentHost.name();
		final int port = application().lifebeatDestinationPort();

		final WotaskdInfoPage aPage = pageWithName( WotaskdInfoPage.class );
		aPage.wotaskdText = fetchWotaskdConfigurationString( hostName, port, siteConfig().password() );
		return aPage;
	}

	/**
	 * @param hostName Name of host running wotaskd
	 * @param port wotaskd's port
	 * @param password wotaskd's password (hashed, as returned by the siteconfig)
	 * 
	 * @return An overview of wotaskd's adaptor info, as generated by wotaskd.
	 */
	private static String fetchWotaskdConfigurationString( final String hostName, final int port, final String password ) {

		final HttpRequest.Builder requestBuilder = HttpRequest
				.newBuilder()
				.uri( URI.create( "http://%s:%s/".formatted( hostName, port ) ) )
				.timeout( Duration.ofSeconds( 10 ) );
		
		if( password != null ) {
			requestBuilder.header( "password", password );
		}

		final HttpRequest request = requestBuilder.build();

		try {
			return HttpClient
					.newHttpClient()
					.send( request, BodyHandlers.ofString() )
					.body();
		}
		catch( IOException | InterruptedException e ) {
			e.printStackTrace();
			return "Failed to get response from wotaskd %s:%s".formatted( hostName, port );
		}
	}

	private static boolean hostMeetsMinimumVersion( InetAddress anAddress ) {
		byte[] versionRequest = ("womp://queryVersion").getBytes( StandardCharsets.UTF_8 );
		DatagramPacket outgoingPacket = new DatagramPacket( versionRequest, versionRequest.length, anAddress, WOApplication.application().lifebeatDestinationPort() );

		byte[] mbuffer = new byte[1000];
		DatagramPacket incomingPacket = new DatagramPacket( mbuffer, mbuffer.length );
		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket();
			socket.send( outgoingPacket );
			incomingPacket.setLength( mbuffer.length );
			socket.setSoTimeout( 2000 );
			socket.receive( incomingPacket );
			String reply = new String( incomingPacket.getData() );
			if( reply.startsWith( "womp://replyVersion/" ) ) {
				int lastIndex = reply.lastIndexOf( ":webObjects" );
				lastIndex += 11;
				String version = reply.substring( lastIndex );
				if( version.equals( "4.5" ) ) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		catch( InterruptedIOException iioe ) {
			return true;
		}
		catch( SocketException se ) {
			return true;
		}
		catch( Throwable e ) {
			return false;
		}
		finally {
			if( socket != null ) {
				socket.close();
			}
		}

		return true;
	}

	public static HostsPage create( WOContext context ) {
		return (HostsPage)context.page().pageWithName( HostsPage.class.getName() );
	}
}