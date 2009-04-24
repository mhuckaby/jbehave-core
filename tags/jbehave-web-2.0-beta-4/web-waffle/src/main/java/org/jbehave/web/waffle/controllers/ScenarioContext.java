package org.jbehave.web.waffle.controllers;

import static java.util.Arrays.asList;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ScenarioContext {

	private static final String EMPTY = "";
	private static final List<String> EMPTY_LIST = asList();

	private String input;
	private String output;
	private List<String> messages;
	private Throwable cause;

	public ScenarioContext() {
		this(EMPTY, EMPTY, EMPTY_LIST);
	}

	public ScenarioContext(String input, String output, List<String> messages) {
		this.input = input;
		this.output = output;
		this.messages = new ArrayList<String>();
		this.messages.addAll(messages);
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public List<String> getFailureMessages() {
		messages.clear();
		addFailureMessage(cause);
		return messages;
	}

	private void addFailureMessage(Throwable cause) {
		if ( cause != null ){
			if ( cause.getMessage() != null ){
				messages.add(cause.getMessage());
			}
			// recurse
			addFailureMessage(cause.getCause());
		}
	}

	public void clearFailureCause() {
		this.cause = null;		
	}
	
	public void runFailedFor(Throwable cause) {
		this.cause = cause;
	}

	public String getFailureStackTrace() {
		StringWriter writer = new StringWriter();
		if (cause != null) {
			cause.printStackTrace(new PrintWriter(writer));
		}
		return writer.getBuffer().toString();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


}