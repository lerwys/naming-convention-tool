package org.openepics.names.ui.common;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.Iterator;

/**
 * A global JSF exception handler that displays caught exceptions in the UI as popup messages.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {
    
    private final ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override public ExceptionHandler getWrapped() { return this.wrapped; }

    @Override public void handle() throws FacesException {
        final Iterator iterator = getUnhandledExceptionQueuedEvents().iterator();
        while (iterator.hasNext()) {
            final ExceptionQueuedEvent event = (ExceptionQueuedEvent) iterator.next();
            final ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            final Throwable throwable = context.getException();
            try {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unexpected error", throwable.getMessage()));
            } finally {
                iterator.remove();
            }
        }
        wrapped.handle();
    }
}
