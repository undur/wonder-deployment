package com.webobjects.monitor.application.admin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.StringExtensions;
import com.webobjects.monitor.application.components.AppDetailPage;
import com.webobjects.monitor.application.components.ApplicationsPage;

public class AdminApplicationsPage extends ApplicationsPage {

	public static final String DISPLAY_NAME = "displayName";
	public static final String ACTION_NAME = "actionName";
//	protected static NSArray _actions;
//	public NSArray actions;
	public NSDictionary selectedAction;
	public NSDictionary currentActionItem;
	protected NSMutableArray processedHosts;
	protected NSMutableArray processedInstances;

//	static {
//		try {
//			Class c = AdminApplicationsPage.class;
//			Class aclass[] = { List.class };
//			String[] keys = new String[] { DISPLAY_NAME, ACTION_NAME };
//			_actions = new NSArray( new NSDictionary[] {
//					new NSDictionary( new Object[] { "Start", c.getMethod( "start", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Stop", c.getMethod( "stop", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Turn Auto Recover on for", c.getMethod( "turnAutoRecoverOn", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Turn Auto Recover off for", c.getMethod( "turnAutoRecoverOff", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Turn Refuse New Sessions on for", c.getMethod( "turnRefuseNewSessionsOn", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Turn Refuse New Sessions off for", c.getMethod( "turnRefuseNewSessionsOff", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Turn Scheduled on for", c.getMethod( "turnScheduledOn", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Turn Scheduled off for", c.getMethod( "turnScheduledOff", aclass ) }, keys ),
//					new NSDictionary( new Object[] { "Force Quit", c.getMethod( "forceQuit", aclass ) }, keys )
//			} );
//		}
//		catch( NoSuchMethodException nosuchmethodexception ) {
//			nosuchmethodexception.printStackTrace();
//		}
//	}

	public AdminApplicationsPage( WOContext context ) {
		super( context );
//		actions = _actions;
		processedHosts = new NSMutableArray();
		processedInstances = new NSMutableArray();
	}

	private void processedInstance( MInstance minstance ) {
		processedInstances.addObject( minstance );
		processedHosts.addObject( minstance.host() );
	}

	private void cleanup() {
		processedInstances.removeAllObjects();
		processedHosts.removeAllObjects();
	}

	private void sendUpdateInstancesToWotaskds() {
		if( processedInstances.count() > 0 ) {
			handler().sendUpdateInstancesToWotaskds( processedInstances, processedHosts );
		}
		cleanup();
	}

	private void sendCommandInstancesToWotaskds( String s ) {
		if( processedInstances.count() > 0 ) {
			handler().sendCommandInstancesToWotaskds( s, processedInstances, processedHosts );
		}
		cleanup();
	}

	public void clearDeaths( List<MInstance> nsarray ) {

		for( MInstance minstance : nsarray ) {
			processedInstance( minstance );
		}

		sendCommandInstancesToWotaskds( "CLEAR" );
	}

	public void scheduleType( List<MInstance> nsarray, String scheduleType ) {
		// Should be one of "HOURLY", "DAILY", "WEEKLY"
		for( MInstance minstance : nsarray ) {
			minstance.setSchedulingType( scheduleType );
			processedInstance( minstance );
		}
		sendUpdateInstancesToWotaskds();
	}

	public void hourlyStartHours( List<MInstance> nsarray, int beginScheduleWindow, int endScheduleWindow, int interval ) {
		int hour = beginScheduleWindow;
		for( MInstance minstance : nsarray ) {
			if( hour > endScheduleWindow )
				hour = beginScheduleWindow;
			minstance.setSchedulingHourlyStartTime( Integer.valueOf( hour ) );
			minstance.setSchedulingInterval( Integer.valueOf( interval ) );
			processedInstance( minstance );
			hour++;
		}
		sendUpdateInstancesToWotaskds();
	}

	public void dailyStartHours( List<MInstance> nsarray, int beginScheduleWindow, int endScheduleWindow ) {
		int hour = beginScheduleWindow;
		for( MInstance minstance : nsarray ) {
			if( hour > endScheduleWindow )
				hour = beginScheduleWindow;
			minstance.setSchedulingDailyStartTime( Integer.valueOf( hour ) );
			processedInstance( minstance );
			hour++;
		}
		sendUpdateInstancesToWotaskds();
	}

	public void weeklyStartHours( List<MInstance> nsarray, int beginScheduleWindow, int endScheduleWindow, int startDay ) {
		int hour = beginScheduleWindow;
		for( MInstance minstance : nsarray ) {
			if( hour > endScheduleWindow )
				hour = beginScheduleWindow;
			minstance.setSchedulingWeeklyStartTime( Integer.valueOf( hour ) );
			minstance.setSchedulingStartDay( Integer.valueOf( startDay ) );
			processedInstance( minstance );
			hour++;
		}
		sendUpdateInstancesToWotaskds();
	}

	public void turnScheduledOn( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( !minstance.isScheduled() ) {
				minstance.setSchedulingEnabled( Boolean.TRUE );
				processedInstance( minstance );
			}
		}
		sendUpdateInstancesToWotaskds();
	}

	public void turnScheduledOff( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( minstance.isScheduled() ) {
				minstance.setSchedulingEnabled( Boolean.FALSE );
				processedInstance( minstance );
			}
		}
		sendUpdateInstancesToWotaskds();
	}

	public void setAdditionalArgs( List<MInstance> instances, String arguments ) {
		for( MInstance instance : instances ) {
			String instArgs = instance.additionalArgs();
			if( instArgs == null || !arguments.equals( instArgs ) ) {
				instance.setAdditionalArgs( arguments );
				processedInstance( instance );
			}
		}
		sendUpdateInstancesToWotaskds();
	}

	public void turnRefuseNewSessionsOn( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( !minstance.isRefusingNewSessions() ) {
				minstance.setRefusingNewSessions( true );
				processedInstance( minstance );
			}
		}
		sendCommandInstancesToWotaskds( "REFUSE" );
	}

	public void turnRefuseNewSessionsOff( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( minstance.isRefusingNewSessions() ) {
				minstance.setRefusingNewSessions( false );
				processedInstance( minstance );
			}
		}
		sendCommandInstancesToWotaskds( "ACCEPT" );
	}

	public void turnAutoRecoverOn( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( minstance.autoRecover() == null || !minstance.autoRecover().booleanValue() ) {
				minstance.setAutoRecover( Boolean.TRUE );
				processedInstance( minstance );
			}
		}
		sendUpdateInstancesToWotaskds();
	}

	public void turnAutoRecoverOff( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( minstance.autoRecover() != null && minstance.autoRecover().booleanValue() ) {
				minstance.setAutoRecover( Boolean.FALSE );
				processedInstance( minstance );
			}
		}
		sendUpdateInstancesToWotaskds();
	}

	public void forceQuit( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			minstance.state = MObject.STOPPING;
			processedInstance( minstance );
		}
		sendCommandInstancesToWotaskds( "QUIT" );
	}

	public void stop( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( minstance.state == MObject.ALIVE || minstance.state == MObject.STARTING ) {
				minstance.state = MObject.STOPPING;
				processedInstance( minstance );
			}
		}
		sendCommandInstancesToWotaskds( "STOP" );
	}

	public void start( List<MInstance> nsarray ) {
		for( MInstance minstance : nsarray ) {
			if( minstance.state == MObject.DEAD || minstance.state == MObject.STOPPING || minstance.state == MObject.CRASHING || minstance.state == MObject.UNKNOWN ) {
				minstance.state = MObject.STARTING;
				processedInstance( minstance );
			}
		}
		sendCommandInstancesToWotaskds( "START" );
	}

	public void bounce( List<MApplication> applications ) {
		bounceGraceful( applications );
	}

	public void bounceGraceful( List<MApplication> applications ) {
		for( MApplication application : applications ) {
			AppDetailPage page = AppDetailPage.create( context(), application );
			page = (AppDetailPage)page.bounceClickedWithGracefulBouncer();
		}
	}

	public void bounceShutdown( List<MApplication> applications, int maxwait ) {
		for( MApplication application : applications ) {
			AppDetailPage page = AppDetailPage.create( context(), application );
			page = (AppDetailPage)page.bounceClickedWithShutdownBouncer( maxwait );
		}
	}

	public void bounceRolling( List<MApplication> applications ) {
		for( MApplication application : applications ) {
			AppDetailPage page = AppDetailPage.create( context(), application );
			page = (AppDetailPage)page.bounceClickedWithRollingBouncer();
		}
	}

	@Override
	public WOComponent bounceClicked() {
		AppDetailPage page = AppDetailPage.create( context(), currentApplication );
		page = (AppDetailPage)page.bounceClicked();
		return page;
	}

	private NSArray allInstances() {
		NSMutableArray nsmutablearray = new NSMutableArray();

		for( Enumeration enumeration = applicationArray().objectEnumerator(); enumeration.hasMoreElements(); ) {
			NSArray instances = ((MApplication)enumeration.nextElement()).instanceArray();
			nsmutablearray.addObjectsFromArray( instances );
		}

		return nsmutablearray;
	}

	private NSMutableArray applicationArray() {
		return siteConfig().applicationArray();
	}

	private WOComponent performInstanceAction() {
		handler().startReading();
		try {
			((Method)selectedAction.valueForKey( "actionName" )).invoke( this, new Object[] { allInstances() } );
		}
		catch( IllegalArgumentException e ) {
			e.printStackTrace();
		}
		catch( IllegalAccessException e ) {
			e.printStackTrace();
		}
		catch( InvocationTargetException e ) {
			e.printStackTrace();
		}
		finally {
			handler().endReading();
		}
		return AdminApplicationsPage.create( context() );
	}

	private boolean showMovers() {
		return applicationArray().count() > 1;
	}

	private WOComponent saveMoving() {
		handler().startReading();
		try {
			MHost mhost;
			NSArray hosts = siteConfig().hostArray();
			for( Enumeration enumeration = hosts.objectEnumerator(); enumeration.hasMoreElements(); ) {
				mhost = (MHost)enumeration.nextElement();
				handler().sendOverwriteToWotaskd( mhost );
			}
			return AdminApplicationsPage.create( context() );
		}
		finally {
			handler().endReading();
		}
	}

	private WOComponent moveUpClicked() {
		handler().startReading();
		try {
			NSMutableArray nsmutablearray = applicationArray();
			int i = nsmutablearray.indexOfObject( currentApplication );
			nsmutablearray.removeObjectAtIndex( i );
			if( i == 0 )
				nsmutablearray.addObject( currentApplication );
			else
				nsmutablearray.insertObjectAtIndex( currentApplication, i - 1 );
			siteConfig().dataHasChanged();
			return AdminApplicationsPage.create( context() );
		}
		finally {
			handler().endReading();
		}
	}

	private WOComponent moveDownClicked() {
		handler().startReading();
		try {
			NSMutableArray nsmutablearray = applicationArray();
			int i = nsmutablearray.indexOfObject( currentApplication );
			nsmutablearray.removeObjectAtIndex( i );
			if( i == nsmutablearray.count() )
				nsmutablearray.insertObjectAtIndex( currentApplication, 0 );
			else
				nsmutablearray.insertObjectAtIndex( currentApplication, i + 1 );
			siteConfig().dataHasChanged();
			return AdminApplicationsPage.create( context() );
		}
		finally {
			handler().endReading();
		}
	}

	@Override
	public WOComponent addApplicationClicked() {
		String s = null;
		WOComponent result = null;
		if( !StringExtensions.isValidXMLString( newApplicationName ) )
			s = "\"" + newApplicationName
					+ "\" is an invalid application name.";
		if( siteConfig().applicationWithName( newApplicationName ) != null )
			s = "An application with the name \"" + newApplicationName
					+ "\" does already exist.";
		if( s != null ) {
			result = AdminApplicationsPage.create( context() );
		}
		else {
			result = super.addApplicationClicked();
		}
		return result;
	}

	public static WOComponent create( WOContext context ) {
		return WOApplication.application().pageWithName( AdminApplicationsPage.class.getName(), context );
	}
}