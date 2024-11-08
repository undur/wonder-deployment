package com.webobjects.monitor.util;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MInstance;

/**
 * FIXME: Temporary holder class for some statistics functionality we're moving out of the front end // Hugi 2024-10-26 
 */

public class StatsUtilitiesEvenMore {

	public static String statisticsString() {
		return NSPropertyListSerialization.stringFromPropertyList( StatsUtilitiesEvenMore.statistics() );
	}

	private static NSArray statistics() {
		final WOTaskdHandler handler = new WOTaskdHandler( errors -> {} );

		final NSMutableArray stats = new NSMutableArray();

		handler.whileReading( () -> {
			for( final MApplication app : WOTaskdHandler.siteConfig().applicationArray() ) {
				// FIXME: Aren't we redundantly fetching the same info multiple times here? // Hugi 2024-11-08 
				handler.getInstanceStatusForHosts( app.hostArray() );
				stats.addObject( statistics( app ) );
			}
		} );

		return stats;
	}

	private static NSDictionary statistics( final MApplication app ) {

		final NSMutableDictionary<String, Object> result = new NSMutableDictionary<>();
		result.setObjectForKey( app.name(), "applicationName" );

		final NSArray<MInstance> allInstances = app.instanceArray();
		result.setObjectForKey( Integer.valueOf( allInstances.count() ), "configuredInstances" );

		int runningInstances = 0;
		int refusingInstances = 0;
		final NSMutableArray instances = new NSMutableArray();

		for( MInstance instance : allInstances ) {
			if( instance.isRunning_M() ) {
				runningInstances++;
				instances.addObject( instance );
			}

			if( instance.isRefusingNewSessions() ) {
				refusingInstances++;
			}
		}

		result.setObjectForKey( Integer.valueOf( runningInstances ), "runningInstances" );
		result.setObjectForKey( Integer.valueOf( refusingInstances ), "refusingInstances" );

		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@sum.activeSessionsValue" ) ), "sumSessions" );
		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@max.activeSessionsValue" ) ), "maxSessions" );
		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@avg.activeSessionsValue" ) ), "avgSessions" );

		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@sum.transactionsValue" ) ), "sumTransactions" );
		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@max.transactionsValue" ) ), "maxTransactions" );
		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@avg.transactionsValue" ) ), "avgTransactions" );

		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@max.avgTransactionTimeValue" ) ), "maxAvgTransactionTime" );
		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@avg.avgTransactionTimeValue" ) ), "avgAvgTransactionTime" );

		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@max.avgIdleTimeValue" ) ), "maxAvgIdleTime" );
		result.setObjectForKey( nonNull( app.instanceArray().valueForKeyPath( "@avg.avgIdleTimeValue" ) ), "avgAvgIdleTime" );

		return result;
	}

	private static Object nonNull( Object value ) {
		return value != null ? value : "";
	}
}