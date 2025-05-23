package com.webobjects.monitor.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;

import com.webobjects.monitor._private.MInstance;

/**
 * Home for some experimental functionality (that will probably get a different home later
 */

public class ExperimentalUtilities {

	public static String jstack( final MInstance instance ) {
		final List<String> arguments = instance.commandLineArgumentsAsArray();
		final int indexOfAppPasswordDefinition = arguments.indexOf( "-WOMonitorServicePassword" );

		if( indexOfAppPasswordDefinition == -1 ) {
			throw new IllegalArgumentException( "WOMonitorServicePassword is not set" );
		}

		final String password = arguments.get( indexOfAppPasswordDefinition + 1 );
		final String hostName = "localhost";
		
		// FIXME: This method of obtaining a port for the monitor service absolutely sucks
		final int port = instance.port() + 10000;
		final String url = "/monitor/jstack";

		return fetchStringFromMonitorService( hostName, port, password, url );
	}
	
	/**
	 * @param hostName Name of host running wotaskd
	 * @param port wotaskd's port
	 * @param password wotaskd's password (hashed, as returned by the siteconfig)
	 * 
	 * @return An overview of wotaskd's adaptor info, as generated by wotaskd.
	 */
	private static String fetchStringFromMonitorService( final String hostName, final int port, final String password, final String url ) {

		final HttpClient client = HttpClient
				.newBuilder()
				.build();

		final HttpRequest request = HttpRequest
				.newBuilder()
				.uri( URI.create( "http://%s:%s%s".formatted( hostName, port, url ) ) )
				.timeout( Duration.ofSeconds( 10 ) )
				.header( "monitor-service-password", password )
				.GET()
				.build();

		System.out.println( request.headers() );

		try {
			return client.send( request, BodyHandlers.ofString() ).body();
		}
		catch( IOException | InterruptedException e ) {
			e.printStackTrace();
			return "Failed to get response from wotaskd %s:%s".formatted( hostName, port );
		}
	}
}