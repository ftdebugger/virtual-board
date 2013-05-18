package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class AdditionIntoLocalListData <T> extends AbstractData {
	private static final long serialVersionUID = -2626154032190850813L;
	private T object;

	public AdditionIntoLocalListData(String sender, MessageType messageType,
			String message, T object) {
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
