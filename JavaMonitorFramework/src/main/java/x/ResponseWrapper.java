package x;

import java.net.http.HttpHeaders;
import java.util.Optional;

/**
 * A little wrapper class for us to use while we're wrapping up WO style responses
 */

public class ResponseWrapper {

	public byte[] _content;
	public HttpHeaders _headers;

	public byte[] content() {
		return _content;
	}

	public String contentString() {

		if( _content != null ) {
			return new String( _content );
		}

		return null;
	}

	public String headerForKey( String key ) {

		if( _headers == null ) {
			return null;
		}

		final Optional<String> value = _headers.firstValue( key );

		if( value.isEmpty() ) {
			return null;
		}

		return value.get();
	}
}