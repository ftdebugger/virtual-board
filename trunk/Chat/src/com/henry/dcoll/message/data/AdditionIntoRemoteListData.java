package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class AdditionIntoRemoteListData extends AbstractData {
	private static final long serialVersionUID = -1196498097145842157L;

	public AdditionIntoRemoteListData(String sender, MessageType messageType,
			String message) {
		super(sender, messageType, message);
	}
}
