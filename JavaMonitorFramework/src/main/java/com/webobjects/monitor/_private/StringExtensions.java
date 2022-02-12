/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import com.webobjects.foundation.NSKeyValueCodingAdditions;

public class StringExtensions {

	public static boolean boolValue( final String s ) {

		if( s == null ) {
			return false;
		}

		return (s.equalsIgnoreCase( "_YES" ) ||
				s.equalsIgnoreCase( "Y" ) ||
				s.equalsIgnoreCase( "YES" ) ||
				s.equalsIgnoreCase( "true" ) ||
				s.equalsIgnoreCase( "1" ));
	}

	public static String booleanAsYNString( final Boolean b ) {
		return (b != null && b.booleanValue()) ? "YES" : "NO";
	}

	public static boolean boolValue( final Object o ) {

		if( o == null ) {
			return false;
		}
		else if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		}
		else if( o instanceof Number ) {
			return ((Number)o).intValue() != 0;
		}
		else if( o instanceof String ) {
			return boolValue( (String)o );
		}

		return false;
	}

	public static boolean isValidXMLString( final String s ) {

		if( s == null || s.length() == 0 ) {
			return false;
		}

		for( int i = 0; i < s.length(); i++ ) {
			char aChar = s.charAt( i );
			if( (!Character.isLetterOrDigit( aChar )) && (aChar != '-') && (aChar != '.') ) {
				return false;
			}
		}

		return true;
	}

	// Re-added from ERXStringUtilities since JavaMonitor turned out to be still using it.
	public static final String lastPropertyKeyInKeyPath( String keyPath ) {
		String part = null;
		if( keyPath != null ) {
			int index = keyPath.lastIndexOf( NSKeyValueCodingAdditions.KeyPathSeparator );
			if( index != -1 ) {
				part = keyPath.substring( index + 1 );
			}
			else {
				part = keyPath;
			}
		}
		return part;
	}

	public static class GzipStringShit {

		public static void stringToGZippedFile( String s, File f ) throws IOException {
			if( s == null ) {
				throw new NullPointerException( "string argument cannot be null" );
			}
			if( f == null ) {
				throw new NullPointerException( "file argument cannot be null" );
			}

			final byte[] bytes = s.getBytes( StandardCharsets.UTF_8 );
			final ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
			writeInputStreamToGZippedFile( bais, f );
		}

		private static void writeInputStreamToGZippedFile( InputStream stream, File file ) throws IOException {
			if( file == null ) {
				throw new IllegalArgumentException( "Attempting to write to a null file!" );
			}
			try( GZIPOutputStream out = new GZIPOutputStream( new FileOutputStream( file ) )) {
				writeInputStreamToOutputStream( stream, false, out, true );
			}
		}

		/**
		 * Copies the contents of the input stream to the given output stream.
		 *
		 * @param in the input stream to copy from
		 * @param closeInputStream if true, the input stream will be closed
		 * @param out the output stream to copy to
		 * @param closeOutputStream if true, the output stream will be closed
		 * @throws IOException if there is any failure
		 */
		private static void writeInputStreamToOutputStream( InputStream in, boolean closeInputStream, OutputStream out, boolean closeOutputStream ) throws IOException {
			try {
				BufferedInputStream bis = new BufferedInputStream( in );
				try {
					byte buf[] = new byte[1024 * 50]; //64 KBytes buffer
					int read = -1;
					while( (read = bis.read( buf )) != -1 ) {
						out.write( buf, 0, read );
					}
				}
				finally {
					if( closeInputStream ) {
						bis.close();
					}
				}
				out.flush();
			}
			finally {
				if( closeOutputStream ) {
					out.close();
				}
			}
		}
	}
}