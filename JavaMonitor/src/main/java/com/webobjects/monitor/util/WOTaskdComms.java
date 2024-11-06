package com.webobjects.monitor.util;

import java.util.ArrayList;
import java.util.List;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.monitor._private.CoderWrapper;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;

public class WOTaskdComms {

	/**
	 * Communications Goop
	 */
	public static WOResponse[] sendRequestToWotaskdArray( final String contentString, final List<MHost> hosts, final boolean willChange ) {

		final MHost aHost = hosts.get( 0 );

		// FIXME: A little danger sign here... // Hugi 2024-11-02
		if( aHost == null ) {
			return null;
		}

		final MSiteConfig siteConfig = aHost.siteConfig();

		// we had errors reaching a host last time - do it again!
		if( !siteConfig.hostErrorArray.isEmpty() ) {
			syncHostsWithErrors( siteConfig );
		}

		final WORequest aRequest = new WORequest( MObject._POST, MObject.WOTASKD_DIRECT_ACTION_URL, MObject._HTTP1, siteConfig.passwordDictionary(), new NSData( contentString.getBytes() ), null );

		final Thread[] workers = new Thread[hosts.size()];
		final WOResponse[] responses = new WOResponse[workers.length];

		for( int i = 0; i < workers.length; i++ ) {
			final int j = i;

			Runnable work = new Runnable() {
				@Override
				public void run() {
					responses[j] = hosts.get( j ).sendRequestToWotaskd( aRequest, willChange, false );
				}
			};

			workers[j] = new Thread( work );
			workers[j].start();
		}

		try {
			for( int i = 0; i < workers.length; i++ ) {
				workers[i].join();
			}
		}
		catch( InterruptedException ie ) {
			// might be bad?
		}

		return responses;
	}

	private static void syncHostsWithErrors( final MSiteConfig siteConfig ) {
		final WORequest syncRequest = syncRequest( siteConfig );
		final List<MHost> syncHosts = new ArrayList<>( siteConfig.hostErrorArray );

		if( NSLog.debugLoggingAllowedForLevelAndGroups( NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment ) ) {
			NSLog.debug.appendln( "Sending sync requests to: " + syncHosts );
		}

		final Thread[] workers = new Thread[syncHosts.size()];

		for( int i = 0; i < workers.length; i++ ) {
			final int j = i;

			final Runnable work = new Runnable() {
				@Override
				public void run() {
					MHost aHost = syncHosts.get( j );
					aHost.sendRequestToWotaskd( syncRequest, true, true );
				}
			};

			workers[j] = new Thread( work );
			workers[j].start();
		}

		try {
			for( int i = 0; i < workers.length; i++ ) {
				workers[i].join();
			}
		}
		catch( InterruptedException ie ) {}
	}

	private static WORequest syncRequest( final MSiteConfig siteConfig ) {
		final NSMutableDictionary<String, NSDictionary> data = new NSMutableDictionary<>( siteConfig.dictionaryForArchive(), "SiteConfig" );
		final NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>> updateWotaskd = new NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>( data, "sync" );
		final NSMutableDictionary<String, NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>> monitorRequest = new NSMutableDictionary<String, NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>>( updateWotaskd, "updateWotaskd" );
		final NSData content = new NSData( (new CoderWrapper()).encodeRootObjectForKey( monitorRequest, "monitorRequest" ).getBytes() );
		return new WORequest( MObject._POST, MObject.WOTASKD_DIRECT_ACTION_URL, MObject._HTTP1, siteConfig.passwordDictionary(), content, null );
	}
}