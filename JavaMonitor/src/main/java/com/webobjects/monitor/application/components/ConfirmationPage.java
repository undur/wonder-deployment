package com.webobjects.monitor.application.components;

import java.util.Objects;
import java.util.function.Supplier;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.monitor.application.MonitorComponent;

import er.extensions.appserver.ERXApplication;

public class ConfirmationPage extends MonitorComponent {

	/**
	 * FIXME: Delete once we've replaced all occurrences with ConfirmationDelegate
	 */
	@Deprecated
	public interface Delegate {

		public int pageType();

		public String question();

		// FIXME: Tackle that lovely typo here // Hugi 2024-10-28
		public String explaination();

		public WOActionResults confirm();

		public WOActionResults cancel();
	}

	public record ConfirmationDelegate(
			int pageType,
			String question,
			String explaination,
			Supplier<WOActionResults> confirmFunction,
			Supplier<WOActionResults> cancelFunction ) implements Delegate {

		@Override
		public WOActionResults confirm() {
			return confirmFunction().get();
		}

		@Override
		public WOActionResults cancel() {
			return cancelFunction().get();
		}
	}

	private Delegate _delegate;

	public ConfirmationPage( WOContext context ) {
		super( context );
	}

	public Delegate delegate() {
		return _delegate;
	}

	public static ConfirmationPage create( WOContext context, Delegate delegate ) {
		Objects.requireNonNull( context );
		Objects.requireNonNull( delegate );

		final ConfirmationPage page = ERXApplication.erxApplication().pageWithName( ConfirmationPage.class );
		page._delegate = delegate;
		return page;
	}
}