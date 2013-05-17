package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class RemovalFromRemoteListData extends AbstractData {
	private static final long serialVersionUID = 3650775787244723907L;

	public RemovalFromRemoteListData(String sender, MessageType messageType,
			String message) {
		super(sender, messageType, message);
	}
}
