package com.webobjects.monitor.util;

import com.webobjects.foundation.NSKeyValueCodingAdditions;

public class Util {

	// FIXME: Re-added from ERXStringUtilities since JavaMonitor turned out to be still using it.
	@Deprecated
	public static final String lastPropertyKeyInKeyPath(String keyPath) {
		String part = null;
		if (keyPath != null) {
			int index = keyPath.lastIndexOf(NSKeyValueCodingAdditions.KeyPathSeparator);
			if (index != -1)
				part = keyPath.substring(index + 1);
			else
				part = keyPath;
		}
		return part;
	}
}