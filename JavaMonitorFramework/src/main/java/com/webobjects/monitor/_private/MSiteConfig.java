/*
(c) Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver._private.WOHostUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;

public class MSiteConfig extends MObject {

	private static final Logger logger = LoggerFactory.getLogger( MSiteConfig.class );

	/*
	// Site
	String password;
	String woAdaptor;
	String SMTPhost;
	String emailReturnAddr;
	Boolean viewRefreshEnabled;
	Integer viewRefreshRate;
	Integer sequence;
	// Adaptor
	Integer retries;
	String scheduler;	// "RANDOM" | "ROUNDROBIN" | "LOADAVERAGE" | <Custom Scheduler Name>
	Integer dormant;
	String redir;
	Integer sendTimeout;
	Integer recvTimeout;
	Integer cnctTimeout;
	Integer sendBufSize;
	Integer recvBufSize;
	Integer poolsize;
	Integer urlVersion;	// 3 | 4
	*/

	/********** 'values' accessors **********/
	public String password() {
		return (String)values.valueForKey( "password" );
	}

	// Special treatment - the password is stored encrypted!
	public void setPassword( String value ) {
		_setPassword( value );
		_siteConfig.dataHasChanged();
	}

	public String woAdaptor() {
		return (String)values.valueForKey( "woAdaptor" );
	}

	public void setWoAdaptor( String value ) {
		values.takeValueForKey( value, "woAdaptor" );
		_siteConfig.dataHasChanged();
	}

	public String SMTPhost() {
		return (String)values.valueForKey( "SMTPhost" );
	}

	public void setSMTPhost( String value ) {
		values.takeValueForKey( value, "SMTPhost" );
		_siteConfig.dataHasChanged();
	}

	public String emailReturnAddr() {
		return (String)values.valueForKey( "emailReturnAddr" );
	}

	public void setEmailReturnAddr( String value ) {
		values.takeValueForKey( value, "emailReturnAddr" );
		_siteConfig.dataHasChanged();
	}

	public Boolean viewRefreshEnabled() {
		return (Boolean)values.valueForKey( "viewRefreshEnabled" );
	}

	public void setViewRefreshEnabled( Boolean value ) {
		values.takeValueForKey( value, "viewRefreshEnabled" );
		_siteConfig.dataHasChanged();
	}

	public Integer viewRefreshRate() {
		return (Integer)values.valueForKey( "viewRefreshRate" );
	}

	public void setViewRefreshRate( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "viewRefreshRate" );
		_siteConfig.dataHasChanged();
	}

	public Integer sequence() {
		return (Integer)values.valueForKey( "sequence" );
	}

	public void setSequence( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "sequence" );
		_siteConfig.dataHasChanged();
	}

	public Integer retries() {
		return (Integer)values.valueForKey( "retries" );
	}

	public void setRetries( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "retries" );
		_siteConfig.dataHasChanged();
	}

	public String scheduler() {
		return (String)values.valueForKey( "scheduler" );
	}

	public void setScheduler( String value ) {
		values.takeValueForKey( value, "scheduler" );
		_siteConfig.dataHasChanged();
	}

	public Integer dormant() {
		return (Integer)values.valueForKey( "dormant" );
	}

	public void setDormant( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "dormant" );
		_siteConfig.dataHasChanged();
	}

	public String redir() {
		return (String)values.valueForKey( "redir" );
	}

	public void setRedir( String value ) {
		values.takeValueForKey( value, "redir" );
		_siteConfig.dataHasChanged();
	}

	public Integer sendTimeout() {
		return (Integer)values.valueForKey( "sendTimeout" );
	}

	public void setSendTimeout( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "sendTimeout" );
		_siteConfig.dataHasChanged();
	}

	public Integer recvTimeout() {
		return (Integer)values.valueForKey( "recvTimeout" );
	}

	public void setRecvTimeout( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "recvTimeout" );
		_siteConfig.dataHasChanged();
	}

	public Integer cnctTimeout() {
		return (Integer)values.valueForKey( "cnctTimeout" );
	}

	public void setCnctTimeout( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "cnctTimeout" );
		_siteConfig.dataHasChanged();
	}

	public Integer sendBufSize() {
		return (Integer)values.valueForKey( "sendBufSize" );
	}

	public void setSendBufSize( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "sendBufSize" );
		_siteConfig.dataHasChanged();
	}

	public Integer recvBufSize() {
		return (Integer)values.valueForKey( "recvBufSize" );
	}

	public void setRecvBufSize( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "recvBufSize" );
		_siteConfig.dataHasChanged();
	}

	public Integer poolsize() {
		return (Integer)values.valueForKey( "poolsize" );
	}

	public void setPoolsize( Integer value ) {
		values.takeValueForKey( MObject.validatedInteger( value ), "poolsize" );
		_siteConfig.dataHasChanged();
	}

	public Integer urlVersion() {
		return (Integer)values.valueForKey( "urlVersion" );
	}

	public void setUrlVersion( Integer value ) {
		values.takeValueForKey( MObject.validatedUrlVersion( value ), "urlVersion" );
		_siteConfig.dataHasChanged();
	}

	/********** Errors  **********/
	public _NSThreadsafeMutableDictionary<String, String> globalErrorDictionary = new _NSThreadsafeMutableDictionary<>( new NSMutableDictionary<>() );

	public Set<MHost> hostErrorArray = Collections.synchronizedSet( new HashSet<>() );

	/********** Object Graph  **********/
	private final NSMutableArray<MHost> _hostArray = new NSMutableArray<>();
	private final NSMutableArray<MInstance> _instanceArray = new NSMutableArray<>();
	private final NSMutableArray<MApplication> _applicationArray = new NSMutableArray<>();

	private MHost _localHost;

	public NSMutableArray<MHost> hostArray() {
		return _hostArray;
	}

	public NSMutableArray<MInstance> instanceArray() {
		return _instanceArray;
	}

	public NSMutableArray<MApplication> applicationArray() {
		return _applicationArray;
	}

	public MHost localHost() {
		return _localHost;
	}

	/********** Change Notifications **********/
	private boolean _hasChanges = true;

	public boolean hasChanges() {
		return _hasChanges;
	}

	public void resetChanges() {
		_hasChanges = false;
	}

	public void dataHasChanged() {
		_hasChanges = true;
	}

	/********** Adding and Deleting **********/
	private InetAddress localHostAddress;
	private String localHostName;

	public void _addHost( MHost newHost ) {
		// If WOHost was passed, it'll resolve against that, otherwise, it'll resolve any local address
		if( WOHostUtilities.isLocalInetAddress( newHost.address(), true ) ) {
			_localHost = newHost;
		}
		_hostArray.addObject( newHost );
		dataHasChanged();
	}

	public void addHost_M( MHost newHost ) {
		backup( "addHost-" + newHost.name() );
		_addHost( newHost );
	}

	public void addHost_W( MHost newHost ) {
		_addHost( newHost );
	}

	public void _removeHost( MHost aHost ) {
		_hostArray.removeObject( aHost );
		if( aHost == _localHost ) {
			_localHost = null;
		}
		dataHasChanged();
	}

	public void removeHost_M( MHost aHost ) {

		backup( "removeHost-" + aHost.name() );

		final NSArray<MInstance> tempArray = new NSArray<>( aHost.instanceArray() );

		for( int i = 0; i < tempArray.count(); i++ ) {
			removeInstance_M( tempArray.objectAtIndex( i ), false );
		}

		_removeHost( aHost );
	}

	public void removeHost_W( MHost aHost ) {
		final NSArray<MInstance> tempArray = new NSArray<>( aHost.instanceArray() );

		for( int i = 0; i < tempArray.count(); i++ ) {
			removeInstance_W( tempArray.objectAtIndex( i ) );
		}

		_removeHost( aHost );
	}

	public void _addApplication( MApplication newApplication ) {
		_applicationArray.addObject( newApplication );
		dataHasChanged();
	}

	public void addApplication_M( MApplication newApplication ) {
		backup( "addApplication-" + newApplication.name() );
		_addApplication( newApplication );
	}

	public void addApplication_W( MApplication newApplication ) {
		_addApplication( newApplication );
	}

	public void _removeApplication( MApplication anApplication ) {
		_applicationArray.removeObject( anApplication );
		dataHasChanged();
	}

	public void removeApplication_M( MApplication anApplication ) {

		backup( "removeApplication-" + anApplication.name() );

		final NSArray<MInstance> tempArray = new NSArray<>( anApplication.instanceArray() );

		for( int i = 0; i < tempArray.count(); i++ ) {
			removeInstance_M( tempArray.objectAtIndex( i ), false );
		}

		_removeApplication( anApplication );
	}

	public void removeApplication_W( MApplication anApplication ) {

		final NSArray<MInstance> tempArray = new NSArray<>( anApplication.instanceArray() );

		for( int i = 0; i < tempArray.count(); i++ ) {
			removeInstance_W( tempArray.objectAtIndex( i ) );
		}

		_removeApplication( anApplication );
	}

	public void _addInstance( MInstance newInstance ) {
		_instanceArray.addObject( newInstance );
		newInstance._host._addInstancePrimitive( newInstance );
		newInstance._application._addInstancePrimitive( newInstance );
		dataHasChanged();
	}

	public NSMutableArray<MInstance> addInstances_M( MHost selectedHost, MApplication anApplication, int numberToAdd ) {

		backup( "addInstances-" + anApplication.name() + "-" + selectedHost.name() + "-" + numberToAdd );

		final NSMutableArray<MInstance> newInstanceArray = new NSMutableArray<>( numberToAdd );

		for( int i = 0; i < numberToAdd; i++ ) {
			final Integer aUniqueID = anApplication.nextID();
			final MInstance newInstance = new MInstance( selectedHost, anApplication, aUniqueID, this );
			addInstance_M( newInstance );
			newInstanceArray.addObject( newInstance );
		}

		return newInstanceArray;
	}

	public void addInstance_M( MInstance newInstance ) {
		_addInstance( newInstance );
	}

	public void addInstance_W( MInstance newInstance ) {
		_addInstance( newInstance );
	}

	public void _removeInstance( MInstance anInstance ) {
		//cancel all tasks
		anInstance.cancelForceQuitTask();
		anInstance._host._removeInstancePrimitive( anInstance );
		anInstance._application._removeInstancePrimitive( anInstance );
		_instanceArray.removeObject( anInstance );
		dataHasChanged();
	}

	public void removeInstance_M( MInstance anInstance ) {
		removeInstance_M( anInstance, true );
	}

	public void removeInstances_M( MApplication application, List<MInstance> instances ) {

		backup( "removeInstances-" + application + "-" + instances.size() );

		for( final MInstance instance : instances ) {
			removeInstance_M( instance, false );
		}
	}

	private void removeInstance_M( MInstance anInstance, boolean doBackup ) {

		if( doBackup ) {
			backup( "removeInstance-" + anInstance.displayName() );
		}

		_removeInstance( anInstance );
	}

	public void removeInstance_W( MInstance anInstance ) {

		if( (anInstance._host == _localHost) && anInstance.isRunning_W() ) {
			final ProtoLocalMonitor plMonitor = (ProtoLocalMonitor)WOApplication.application().valueForKey( "localMonitor" );

			try {
				plMonitor.stopInstance( anInstance );
			}
			catch( final MonitorException me ) {
				logger.error( "Can't remove", me );
			}
		}

		_removeInstance( anInstance );
	}

	/**********/

	/********** Password Methods **********/
	private static long myrand() {
		long nextLong = ThreadLocalRandom.current().nextLong();
		while( nextLong == Long.MIN_VALUE ) {
			nextLong = ThreadLocalRandom.current().nextLong();
		}
		return Math.abs( nextLong );
	}

	private String encryptStringWithKey( String to_be_encrypted, String aKey ) {
		String encrypted_value = "";
		final char xdigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		MessageDigest messageDigest;

		try {
			messageDigest = MessageDigest.getInstance( "MD5" );
		}
		catch( final NoSuchAlgorithmException exc ) {
			globalErrorDictionary.takeValueForKey( ("Security package does not contain appropriate algorithm"), ("Security package does not contain appropriate algorithm") );
			logger.error( "Security package does not contain appropriate algorithm" );
			return encrypted_value;
		}
		if( to_be_encrypted != null ) {
			byte digest[];
			byte fudge_constant[];
			try {
				fudge_constant = ("X#@!").getBytes( "UTF8" );
			}
			catch( final UnsupportedEncodingException uee ) {
				fudge_constant = ("X#@!").getBytes();
			}
			byte fudgetoo_part[] = {
					(byte)xdigit[(int)(MSiteConfig.myrand() % 16)],
					(byte)xdigit[(int)(MSiteConfig.myrand() % 16)],
					(byte)xdigit[(int)(MSiteConfig.myrand() % 16)],
					(byte)xdigit[(int)(MSiteConfig.myrand() % 16)]
			};
			int i = 0;

			if( aKey != null ) {
				try {
					fudgetoo_part = aKey.getBytes( "UTF8" );
				}
				catch( final UnsupportedEncodingException uee ) {
					fudgetoo_part = aKey.getBytes();
				}
			}
			messageDigest.update( fudge_constant );
			try {
				messageDigest.update( to_be_encrypted.getBytes( "UTF8" ) );
			}
			catch( final UnsupportedEncodingException uee ) {
				messageDigest.update( to_be_encrypted.getBytes() );
			}
			messageDigest.update( fudgetoo_part );
			digest = messageDigest.digest();
			encrypted_value = new String( fudgetoo_part );
			for( i = 0; i < digest.length; i++ ) {
				int mashed;
				final char temp[] = new char[2];
				if( digest[i] < 0 ) {
					mashed = 127 + (-1 * digest[i]);
				}
				else {
					mashed = digest[i];
				}
				temp[0] = xdigit[mashed / 16];
				temp[1] = xdigit[mashed % 16];
				encrypted_value = encrypted_value + (new String( temp ));
			}
		}
		return encrypted_value;
	}

	public boolean isPasswordRequired() {
		return password() != null;
	}

	// setPassword(value) is in the 'values' accessors
	public void _setPassword( String value ) {
		if( value != null ) {
			values.takeValueForKey( encryptStringWithKey( value, null ), "password" );
		}
		else {
			resetPassword();
		}
	}

	public void resetPassword() {
		values.takeValueForKey( null, "password" );
	}

	private String _oldPassword = null;
	private boolean _oldPasswordSet = false;

	public void _setOldPassword() {
		_oldPassword = password();
		_oldPasswordSet = true;
	}

	public void _resetOldPassword() {
		_oldPassword = null;
		_oldPasswordSet = false;
	}

	public boolean compareStringWithPassword( String aString ) {
		final String _encryptedPassword = password();

		if( aString == null && _encryptedPassword == null ) {
			// if both are null, match
			return true;
		}
		else if( aString == null || _encryptedPassword == null ) {
			// if one is null, and the other isn't, no match
			return false;
		}
		else { // do all the calculations
				// extract random portion of the encrypted password
			final String fudgetoo_part = _encryptedPassword.substring( 0, 4 );
			// encrypt the new string using the random bit from the old string
			final String encrypted_string = encryptStringWithKey( aString, fudgetoo_part );
			// compare keys and return
			return encrypted_string.equals( _encryptedPassword );
		}
	}

	// The argument is always the tested. _encryptedPassword is the "correct" one.
	public boolean comparePasswordWithPassword( String aString ) {
		final String _encryptedPassword = password();

		if( (_encryptedPassword == null) || (_encryptedPassword.length() == 0) ) {
			// if _encryptedPassword is null or blank, match
			return true;
		}
		else if( (aString == null) || (aString.length() == 0) ) {
			// if aString is null or blank, no match (since by this time, _encryptedPassword is non-null and not blank)
			return false;
		}
		else if( aString.equals( _encryptedPassword ) ) {
			return true;
		}
		return false;
	}

	@Deprecated
	private Map<String, List<String>> _passwordHeaderMap;

	// FIXME: This was for construction of HTTP headers. Leaving in place for now as a reminder to fix up the "oldpassword" stuff // Hugi 2024-11-06
	@Deprecated
	public Map<String, List<String>> passwordHeaderMap() {

		if( _passwordHeaderMap == null ) {
			_passwordHeaderMap = new NSMutableDictionary<>();
			_passwordHeaderMap.put( "password", List.of( "" ) );
		}

		final String password = password();

		if( _oldPasswordSet ) {
			if( _oldPassword != null ) {
				_passwordHeaderMap.put( "password", List.of( _oldPassword ) );
				return _passwordHeaderMap;
			}
			return NSDictionary.emptyDictionary();
		}

		if( password != null ) {
			_passwordHeaderMap.put( "password", List.of( password ) );
			return _passwordHeaderMap;
		}

		return NSDictionary.emptyDictionary();
	}

	public MSiteConfig( NSDictionary xmlDict ) {
		localHostAddress = WOApplication.application().hostAddress();
		localHostName = WOApplication.application().host();

		_siteConfig = this;
		if( xmlDict == null ) {
			values = new NSMutableDictionary();
			setViewRefreshEnabled( Boolean.TRUE );
			setViewRefreshRate( Integer.valueOf( 60 ) );
		}
		else {
			final NSDictionary siteDict = (NSDictionary)xmlDict.valueForKey( "site" );
			if( siteDict == null ) {
				// rdar://3935864 - Seed: "Null Pointer Exception" for WO Application Instances
				// It seems this should not be necessary, but there is no other place for default values to get fed in. -rrk
				//
				values = new NSMutableDictionary( new NSArray( new Object[] { Boolean.TRUE, Integer.valueOf( 60 ) } ), new NSArray( new Object[] { "viewRefreshEnabled", "viewRefreshRate" } ) );
			}
			else {
				values = new NSMutableDictionary( siteDict );
			}

			final NSArray hostArray = (NSArray)xmlDict.valueForKey( "hostArray" );
			_initHostsWithArray( hostArray );

			final NSArray applicationArray = (NSArray)xmlDict.valueForKey( "applicationArray" );
			_initApplicationsWithArray( applicationArray );

			final NSArray instanceArray = (NSArray)xmlDict.valueForKey( "instanceArray" );
			_initInstancesWithArray( instanceArray );
		}

		// setting the multiplier for assuming an application is dead
		_appIsDeadMultiplier = 2 * 1000;
		final String WOAssumeAppIsDeadMultiplier = System.getProperties().getProperty( "WOAssumeApplicationIsDeadMultiplier" );
		if( WOAssumeAppIsDeadMultiplier != null ) {
			try {
				final Integer tempInt = Integer.valueOf( WOAssumeAppIsDeadMultiplier );
				_appIsDeadMultiplier = tempInt.intValue() * 1000;
			}
			catch( final NumberFormatException e ) {
				// go with the default
				NSLog._conditionallyLogPrivateException( e );
			}
		}
		_lastConfig = generateSiteConfigXML();
	}

	private void _initHostsWithArray( final List<NSDictionary> list ) {

		if( list == null ) {
			return;
		}

		for( final NSDictionary map : list ) {
			final MHost aHost = new MHost( map, this );
			_addHost( aHost );
		}
	}

	private void _initApplicationsWithArray( final List<NSDictionary> list ) {

		if( list == null ) {
			return;
		}

		for( final NSDictionary map : list ) {
			final MApplication anApplication = new MApplication( map, this );
			_addApplication( anApplication );
		}
	}

	private void _initInstancesWithArray( final List<NSDictionary> list ) {

		if( list == null ) {
			return;
		}

		for( final NSDictionary map : list ) {
			final MInstance anInstance = new MInstance( map, this );
			_addInstance( anInstance );
		}
	}

	/**********/

	/********** File System Stuff **********/
	private static String _configDirectoryPath = null;
	private static String _pathForSiteConfig = null;
	private static String _pathForAdaptorConfig = null;
	private static File _fileForSiteConfig = null;
	private static File _fileForAdaptorConfig = null;

	public static String configDirectoryPath() {

		if( _configDirectoryPath == null ) {
			_configDirectoryPath = System.getProperty( "WODeploymentConfigurationDirectory" );

			if( _configDirectoryPath == null || _configDirectoryPath.isEmpty() ) {
				logger.error( "WODeploymentConfigurationDirectory not set" );
				System.exit( 1 );
			}

			logger.info( "WODeploymentConfigurationDirectory is: " + _configDirectoryPath );

			if( !_configDirectoryPath.endsWith( File.separator ) ) {
				_configDirectoryPath = _configDirectoryPath + File.separator;
			}

			final File configDir = new File( _configDirectoryPath );

			if( !configDir.exists() ) {
				if( !configDir.mkdirs() ) {
					logger.error( "Configuration Directory {} does not exist, and cannot be created.", _configDirectoryPath );
					System.exit( 1 );
				}
			}
			else {
				if( !configDir.isDirectory() ) {
					logger.error( "Configuration Directory {} is not actually a directory.", _configDirectoryPath );
					System.exit( 1 );
				}
				if( !configDir.canRead() ) {
					logger.error( "Don't have permission to read from Configuration Directory {} as this user, please change the permissions or restart {} as another user.", _configDirectoryPath, WOApplication.application().name() );
					System.exit( 1 );
				}
				if( (WOApplication.application().name().equals( "wotaskd" )) && (!configDir.canWrite()) ) {
					logger.error( "Don't have permission to write to Configuration Directory {} as this user; please change the permissions.", _configDirectoryPath );
					System.exit( 1 );
				}
			}
		}

		return _configDirectoryPath;
	}

	private static String pathForSiteConfig() {
		if( _pathForSiteConfig == null ) {
			_pathForSiteConfig = MSiteConfig.configDirectoryPath().concat( "SiteConfig.xml" );
		}
		return _pathForSiteConfig;
	}

	private static String pathForAdaptorConfig() {
		if( _pathForAdaptorConfig == null ) {
			_pathForAdaptorConfig = MSiteConfig.configDirectoryPath().concat( "WOConfig.xml" );
		}
		return _pathForAdaptorConfig;
	}

	private static File fileForSiteConfig() {
		if( _fileForSiteConfig == null ) {
			_fileForSiteConfig = new File( pathForSiteConfig() );
		}
		return _fileForSiteConfig;
	}

	private static File fileForAdaptorConfig() {
		if( _fileForAdaptorConfig == null ) {
			_fileForAdaptorConfig = new File( pathForAdaptorConfig() );
		}
		return _fileForAdaptorConfig;
	}

	public static MSiteConfig unarchiveSiteConfig( boolean isWotaskd ) {
		MSiteConfig aConfig = null;

		// The file may not exist, but we can create it.
		// It is awkward to do the file creation here, in this way, but this stuff needs to be factored properly.
		// This may throw an exception when it tries to create the file. Go to the utility method to see the exception being dropped.
		if( !fileForSiteConfig().exists() ) {
			final String emptySiteConfig = new CoderWrapper().encodeRootObjectForKey( NSDictionary.EmptyDictionary, "SiteConfig" );
			_NSStringUtilities.writeToFile( fileForSiteConfig(), emptySiteConfig );
		}

		// Now, the file should exist, or we have an error.
		if( fileForSiteConfig().exists() ) {
			if( fileForSiteConfig().canRead() ) {
				try {
					final NSDictionary siteDict = (NSDictionary)new CoderWrapper().decodeRootObject( pathForSiteConfig() );

					aConfig = new MSiteConfig( siteDict );

					if( NSLog.debugLoggingAllowedForLevelAndGroups( NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment ) ) {
						NSLog.debug.appendln( "the SiteConfig is \n" + aConfig.generateSiteConfigXML() );
					}
				}
				catch( final Throwable ex ) {
					if( isWotaskd ) {
						logger.error( "Failed to parse {}. Backing up original SiteConfig and continuing as if empty.", pathForSiteConfig() );
						backupSiteConfig();
					}
					else {
						logger.error( "Failed to parse {}. Continuing as if empty.", pathForSiteConfig() );
					}
				}
			}
			else {
				logger.error( "Cannot read from SiteConfig file {}. Possible Permissions Problem.", pathForSiteConfig() );
				System.exit( 1 );
			}
		}
		else {
			logger.error( "SiteConfig file {} doesn't exist. Continuing as if empty.", pathForSiteConfig() );
		}

		if( aConfig == null ) {
			aConfig = new MSiteConfig( null );
		}

		return aConfig;
	}

	private static void backupSiteConfig() {
		try {
			final File sc = fileForSiteConfig();
			if( sc.exists() ) {
				final NSTimestampFormatter formatter = new NSTimestampFormatter( "%Y%m%d%H%M%S%F" ); // FIXME: Replace with java.time // Hugi 2024-11-10
				final File renamedFile = new File( pathForSiteConfig() + "." + formatter.format( new NSTimestamp() ) );
				sc.renameTo( renamedFile );
			}
		}
		catch( final NSForwardException ne ) {
			logger.error( "Cannot backup file {}. Possible Permissions Problem.", pathForSiteConfig() );
		}
	}

	public void archiveSiteConfig() {
		saveSiteConfig( fileForSiteConfig(), generateSiteConfigXML(), false );
	}

	private void saveSiteConfig( File siteConfigFile, String serialisedSiteConfig, boolean compress ) {
		try {
			if( siteConfigFile.exists() && !siteConfigFile.canWrite() ) {
				logger.error( "Don't have permission to write to file {} as this user, please change the permissions.", siteConfigFile.getAbsolutePath() );
				final String pre = WOApplication.application().name() + " - " + localHostName;
				globalErrorDictionary.takeValueForKey( pre + " Don't have permission to write to file " + siteConfigFile.getAbsolutePath() + " as this user, please change the permissions.", "archiveSiteConfig" );
				return;
			}

			if( compress ) {
				siteConfigFile = new File( siteConfigFile.getParentFile(), siteConfigFile.getName() + ".gz" );
				stringToGZippedFile( serialisedSiteConfig, siteConfigFile );
			}
			else {
				_NSStringUtilities.writeToFile( siteConfigFile, serialisedSiteConfig );
			}

			globalErrorDictionary.takeValueForKey( null, "archiveSiteConfig" );
		}
		catch( final IOException e ) {
			final String message = "Cannot write to file " + siteConfigFile.getAbsolutePath() + ". IOException: " + e.getLocalizedMessage();
			logger.error( message );
			final String pre = WOApplication.application().name() + " - " + localHostName;
			globalErrorDictionary.takeValueForKey( pre + message, "archiveSiteConfig" );
		}
		catch( final NSForwardException ne ) {
			logger.error( "Cannot write to file {}. Possible Permissions Problem.", siteConfigFile.getAbsolutePath() );
			final String pre = WOApplication.application().name() + " - " + localHostName;
			globalErrorDictionary.takeValueForKey( pre + " Cannot write to file " + siteConfigFile.getAbsolutePath() + ". Possible Permissions Problem.", "archiveSiteConfig" );
		}
	}

	private static void stringToGZippedFile( final String string, final File file ) throws IOException {
		Objects.requireNonNull( string );
		Objects.requireNonNull( file );

		final byte[] bytes = string.getBytes( StandardCharsets.UTF_8 );
		final ByteArrayInputStream stream = new ByteArrayInputStream( bytes );

		try( final GZIPOutputStream out = new GZIPOutputStream( new FileOutputStream( file ) )) {
			stream.transferTo( out );
		}
	}

	public void archiveAdaptorConfig() {
		try {
			final File ac = fileForAdaptorConfig();
			if( ac.exists() && !ac.canWrite() ) {
				logger.error( "Don't have permission to write to file {} as this user, please change the permissions.", fileForAdaptorConfig() );
				final String pre = WOApplication.application().name() + " - " + localHostName;
				globalErrorDictionary.takeValueForKey( pre + " Don't have permission to write to file " + fileForAdaptorConfig() + "as this user, please change the permissions.", "archiveSiteConfig" );
				return;
			}
			_NSStringUtilities.writeToFile( fileForAdaptorConfig(), generateAdaptorConfigXML( false, false ) );
			globalErrorDictionary.takeValueForKey( null, "archiveAdaptorConfig" );
		}
		catch( final NSForwardException ne ) {
			logger.error( "Cannot write to file {}. Possible Permissions Problem.", pathForAdaptorConfig() );
			final String pre = WOApplication.application().name() + " - " + localHostName;
			globalErrorDictionary.takeValueForKey( pre + " Cannot write to file " + pathForAdaptorConfig() + ". Possible Permissions Problem.", "archiveAdaptorConfig" );
		}
	}

	public String generateAdaptorConfigXML( boolean onlyIncludeRunningInstances, boolean shouldIncludeUnregisteredInstances ) {
		final StringBuilder sb = new StringBuilder( "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n<adaptor>\n" );

		for( final MApplication anApp : applicationArray() ) {

			if( !(onlyIncludeRunningInstances && !anApp.isRunning_W()) ) {

				anApp.extractAdaptorValuesFromSiteConfig();

				final Integer retries = (Integer)anApp.adaptorValues.valueForKey( "retries" );
				final String scheduler = (String)anApp.adaptorValues.valueForKey( "scheduler" );
				final Integer dormant = (Integer)anApp.adaptorValues.valueForKey( "dormant" );
				final String redir = (String)anApp.adaptorValues.valueForKey( "redir" );
				final Integer poolsize = (Integer)anApp.adaptorValues.valueForKey( "poolsize" );
				final Integer urlVersion = (Integer)anApp.adaptorValues.valueForKey( "urlVersion" );

				sb.append( "  <application name=\"" );
				sb.append( anApp.name() );

				if( retries != null ) {
					sb.append( "\" retries=\"" );
					sb.append( retries.toString() );
				}
				if( scheduler != null ) {
					sb.append( "\" scheduler=\"" );
					sb.append( scheduler );
				}
				if( dormant != null ) {
					sb.append( "\" dormant=\"" );
					sb.append( dormant );
				}
				if( redir != null ) {
					sb.append( "\" redir=\"" );
					sb.append( redir );
				}
				if( poolsize != null ) {
					sb.append( "\" poolsize=\"" );
					sb.append( poolsize.toString() );
				}
				if( urlVersion != null ) {
					sb.append( "\" urlVersion=\"" );
					sb.append( urlVersion.toString() );
				}
				sb.append( "\">\n" );

				for( final MInstance anInst : anApp.instanceArray() ) {

					if( !(onlyIncludeRunningInstances && !anInst.isRunning_W()) ) {

						anInst.extractAdaptorValuesFromApplication();

						final Integer id = (Integer)anInst.values.valueForKey( "id" );
						final Integer port = (Integer)anInst.values.valueForKey( "port" );
						final String host = (String)anInst.values.valueForKey( "hostName" );
						final Integer sendTimeout = (Integer)anInst.adaptorValues.valueForKey( "sendTimeout" );
						final Integer recvTimeout = (Integer)anInst.adaptorValues.valueForKey( "recvTimeout" );
						final Integer cnctTimeout = (Integer)anInst.adaptorValues.valueForKey( "cnctTimeout" );
						final Integer sendBufSize = (Integer)anInst.adaptorValues.valueForKey( "sendBufSize" );
						final Integer recvBufSize = (Integer)anInst.adaptorValues.valueForKey( "recvBufSize" );

						sb.append( "    <instance" );

						if( id != null ) {
							sb.append( " id=\"" );
							sb.append( id.toString() );
						}
						if( port != null ) {
							sb.append( "\" port=\"" );
							sb.append( port.toString() );
						}
						if( host != null ) {
							sb.append( "\" host=\"" );
							sb.append( host );
						}
						if( sendTimeout != null ) {
							sb.append( "\" sendTimeout=\"" );
							sb.append( sendTimeout.toString() );
						}
						if( recvTimeout != null ) {
							sb.append( "\" recvTimeout=\"" );
							sb.append( recvTimeout.toString() );
						}
						if( cnctTimeout != null ) {
							sb.append( "\" cnctTimeout=\"" );
							sb.append( cnctTimeout.toString() );
						}
						if( sendBufSize != null ) {
							sb.append( "\" sendBufSize=\"" );
							sb.append( sendBufSize.toString() );
						}
						if( recvBufSize != null ) {
							sb.append( "\" recvBufSize=\"" );
							sb.append( recvBufSize.toString() );
						}
						sb.append( "\"/>\n" );
					} // end if (!(onlyIncludeRunningInstances && !anInst.isRunning()));
				}

				sb.append( "  </application>\n" );
			} // end if (!(onlyIncludeRunningInstances && anApp.isRunning()))
		} // end Application Enumeration

		if( shouldIncludeUnregisteredInstances ) {
			// For unknown/unregistered instances
			final ProtoLocalMonitor plMonitor = (ProtoLocalMonitor)WOApplication.application().valueForKey( "localMonitor" );
			if( plMonitor != null ) {
				final StringBuffer unknownSB = plMonitor.generateAdaptorConfigXML();
				if( unknownSB.length() > 0 ) {
					sb.append( unknownSB );
				}
			}
		}

		sb.append( "</adaptor>\n" );
		return sb.toString();
	}

	public String generateSiteConfigXML() {
		return new CoderWrapper().encodeRootObjectForKey( dictionaryForArchive(), "SiteConfig" );
	}

	private String _lastConfig;

	private void backup( String action ) {
		if( Boolean.getBoolean( "WODeploymentBackups" ) ) {
			final String currentSiteConfig = generateSiteConfigXML();
			if( !_lastConfig.equals( generateSiteConfigXML() ) ) {
				final String date = new SimpleDateFormat( "yyyy-MM-dd-hh_mm_ss" ).format( new Date() );
				saveSiteConfig( new File( fileForSiteConfig().getParentFile(), "SiteConfigBackup.xml." + date + "." + action ), _lastConfig, true );
				_lastConfig = currentSiteConfig;
			}
		}
	}

	public void forceBackup( String reason ) {
		reason = reason != null ? "." + reason : "";
		final String date = new SimpleDateFormat( "yyyy-MM-dd-hh_mm_ss" ).format( new Date() );
		saveSiteConfig( new File( fileForSiteConfig().getParentFile(), "SiteConfigBackup.xml." + date + reason ), generateSiteConfigXML(), true );
	}

	public NSDictionary dictionaryForArchive() {
		final int hostArrayCount = _hostArray.size();
		final int applicationArrayCount = _applicationArray.size();
		final int instanceArrayCount = _instanceArray.size();

		final NSMutableDictionary siteConfig = new NSMutableDictionary( 4 );

		final NSMutableDictionary site = values;

		final NSMutableArray<MHost> hostArray = new NSMutableArray<>( hostArrayCount );

		for( int i = 0; i < hostArrayCount; i++ ) {
			final MHost anMobject = _hostArray.get( i );
			hostArray.addObject( anMobject.values );
		}

		final NSMutableArray<MApplication> applicationArray = new NSMutableArray<>( applicationArrayCount );

		for( int i = 0; i < applicationArrayCount; i++ ) {
			final MApplication anMobject = _applicationArray.get( i );
			applicationArray.addObject( anMobject.values );
		}

		final NSMutableArray<MInstance> instanceArray = new NSMutableArray<>( instanceArrayCount );

		for( int i = 0; i < instanceArrayCount; i++ ) {
			final MInstance anMobject = _instanceArray.get( i );
			instanceArray.addObject( anMobject.values );
		}

		siteConfig.takeValueForKey( site, "site" );
		siteConfig.takeValueForKey( hostArray, "hostArray" );
		siteConfig.takeValueForKey( applicationArray, "applicationArray" );
		siteConfig.takeValueForKey( instanceArray, "instanceArray" );

		return siteConfig;
	}

	@Override
	public String toString() {
		return values.toString() + "\n" + "hasChanges = " + _hasChanges + "\n" + "configDirectoryPath = " + _configDirectoryPath;
	}

	// KH - all these should be cached!
	public long autoRecoverInterval() {
		final int instanceArrayCount = _instanceArray.count();
		int smallestInterval = 0;
		for( int i = 0; i < instanceArrayCount; i++ ) {
			final MInstance anInst = _instanceArray.objectAtIndex( i );
			final Integer Interval = anInst.lifebeatInterval();
			if( Interval != null ) {
				final int interval = Interval.intValue();
				if( interval < smallestInterval ) {
					smallestInterval = interval;
				}
			}
		}
		if( smallestInterval < 1 ) {
			return 30 * 1000;
		}
		return smallestInterval * 1000;
	}

	public MApplication applicationWithName( String anAppName ) {

		if( anAppName == null ) {
			return null;
		}

		for( final MApplication anApp : _applicationArray ) {
			if( anApp.name().equals( anAppName ) ) {
				return anApp;
			}
		}

		return null;
	}

	public MHost hostWithName( String aHostName ) {

		if( aHostName == null ) {
			return null;
		}

		if( aHostName.equals( "localhost" ) ) {
			return localHost();
		}

		for( final MHost aHost : _hostArray ) {
			if( aHost.name().equals( aHostName ) ) {
				return aHost;
			}
		}

		return null;
	}

	public boolean localhostOrLoopbackHostExists() {

		final String localhost = "localhost";
		final String loopback = "127.0.0.1";

		for( final MHost aHost : _hostArray ) {
			if( aHost.name().equals( localhost ) || aHost.name().equals( loopback ) ) {
				return true;
			}
		}

		return false;
	}

	public MHost hostWithAddress( InetAddress anAddress ) {

		if( anAddress == null ) {
			return null;
		}

		if( _localHost != null && anAddress.equals( localHostAddress ) ) {
			return _localHost;
		}

		for( final MHost aHost : _hostArray ) {
			if( anAddress.equals( aHost.address() ) ) {
				return aHost;
			}
		}

		return null;
	}

	public MInstance instanceWithName( String anInstanceName ) {

		if( anInstanceName == null ) {
			return null;
		}

		for( final MInstance anInstance : _instanceArray ) {
			if( anInstance.displayName().equals( anInstanceName ) ) {
				return anInstance;
			}
		}

		return null;
	}

	public MInstance instanceWithHostnameAndPort( String hostName, Integer port ) {

		final MHost aHost = hostWithName( hostName );

		if( aHost == null ) {
			return null;
		}

		return aHost.instanceWithPort( port );
	}

	public MInstance instanceWithHostAndPort( String name, InetAddress host, String port ) {

		try {
			final Integer anIntPort = Integer.valueOf( port );
			final MHost aHost = hostWithAddress( host );

			if( aHost == null ) {
				return null;
			}

			final MInstance anInstance = aHost.instanceWithPort( anIntPort );

			if( anInstance != null ) {
				if( anInstance.applicationName().equals( name ) ) {
					return anInstance;
				}
			}
		}
		catch( final Exception e ) {
			logger.error( "Exception getting instance: {}:{}", host, port, e );
		}

		return null;
	}

	public int _appIsDeadMultiplier;
}