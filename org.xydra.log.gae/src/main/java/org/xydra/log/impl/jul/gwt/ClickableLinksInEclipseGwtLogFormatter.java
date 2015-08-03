package org.xydra.log.impl.jul.gwt;

import java.io.PrintStream;
import java.util.logging.LogRecord;

import org.xydra.log.impl.jul.EclipseFormat;

import com.google.gwt.logging.impl.FormatterImpl;
import com.google.gwt.logging.impl.StackTracePrintStream;

public class ClickableLinksInEclipseGwtLogFormatter extends FormatterImpl {

	private final boolean showStackTraces;

	public ClickableLinksInEclipseGwtLogFormatter(final boolean showStackTraces) {
		this.showStackTraces = showStackTraces;
	}

	@Override
	public String format(final LogRecord event) {
		final StringBuilder message = new StringBuilder();
		message.append(EclipseFormat.format(event, 1));
		if (this.showStackTraces && event.getThrown() != null) {

			final StringBuilder builder = new StringBuilder();
			final PrintStream stream = new StackTracePrintStream(builder) {
				@Override
				public void append(final String str) {
					builder.append(str);
				}

				@Override
				public void newLine() {
					builder.append("\n");
				}
			};

			event.getThrown().printStackTrace(stream);
			return builder.toString();

		}
		return message.toString();
	}
}
