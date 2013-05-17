package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class RemovalFromLocalListByIndexData extends AbstractData {
	private static final long serialVersionUID = -1386778654445582502L;

	public RemovalFromLocalListByIndexData(String sender,
			MessageType messageType, String message) {
		super(sender, messageType, message);
	}
}
