package com.henry.dcoll.message.data;

import com.henry.dcoll.message.MessageType;

public class MethodInvocationData extends AbstractData {
	private static final long serialVersionUID = -6818604777741094573L;
	private Class<?>[] parameterTypes;
	private Object[] args;

	public MethodInvocationData(String sender, MessageType messageType,
			String message, Class<?>[] parameterTypes, Object[] args) {
		super(sender, messageType, message);
		this.parameterTypes = parameterTypes;
		this.args = args;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
}
