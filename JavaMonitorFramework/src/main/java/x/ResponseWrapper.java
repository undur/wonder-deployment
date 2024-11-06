package x;

import com.webobjects.appserver.WOResponse;

/**
 * A little wrapper class for us to use while we're wrapping up WO style responses
 */

public class ResponseWrapper {

	public WOResponse _woResponse;
	public byte[] _content;

	public WOResponse woResponse() {
		return _woResponse;
	}

	public byte[] content() {

		if( _content != null ) {
			return _content;
		}

		if( _woResponse == null ) {
			return null;
		}

		if( _woResponse.content() == null ) {
			return null;
		}

		return _woResponse.content().bytes();
	}

	public String contentString() {

		if( _content != null ) {
			return new String( _content );
		}

		if( _woResponse == null ) {
			return null;
		}

		if( _woResponse.contentString() == null ) {
			return null;
		}

		return _woResponse.contentString();
	}
}