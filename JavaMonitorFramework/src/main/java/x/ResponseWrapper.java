package x;

/**
 * A little wrapper class for us to use while we're wrapping up WO style responses
 */

public class ResponseWrapper {

	public byte[] _content;

	public byte[] content() {
		return _content;
	}

	public String contentString() {

		if( _content != null ) {
			return new String( _content );
		}

		return null;
	}
}