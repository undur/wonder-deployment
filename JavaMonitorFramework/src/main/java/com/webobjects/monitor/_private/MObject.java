/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.util.ArrayList;
import java.util.List;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;

public class MObject {

	public static final List<String> LOAD_SCHEDULERS = new ArrayList<>( List.of( "Default", "Round Robin", "Random", "Load Average", "Custom" ) );
	public static final List<String> LOAD_SCHEDULER_VALUES = new ArrayList<>( List.of( "DEFAULT", "ROUNDROBIN", "RANDOM", "LOADAVERAGE", "CUSTOM" ) );
	public static final List<String> HOST_TYPES = new ArrayList<>( List.of( "MacOSX", "Windows", "Unix" ) );
	public static final List<Integer> URL_VERSIONS = new ArrayList<>( List.of( 4, 3 ) );
	public static final List<String> WEEKDAYS = new ArrayList<>( List.of( "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" ) );
	public static final List<String> TIMES_OF_DAY = new ArrayList<>( List.of( "0000", "0100", "0200", "0300", "0400", "0500", "0600", "0700", "0800", "0900", "1000", "1100", "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100", "2200", "2300" ) );
	public static final List<Integer> SCHEDULING_INTERVALS = new ArrayList<>( List.of( 1, 2, 3, 4, 6, 8, 12 ) );
	public static final List<String> SCHEDULING_TYPES = new ArrayList<>( List.of( "HOURLY", "DAILY", "WEEKLY" ) );
	public static final String[] INSTANCE_STATES = new String[] { "UNKNOWN", "STARTING", "ALIVE", "STOPPING", "DEAD", "CRASHING" };

	public static final int UNKNOWN = 0;
	public static final int STARTING = 1;
	public static final int ALIVE = 2;
	public static final int STOPPING = 3;
	public static final int DEAD = 4;
	public static final int CRASHING = 5;

	public static final String WOTASKD_DIRECT_ACTION_URL = "/cgi-bin/WebObjects/wotaskd.woa/wa/monitorRequest";
	public static final String ADMIN_ACTION_STRING_PREFIX = "/cgi-bin/WebObjects/";
	public static final String ADMIN_ACTION_STRING_POSTFIX = ".woa/womp/instanceRequest";

	protected MSiteConfig _siteConfig;

	protected NSMutableDictionary<String, ?> values;
	protected _NSThreadsafeMutableDictionary<String, ?> adaptorValues = new _NSThreadsafeMutableDictionary<>( new NSMutableDictionary<>() );

	public MSiteConfig siteConfig() {
		return _siteConfig;
	}

	public NSMutableDictionary<String, ?> values() {
		return values;
	}

	public void setValues( NSMutableDictionary<String, ?> newValues ) {
		values = newValues;
		_siteConfig.dataHasChanged();
	}

	public void updateValues( NSDictionary<String, ?> aDict ) {
		values = new NSMutableDictionary<>( aDict );
		_siteConfig.dataHasChanged();
	}

	public static Integer validatedInteger( final Integer value ) {

		if( value == null ) {
			return null;
		}

		return Integer.valueOf( Math.abs( value.intValue() ) );
	}

	public static Integer validatedUrlVersion( Integer version ) {

		if( version != null ) {
			int intVal = version.intValue();

			if( intVal != 3 && intVal != 4 ) {
				return Integer.valueOf( 4 );
			}
		}

		return version;
	}

	public static String validatedHostType( String value ) {

		if( value != null ) {
			if( value.equals( "UNIX" ) || value.equals( "WINDOWS" ) || value.equals( "MACOSX" ) ) {
				return value;
			}
		}

		return null;
	}

	public static String validatedOutputPath( String value ) {

		if( value == null || value.length() == 0 ) {
			return "/dev/null";
		}

		return value;
	}

	public static Integer validatedLifebeatInterval( Integer value ) {

		int intVal = 0;

		try {
			intVal = value.intValue();
		}
		catch( Exception e ) {}

		if( intVal < 1 ) {
			return Integer.valueOf( 30 );
		}

		return value;
	}

	public static String validatedSchedulingType( String value ) {

		if( value != null ) {
			if( (value.equals( "HOURLY" )) || (value.equals( "DAILY" )) || (value.equals( "WEEKLY" )) ) {
				return value;
			}
		}

		return null;
	}

	public static Integer validatedSchedulingStartTime( Integer value ) {

		if( value != null ) {
			int intVal = value.intValue();

			if( intVal >= 0 && intVal <= 23 ) {
				return value;
			}
		}

		return null;
	}

	// Our array is from 0-23, but the display is for '12 AM' to '11 PM'
	public static Integer morphedSchedulingStartTime( String value ) {
		int i = TIMES_OF_DAY.indexOf( value );

		if( i != -1 ) {
			return Integer.valueOf( i );
		}

		return null;
	}

	public static String morphedSchedulingStartTime( Integer value ) {

		if( value != null ) {
			return TIMES_OF_DAY.get( value.intValue() );
		}

		return null;
	}

	public static Integer validatedSchedulingStartDay( Integer value ) {

		if( value != null ) {
			int intVal = value.intValue();

			if( intVal >= 0 && intVal <= 6 ) {
				return value;
			}
		}

		return null;
	}

	// Java normally returns 1-7, ObjC returned 0-6, JavaFoundation will return 0-6
	// Our array is from 0-6
	public static Integer morphedSchedulingStartDay( String value ) {
		int i = WEEKDAYS.indexOf( value );

		if( i != -1 ) {
			return Integer.valueOf( i );
		}

		return null;
	}

	public static String morphedSchedulingStartDay( Integer value ) {

		if( value != null ) {
			return WEEKDAYS.get( value.intValue() );
		}

		return null;
	}
}