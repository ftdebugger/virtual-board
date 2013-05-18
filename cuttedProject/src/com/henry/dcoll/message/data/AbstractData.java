package com.henry.dcoll.message.data;

import java.io.Serializable;

import com.henry.dcoll.message.MessageType;

public abstract class AbstractData implements Serializable {
	private static final long serialVersionUID = -4745511490459337318L;
	private String sender;
	private MessageType messageType;
	private String message;

	public AbstractData(String sender, MessageType messageType, String message) {
		super();
		this.sender = sender;
		this.messageType = messageType;
		this.message = message;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
