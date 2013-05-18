package com.henry.dcoll.message.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.MethodInvocationData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;
import com.henry.dcoll.util.Serializer;

public class MethodInvocationHandler extends AbstractMessageHandler {

	public MethodInvocationHandler(String owner,
			DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		super(owner, dSpaceContainer, messageSender);
	}

	@Override
	public Reply handleMessage(AbstractData abstractData) {
		MethodInvocationData data = (MethodInvocationData) abstractData;
		String message = data.getMessage();
		Class<?>[] parameterTypes = data.getParameterTypes();
		Object[] args = data.getArgs();
		String[] messageArray = message.split(":");
		String space = messageArray[0];
		String listName = messageArray[1];
		Integer index = Integer.valueOf(messageArray[2]);
		String methodName = messageArray[3];
		if (dSpaceContainer.contains(space)) {
			DSpace dSpace = dSpaceContainer.get(space);
			if (dSpace.contains(listName)) {
				DListCore<?> dListCore = dSpace.getByListName(listName);
				Object object = dListCore.getLocalList().get(index);
				Object result = null;
				try {
					Method method = object.getClass().getMethod(methodName, parameterTypes);
					result = method.invoke(object, args);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}				
				return new Reply("", "", Serializer.serializeObject(result));
			}
		}
		return new Reply();
	}

}
