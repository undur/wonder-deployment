package com.webobjects.monitor.application.admin;

import java.util.ArrayList;
import java.util.List;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.application.Session;
import com.webobjects.monitor.application.components.AppDetailPage;
import com.webobjects.monitor.util.WOTaskdHandler;

/**
 * <p>
 * The following direct actions were added to Monitor. They might be useful for
 * creating scripts to automate deployments of new WO application versions.
 * (First time deployments and config changes would still require interactive
 * sessions in Monitor.) Each direct action returns a short string (instead of a
 * full HTML page) and an HTTP status code indicating whether the respective
 * action was executed successfully. If Monitor is password-protected, the
 * password must be passed on the URL with the name "pw", (e.g. &pw=foo). If the
 * password is missing or incorrect, these direct actions are not permitted to be 
 * executed.
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Direct Action</th>
 * <th>Return Values</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>running</td>
 * <td rowspan="2">'YES', 'NO', or<br>
 * error message</td>
 * <td>checks whether instances are running (alive)</td>
 * </tr>
 * <tr>
 * <td>stopped</td>
 * <td>checks whether instances have stopped (are dead)</td>
 * </tr>
 * <tr>
 * <td colspan="3"></td>
 * </tr>
 * <tr>
 * <td>start</td>
 * <td rowspan="3">'OK' or <br>
 * error message</td>
 * <td>attempts to start instances which have been stopped or are stopping</td>
 * </tr>
 * <tr>
 * <td>stop</td>
 * <td>attempts to stops instances which are running or starting</td>
 * </tr>
 * <tr>
 * <td>forceQuit</td>
 * <td>stops instances forcefully</td>
 * </tr>
 * <tr>
 * <td colspan="3"></td>
 * </tr>
 * <tr>
 * <td>turnAutoRecoverOn</td>
 * <td rowspan="2">'OK' or <br>
 * error message</td>
 * <td>turns Auto Recover on</td>
 * </tr>
 * <tr>
 * <td>turnAutoRecoverOff</td>
 * <td>turns Auto Recover off</td>
 * </tr>
 * <tr>
 * <td colspan="3"></td>
 * </tr>
 * <tr>
 * <td>turnRefuseNewSessionsOn</td>
 * <td rowspan="2">'OK' or <br>
 * error message</td>
 * <td>turns Refuse New Sessions on</td>
 * </tr>
 * <tr>
 * <td>turnRefuseNewSessionsOff</td>
 * <td>turns Refuse New Sessions off</td>
 * </tr>
 * <tr>
 * <td colspan="3"></td>
 * </tr>
 * <tr>
 * <td>turnScheduledOn</td>
 * <td rowspan="2">'OK' or <br>
 * error message</td>
 * <td>turns Scheduled on</td>
 * </tr>
 * <tr>
 * <td>turnScheduledOff</td>
 * <td>turns Scheduled off</td>
 * </tr>
 * <tr>
 * <td colspan="3"></td>
 * </tr>
 * <tr>
 * <td>clearDeaths</td>
 * <td>'OK' or <br>
 * error message</td>
 * <td>sets the number of deaths to 0</td>
 * </tr>
 * <tr>
 * <td>bounce</td>
 * <td>'OK' or <br>
 * error message</td>
 * <td>bounces the application (starts a few instances per hosts, set the rest to refusing sessions and auto-recover)</td>
 * </tr>
 * <tr>
 * <td>setAdditionalArgs</td>
 * <td>'OK' or <br>
 * error message</td>
 * <td>updates the instances' additional command line arguments</td>
 * </tr>
 * <tr>
 * <td>info</td>
 * <td>JSON or<br>
 * error message</td>
 * <td>returns a JSON encoded list of instances with all the data from the app detail page.  Add form value info=full to also return the Additional Arguments.</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * All direct actions must be invoked with a type:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Type</th>
 * <th>Description</th>
 * <th>Requires Names</th>
 * </tr>
 * <tr>
 * <td>all</td>
 * <td>all instances of all applications</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>app</td>
 * <td>all instances of the specified applications</td>
 * <td rowspan="2">yes</td>
 * </tr>
 * <tr>
 * <td>ins</td>
 * <td>all the specified instances</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * The direct action 'running' can be invoked with a num argument:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Num</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>all / -1</td>
 * <td>all instances of the application must be running. this is the default if no num argument is set</td>
 * </tr>
 * <tr>
 * <td><i>number</i></td>
 * <td>a minimum of <i>number</i> instances of the specified application must be running. if there are less instances configured acts like 'all'</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * The direct action 'bounce' can be invoked with additional arguments:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Argument</th>
 * <th>Value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>bouncetype</td>
 * <td>graceful | shutdown | rolling</td>
 * <td>graceful bounces the application by starting a few instances per host and setting the rest to refusing sessions<br />
 * shutdown bounces the application by stopping all instances and then restarting them (use this if your<br />
 * application will migrate the database so the old application will crash)<br />
 * rolling will start a few instances per host, then forcefully restart the existing instances one at a time<br/>
 * The default bouncetype is graceful.</td>
 * </tr>
 * <tr>
 * <td>maxwait</td>
 * <td><i>secs</i></td>
 * <td>number of seconds to wait for applications to shut down themselves before force quitting the instances.<br />
 * The default is 30 seconds.</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * The direct action <code>setAdditionalArgs</code> must be invoked with the following argument:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Argument</th>
 * <th>Value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>args</code></td>
 * <td><i>string</i></td>
 * <td>the additional arguments to be passed to the instance on startup</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * Possible status codes:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Code</th>
 * <th>Circumstance</th>
 * </tr>
 * <tr>
 * <td>200 (OK)</td>
 * <td>return value is 'OK' or 'YES'</td>
 * </tr>
 * <tr>
 * <td>403 (Unauthorized)</td>
 * <td>Monitor is password protected</td>
 * </tr>
 * <tr>
 * <td>404 (Not Found)</td>
 * <td>one or more of the supplied application or instance names can't be found</td>
 * </tr>
 * <tr>
 * <td>406 (Not Acceptable)</td>
 * <td>an unknown type is supplied, or names are required but missing</td>
 * </tr>
 * <tr>
 * <td>417 (Not Expected)</td>
 * <td>return value is 'NO'</td>
 * </tr>
 * <tr>
 * <td>500 (Error)</td>
 * <td>software defect (please <A HREF="mailto:christian@pekeler.org">send</A>
 * stacktrace from Monitor's log)</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * Examples:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>URL</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>
 * .../JavaMonitor.woa/admin/start?type=app&amp;name=AppleStore&amp;name=MemberSite
 * </td>
 * <td>Starts all instances of the AppleStore and the MemberSite applications.
 * Returns error if any of these applications are unknown to Monitor, OK
 * otherwise.</td>
 * </tr>
 * <tr>
 * <td>.../JavaMonitor.woa/admin/turnScheduledOff?type=all</td>
 * <td>Turns scheduling off for all instances of all applications, then returns
 * OK.</td>
 * </tr>
 * <tr>
 * <td>
 * .../JavaMonitor.woa/admin/stopped?type=ins&amp;name=AppleStore-4&amp;name=
 * MemberSite-8&amp;name=AppleStore-2</td>
 * <td>Returns YES if the instances 2 and 4 of the AppleStore and instance 8 of
 * the MemberSite are all dead. Returns NO if at least one of them has not
 * stopped. Returns error if any of these instances are unknown to Monitor.</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * A simple deployment script could look as follows:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <td><tt>#!/bin/sh<br>
        <br>
        # clean build<br>
        ant clean install <br>
        <br>
        # run unit tests<br>
        ant test <br>
        <br>
        # stop application<br>
        result=`curl -s
        http://bigserver:1086/cgi-bin/WebObjects/JavaMonitor.woa/admin/stop\?type=app\&amp;name=MemberSite`<br>
        [ &quot;$result&quot; = OK ] || { echo $result; exit 1; }<br>
        <br>
        # deploy new application<br>
        scp -rq /Library/WebObjects/Applications/MemberSite.woa
        bigserver:/Library/WebObjects/Applications/<br>
        <br>
        # start application<br>
        result=`curl -s
        http://bigserver:1086/cgi-bin/WebObjects/JavaMonitor.woa/admin/start\?type=app\&amp;name=MemberSite`<br>
        [ &quot;$result&quot; = OK ] || { echo $result; exit 1; }<br>
        <br>
        echo &quot;deployment completed&quot;</tt><br>
 * </td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * Invoking direct actions manually:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <td><tt>curl -w " (status: %{http_code})\n"
        http://bigserver:1086/cgi-bin/WebObjects/JavaMonitor.woa/admin/forceQuit\?type=ins\&name=AppleStore-3
 * </td>
 * </tr>
 * </table>
 * 
 * @author christian@pekeler.org
 * @author ak
 * 
 */
public class AdminAction extends WODirectAction {

	public class DirectActionException extends RuntimeException {

		public int status;

		public DirectActionException( String s, int i ) {
			super( s );
			status = i;
		}
	}

	/**
	 * FIXME: Not sure why this is here, never used
	 */
//	private static List<String> supportedActionNames = List.of( "running","bounce", "stopped", "start", "stop", "forceQuit", "turnAutoRecoverOn", "turnAutoRecoverOff", "turnRefuseNewSessionsOn", "turnRefuseNewSessionsOff", "turnScheduledOn", "turnScheduledOff", "turnAutoRecoverOn", "turnAutoRecoverOff", "clearDeaths", "info" );

	private AdminApplicationsPage applicationsPage;

	private List<MInstance> instances;

	private List<MApplication> applications;

	private WOTaskdHandler _handler;

	public AdminAction( WORequest request ) {
		super( request );
		instances = new ArrayList();
		applications = new ArrayList();
		_handler = new WOTaskdHandler( (Session)session() );
	}

	private AdminApplicationsPage applicationsPage() {
		if( applicationsPage == null ) {
			applicationsPage = new AdminApplicationsPage( context() );
		}

		return applicationsPage;
	}

	public WOActionResults infoAction() {
		WOResponse woresponse = new WOResponse();
		String result = "";
		for( MInstance minstance : instances ) {
			result += (result.length() == 0 ? "" : ", \n");
			result += "{";
			result += "\"name\": \"" + minstance.applicationName() + "\", ";
			result += "\"id\": \"" + minstance.id() + "\", ";
			result += "\"host\": \"" + minstance.hostName() + "\", ";
			result += "\"port\": \"" + minstance.port() + "\", ";
			result += "\"state\": \"" + MObject.INSTANCE_STATES[minstance.state] + "\", ";
			result += "\"deaths\": \"" + minstance.deathCount() + "\", ";
			result += "\"refusingNewSessions\": " + minstance.isRefusingNewSessions() + ", ";
			result += "\"scheduled\": " + minstance.isScheduled() + ", ";
			result += "\"schedulingHourlyStartTime\": " + minstance.schedulingHourlyStartTime() + ", ";
			result += "\"schedulingDailyStartTime\": " + minstance.schedulingDailyStartTime() + ", ";
			result += "\"schedulingWeeklyStartTime\": " + minstance.schedulingWeeklyStartTime() + ", ";
			result += "\"schedulingType\": \"" + minstance.schedulingType() + "\", ";
			result += "\"schedulingStartDay\": " + minstance.schedulingStartDay() + ", ";
			result += "\"schedulingInterval\": " + minstance.schedulingInterval() + ", ";
			result += "\"transactions\": \"" + minstance.transactions() + "\", ";
			result += "\"activeSessions\": \"" + minstance.activeSessions() + "\", ";
			result += "\"averageIdlePeriod\": \"" + minstance.averageIdlePeriod() + "\", ";
			result += "\"avgTransactionTime\": \"" + minstance.avgTransactionTime() + "\",";
			result += "\"autoRecover\": \"" + minstance.isAutoRecovering() + "\"";

			String infoMode = (String)context().request().formValueForKey( "info" );
			if( "full".equalsIgnoreCase( infoMode ) ) {
				result += ", \"additionalArgs\": \"";
				if( minstance.additionalArgs() != null ) {
					result += minstance.additionalArgs().replace( "\"", "\\\"" );
				}
				result += "\"";
			}
			result += "}";
		}
		woresponse.appendContentString( "[" + result + "]" );
		return woresponse;
	}

	public WOActionResults runningAction() {
		WOResponse woresponse = new WOResponse();
		woresponse.setContent( "YES" );
		String num = (String)context().request().formValueForKey( "num" );
		int numberOfInstancesRequested = -1;
		if( num != null && !num.equals( "" ) && !num.equalsIgnoreCase( "all" ) ) {
			try {
				numberOfInstancesRequested = Integer.valueOf( num ).intValue();
				if( numberOfInstancesRequested > instances.size() ) {
					numberOfInstancesRequested = -1;
				}
			}
			catch( Exception e ) {
				// ignore
			}
		}
		int instancesAlive = 0;
		for( MInstance minstance : instances ) {
			if( minstance.state == MObject.ALIVE ) {
				instancesAlive++;
			}
		}
		if( (numberOfInstancesRequested == -1 && instancesAlive < instances.size()) || instancesAlive < numberOfInstancesRequested ) {
			woresponse.setContent( "NO" );
			woresponse.setStatus( 417 );
		}
		return woresponse;
	}

	public WOActionResults stoppedAction() {
		WOResponse woresponse = new WOResponse();
		woresponse.setContent( "YES" );
		for( MInstance minstance : instances ) {
			if( minstance.state == MObject.DEAD )
				continue;
			woresponse.setContent( "NO" );
			woresponse.setStatus( 417 );
			break;
		}
		return woresponse;
	}

	public WOActionResults bounceAction() {
		WOResponse woresponse = new WOResponse();
		woresponse.setContent( "OK" );
		String bouncetype = (String)context().request().formValueForKey( "bouncetype" );
		String maxwaitString = (String)context().request().formValueForKey( "maxwait" );
		if( bouncetype == null || bouncetype == "" || bouncetype.equalsIgnoreCase( "graceful" ) ) {
			applicationsPage().bounceGraceful( applications );
		}
		else if( bouncetype.equalsIgnoreCase( "shutdown" ) ) {
			int maxwait = 30;
			if( maxwaitString != null ) {
				try {
					maxwait = Integer.valueOf( maxwaitString ).intValue();
				}
				catch( NumberFormatException e ) {
					// ignore
				}
			}
			applicationsPage().bounceShutdown( applications, maxwait );
		}
		else if( bouncetype.equalsIgnoreCase( "rolling" ) ) {
			applicationsPage().bounceRolling( applications );
		}
		else {
			woresponse.setContent( "Unknown bouncetype" );
			woresponse.setStatus( 406 );
		}
		return woresponse;
	}

	public void clearDeathsAction() {
		applicationsPage().clearDeaths( instances );
	}

	public void scheduleTypeAction() {
		String scheduleType = (String)context().request().formValueForKey( "scheduleType" );
		if( scheduleType != null && ("HOURLY".equals( scheduleType ) || "DAILY".equals( scheduleType ) || "WEEKLY".equals( scheduleType )) )
			applicationsPage().scheduleType( instances, scheduleType );
	}

	public void hourlyScheduleRangeAction() {
		String beginScheduleWindow = (String)context().request().formValueForKey( "hourBegin" );
		String endScheduleWindow = (String)context().request().formValueForKey( "hourEnd" );
		String interval = (String)context().request().formValueForKey( "interval" );
		if( beginScheduleWindow != null && endScheduleWindow != null && interval != null )
			applicationsPage().hourlyStartHours( instances, Integer.parseInt( beginScheduleWindow ), Integer.parseInt( endScheduleWindow ), Integer.parseInt( interval ) );
	}

	public void dailyScheduleRangeAction() {
		String beginScheduleWindow = (String)context().request().formValueForKey( "hourBegin" );
		String endScheduleWindow = (String)context().request().formValueForKey( "hourEnd" );
		if( beginScheduleWindow != null && endScheduleWindow != null )
			applicationsPage().dailyStartHours( instances, Integer.parseInt( beginScheduleWindow ), Integer.parseInt( endScheduleWindow ) );
	}

	public void weeklyScheduleRangeAction() {
		String beginScheduleWindow = (String)context().request().formValueForKey( "hourBegin" );
		String endScheduleWindow = (String)context().request().formValueForKey( "hourEnd" );
		String weekDay = (String)context().request().formValueForKey( "weekDay" );
		if( beginScheduleWindow != null && endScheduleWindow != null && weekDay != null )
			applicationsPage().weeklyStartHours( instances, Integer.parseInt( beginScheduleWindow ), Integer.parseInt( endScheduleWindow ), Integer.parseInt( weekDay ) );
	}

	public void setAdditionalArgsAction() {
		String arguments = (String)context().request().formValueForKey( "args" );
		if( arguments != null )
			applicationsPage().setAdditionalArgs( instances, arguments );
	}

	public void turnScheduledOnAction() {
		applicationsPage().turnScheduledOn( instances );
	}

	public void turnScheduledOffAction() {
		applicationsPage().turnScheduledOff( instances );
	}

	public void turnRefuseNewSessionsOnAction() {
		applicationsPage().turnRefuseNewSessionsOn( instances );
	}

	public void turnRefuseNewSessionsOffAction() {
		applicationsPage().turnRefuseNewSessionsOff( instances );
	}

	public void turnAutoRecoverOnAction() {
		applicationsPage().turnAutoRecoverOn( instances );
	}

	public void turnAutoRecoverOffAction() {
		applicationsPage().turnAutoRecoverOff( instances );
	}

	public void forceQuitAction() {
		applicationsPage().forceQuit( instances );
	}

	public void stopAction() {
		applicationsPage().stop( instances );
	}

	public void startAction() {
		applicationsPage().start( instances );
	}

	private void prepareApplications( List<String> appNames ) {
		if( appNames == null )
			throw new DirectActionException( "at least one application name needs to be specified for type app", 406 );
		for( String s  : appNames ) {
			final MApplication mapplication = siteConfig().applicationWithName( s );

			if( mapplication != null ) {
				applications.add( mapplication );
				addInstancesForApplication( mapplication );
			}
			else
				throw new DirectActionException( "Unknown application " + s, 404 );
		}

	}

	private void prepareApplicationsOnHosts( final List<String> appNames, final List<String> hostNames ) {

		if( appNames == null ) {
			throw new DirectActionException( "at least one application name needs to be specified for type app", 406 );
		}

		for( final String appName : appNames ) {
			final MApplication mApplication = siteConfig().applicationWithName( appName );

			if( mApplication != null ) {
				final List<MInstance> hostInstances = new ArrayList<>();
				
				for( final MInstance mInstance : mApplication.instanceArray() ) {
					if( hostNames.contains( mInstance.hostName() ) ) {
						hostInstances.add( mInstance );
					}
				}

				instances.addAll( hostInstances );
			}
			else
				throw new DirectActionException( "Unknown application " + appName, 404 );
		}
	}

	private void prepareInstances( List<String> appNamesAndNumbers ) {
		if( appNamesAndNumbers == null )
			throw new DirectActionException( "at least one instance name needs to be specified for type ins", 406 );
		for( String s : appNamesAndNumbers ) {
			MInstance minstance = siteConfig().instanceWithName( s );
			if( minstance != null )
				instances.add( minstance );
			else
				throw new DirectActionException( "Unknown instance " + s, 404 );
		}

	}

	private void addInstancesForApplication( MApplication mapplication ) {
		instances.addAll( mapplication.instanceArray() );
	}

	private void refreshInformation() {

		final List<MApplication> distinctApps = instances
			.stream()
			.map( MInstance::application )
			.distinct()
			.toList();

		// FIXME: The creation of that AppDetail page in here is probably just to get to the functionality in WOTaskdHandler.updateForPage(). We should invoke that functionality directly instead, once we've refactored that part // Hugi 2024-11-02  
		for( MApplication mapplication : distinctApps ) {
			@SuppressWarnings("unused")
			AppDetailPage dummy = AppDetailPage.create( context(), mapplication );
		}
	}

	private WOActionResults performMonitorActionNamed( String s ) {
		String typeParam = (String)context().request().formValueForKey( "type" );

		if( "all".equalsIgnoreCase( typeParam ) ) {
			final List<String> applicationNames = siteConfig()
					.applicationArray()
					.stream()
					.map( MApplication::name )
					.toList();

			prepareApplications( applicationNames );
		}
		else {
			List appNamesParam = context().request().formValuesForKey( "name" );
			List hostsParam = context().request().formValuesForKey( "host" );

			if( "app".equalsIgnoreCase( typeParam ) ) {
				if( hostsParam == null || hostsParam.isEmpty() ) {
					prepareApplications( appNamesParam );
				}
				else {
					prepareApplicationsOnHosts( appNamesParam, hostsParam );
				}
			}
			else if( "ins".equalsIgnoreCase( typeParam ) )
				prepareInstances( appNamesParam );
			else
				throw new DirectActionException( "Invalid type " + typeParam, 406 );
		}
		refreshInformation();
		_handler.startReading();
		try {
			WOActionResults woactionresults = super.performActionNamed( s );
			return woactionresults;
		}
		finally {
			_handler.endReading();
		}
	}

	private MSiteConfig siteConfig() {
		return WOTaskdHandler.siteConfig();
	}

	@Override
	public WOActionResults performActionNamed( String s ) {
		WOResponse woresponse = new WOResponse();
		if( !siteConfig().isPasswordRequired() || siteConfig().compareStringWithPassword( context().request().stringFormValueForKey( "pw" ) ) ) {
			try {
				WOActionResults woactionresults = performMonitorActionNamed( s );
				if( woactionresults != null && (woactionresults instanceof WOResponse) ) {
					woresponse = (WOResponse)woactionresults;
				}
				else {
					woresponse.setContent( "OK" );
				}
			}
			catch( DirectActionException directactionexception ) {
				woresponse.setStatus( directactionexception.status );
				woresponse.setContent( s + " action failed: " + directactionexception.getMessage() );
			}
			catch( Exception throwable ) {
				woresponse.setStatus( 500 );
				woresponse.setContent( s + " action failed: " + throwable.getMessage() + ". See Monitor's log for a stack trace." );
				throwable.printStackTrace();
			}
		}
		else {
			woresponse.setStatus( 403 );
			woresponse.setContent( "Monitor is password protected - password missing or incorrect." );
		}
		return woresponse;
	}
}