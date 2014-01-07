package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import com.google.gwt.logging.impl.FormatterImpl;


public class ClickableLinksInEclipseGwtLogFormatter extends FormatterImpl {
    
    private boolean showStackTraces;
    
    public ClickableLinksInEclipseGwtLogFormatter(boolean showStackTraces) {
        this.showStackTraces = showStackTraces;
    }
    
    @Override
    public String format(LogRecord event) {
        StringBuilder message = new StringBuilder();
        message.append(EclipseFormat.format(event, 1));
        if(this.showStackTraces && event.getThrown() != null) {
            message.append(getStackTraceAsString(event.getThrown(), "\n", "\t"));
        }
        return message.toString();
    }
}
