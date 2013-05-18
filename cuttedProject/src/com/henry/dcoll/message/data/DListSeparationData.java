package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class DListSeparationData extends AbstractData {
	private static final long serialVersionUID = -3829939673646703367L;

	public DListSeparationData(String sender, MessageType messageType,
			String message) {
		super(sender, messageType, message);
	}
}
