package com.webobjects.monitor.util;

import java.util.List;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSThreadsafeMutableArray;
import com.webobjects.monitor._private.CoderWrapper;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;

public class WOTaskdComms {

	/**
	 * Communications Goop
	 */
	public static WOResponse[] sendRequestToWotaskdArray( final String contentString, final List<MHost> wotaskdArray, final boolean willChange ) {

		final MHost aHost = wotaskdArray.get( 0 );

		// FIXME: A little danger sign here... // Hugi 2024-11-02
		if( aHost == null ) {
			return null;
		}

		final MSiteConfig siteConfig = aHost.siteConfig();

		// we had errors reaching a host last time - do it again!
		if( siteConfig.hostErrorArray.count() > 0 ) {
			_syncRequest = null;
			final WORequest syncRequest = syncRequest( siteConfig );
			final _NSThreadsafeMutableArray<MHost> syncHosts = siteConfig.hostErrorArray;

			if( NSLog.debugLoggingAllowedForLevelAndGroups( NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment ) ) {
				NSLog.debug.appendln( "Sending sync requests to: " + syncHosts.array() );
			}

			final Thread[] workers = new Thread[syncHosts.count()];

			for( int i = 0; i < workers.length; i++ ) {
				final int j = i;

				final Runnable work = new Runnable() {
					@Override
					public void run() {
						MHost aHost = syncHosts.objectAtIndex( j );
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

		final WORequest aRequest = new WORequest( MObject._POST, MObject.WOTASKD_DIRECT_ACTION_URL, MObject._HTTP1, siteConfig.passwordDictionary(), new NSData( contentString.getBytes() ), null );
		final List<MHost> finalWotaskdArray = wotaskdArray;
		final boolean wc = willChange;

		final Thread[] workers = new Thread[wotaskdArray.size()];
		final WOResponse[] responses = new WOResponse[workers.length];

		for( int i = 0; i < workers.length; i++ ) {
			final int j = i;

			Runnable work = new Runnable() {
				@Override
				public void run() {
					responses[j] = finalWotaskdArray.get( j ).sendRequestToWotaskd( aRequest, wc, false );
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
	
	private static WORequest _syncRequest = null;

	private static WORequest syncRequest( final MSiteConfig siteConfig ) {
		if( _syncRequest == null ) {
			final NSMutableDictionary<String, NSDictionary> data = new NSMutableDictionary<>( siteConfig.dictionaryForArchive(), "SiteConfig" );
			final NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>> updateWotaskd = new NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>( data, "sync" );
			final NSMutableDictionary<String, NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>> monitorRequest = new NSMutableDictionary<String, NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>>( updateWotaskd, "updateWotaskd" );
			final NSData content = new NSData( (new CoderWrapper()).encodeRootObjectForKey( monitorRequest, "monitorRequest" ).getBytes() );
			_syncRequest = new WORequest( MObject._POST, MObject.WOTASKD_DIRECT_ACTION_URL, MObject._HTTP1, siteConfig.passwordDictionary(), content, null );
		}
		return _syncRequest;
	}
}