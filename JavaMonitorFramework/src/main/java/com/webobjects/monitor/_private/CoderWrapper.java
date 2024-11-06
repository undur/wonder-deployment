package com.webobjects.monitor._private;

import com.webobjects.appserver.xml._JavaMonitorCoder;
import com.webobjects.appserver.xml._JavaMonitorDecoder;
import com.webobjects.foundation.NSData;

public class CoderWrapper {

	private _JavaMonitorCoder _wrappedCoder;
	private _JavaMonitorDecoder _wrappedDecoder;

	public CoderWrapper() {
		_wrappedCoder = new _JavaMonitorCoder();
		_wrappedDecoder = new _JavaMonitorDecoder();
	}

	public Object decodeRootObject( byte[] bytes ) {
		return _wrappedDecoder.decodeRootObject( new NSData( bytes ) );
	}

	@Deprecated
	public Object decodeRootObject( NSData data ) {
		return _wrappedDecoder.decodeRootObject( data );
	}

	public Object decodeRootObject( String string ) {
		return _wrappedDecoder.decodeRootObject( string );
	}

	public String encodeRootObjectForKey( Object object, String key ) {
		return _wrappedCoder.encodeRootObjectForKey( object, key );
	}
}