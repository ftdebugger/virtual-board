package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class RemovalFromLocalListByObjectData <T> extends AbstractData {
	private static final long serialVersionUID = -6144913714339815863L;
	private T object;

	public RemovalFromLocalListByObjectData(String sender,
			MessageType messageType, String message, T object) {
		super(sender, messageType, message);
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}
}
