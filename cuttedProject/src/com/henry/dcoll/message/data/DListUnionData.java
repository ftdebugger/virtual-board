package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class DListUnionData extends AbstractData {
	private static final long serialVersionUID = -6826973400194055683L;

	public DListUnionData(String sender, MessageType messageType, String message) {
		super(sender, messageType, message);
	}
}
