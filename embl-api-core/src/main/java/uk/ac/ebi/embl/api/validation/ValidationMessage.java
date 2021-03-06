/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ValidationMessage<T extends Origin> implements Serializable {

	private static final long serialVersionUID = -2932989221653951201L;

	/**
	 * Message severity.
	 */
	private Severity severity;
	
	/**
	 * Message key (from message bundle) 
	 */
	private String messageKey;
	
	/**
	 * Message parameters 
	 */
	private Object[] params;
	
	/**
	 * Validation origin - where validation problem occurred
	 */
	private List<T> origins;

    /**
     * The message to which the messageKey resolves
     */
    private String message;

    /**
     * additional information to help resolve the error/warning/info 
     */
    private String curatorMessage;

    /**
     * A full-text 'report' if needed - will be rendered on a separate page as should contain a lot of text
     */
    private String reportMessage;

    /**
     * An exception associated with the validation message.
     */
    private Throwable throwable;
    
    /**
     * Static string denoting that there is no message key for this message
     */
    public final static String NO_KEY = "NO_KEY";

    public ValidationMessage(Severity severity, String messageKeyParam, Object... params) {
        this.severity = severity;
        this.params = params;
        this.origins = new ArrayList<T>();
        this.messageKey = messageKeyParam;
        if (!messageKeyParam.equals(NO_KEY)) {
            this.message = ValidationMessageManager.getString(messageKeyParam, params);
        }
    }

    public Object[] getParams() {
        return params;
    }

    /**
	 * Returns the message string.
	 * 
	 * @return message string
	 */
	public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getMessageKey() {
		return messageKey;
	}

	/**
	 * Adds an origin to the validation message.
	 * 
	 * @param origin an origin to be added to the validation message
	 */
	protected void addOrigin(T origin) {
		if (origin != null) {
			this.origins.add(origin);
		}
	}	

	/**
	 * Adds a collection of origins to the validation message.
	 *
	 * @param origins the origins to be added to the validation message
	 */
	protected void addOrigins(Collection<T> origins) {
		if (this.origins != null) {
			this.origins.addAll(origins);
		}
	}

	/**
	 * Appends an origin to the validation message.
	 * 
	 * @param origin an origin to be added to the validation message
	 * @return a reference to this object
	 */
	public ValidationMessage<T> append(T origin) {
		addOrigin(origin);
		return this;
	}	

	/**
	 * Appends origins to the validation message.
	 *
	 * @param origins origins to be added to the validation message
	 * @return a reference to this object
	 */
	public ValidationMessage<T> append(Collection<T> origins) {
		addOrigins(origins);
		return this;
	}

	/**
	 * Gets an list of all origins.
	 * 
	 * @return an unmodifiable list of all origins
	 */
	public List<T> getOrigins() {
		return this.origins;
	}

    public boolean isHasCuratorMessage() {
        return curatorMessage != null;
    }

    public String getCuratorMessage() {
        return curatorMessage;
    }

    public void setCuratorMessage(String curatorMessage) {
        this.curatorMessage = curatorMessage;
    }

    public void appendCuratorMessage(String curatorMessage) {
        if(this.curatorMessage != null){
            this.curatorMessage = this.curatorMessage + " " + curatorMessage;
        }else{
            this.curatorMessage = curatorMessage;
        }
    }

    public boolean isHasReportMessage(){
        return reportMessage != null;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }
    
    public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("severity", severity);
		builder.append("messageKey", messageKey);
		builder.append("params", params);
		builder.append("origins", origins);
		builder.append("message", getMessage());
		return builder.toString();
	}

	/**
	 * Creates a ValidationMessage - severity ERROR
	 * 
	 * @param messageKey message key
	 * @param params message parameters
	 * @return a validation message - severity ERROR
	 */
	public static ValidationMessage<Origin> error(String messageKey,Object... params) {
		return ValidationMessage.message(Severity.ERROR, messageKey, params);
	}

	/**
	 * Creates a ValidationMessage - severity WARNING
	 * 
	 * @param messageKey message key
	 * @param params message parameters
	 * @return a validation message - severity WARNING
	 */	
	public static ValidationMessage<Origin> warning(String messageKey, Object... params) {
		return ValidationMessage.message(Severity.WARNING, messageKey, params);
	}

	/**
	 * Creates a ValidationMessage - severity INFO
	 * 
	 * @param messageKey message key
	 * @param params message parameters
	 * @return a validation message - severity INFO
	 */		
	public static ValidationMessage<Origin> info(String messageKey, Object... params) {
        return ValidationMessage.message(Severity.INFO, messageKey, params);
	}

	/**
	 * Creates a ValidationMessage with provided severity
	 * 
	 * @param severity message severity
	 * @param messageKey message key
	 * @param params message parameters
	 * @return a validation message - severity INFO
	 * @return created validation message
	 */
	public static ValidationMessage<Origin> message(Severity severity, String messageKey, Object... params) {
		return new ValidationMessage<Origin>(severity, messageKey, params);
	}
		
	/** Writes the message in text format.
     * @param writer
     */
	public void writeTextMessage(Writer writer) throws IOException {
		if (getSeverity() == Severity.ERROR) {
			writer.write("ERROR: ");
		}
		else if (getSeverity() == Severity.WARNING) {
			writer.write("WARNING: ");
		}
		else if (getSeverity() == Severity.INFO) {
			writer.write("INFO: ");
		}
		writer.write(getMessage());
		for (Origin origin : getOrigins()) {
			String originText = origin.getOriginText();
			writer.write(originText);
		}
		writer.write("\n");
	}

	/** Writes the message in xml format. 
	 */		
	public void writeXmlMessage() {		
	}	
}
