package x;

import com.webobjects.appserver.WOResponse;

/**
 * A little wrapper class for us to use while we're wrapping up WO style responses
 */

public class ResponseWrapper {

	private WOResponse _woResponse;

	public ResponseWrapper( WOResponse woResponse ) {
		_woResponse = woResponse;
	}

	public WOResponse woResponse() {
		return _woResponse;
	}

	public byte[] content() {

		if( _woResponse == null ) {
			return null;
		}

		if( _woResponse.content() == null ) {
			return null;
		}

		return _woResponse.content().bytes();
	}
}