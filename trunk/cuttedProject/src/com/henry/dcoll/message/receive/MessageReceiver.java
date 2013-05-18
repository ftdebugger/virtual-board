package com.henry.dcoll.message.receive;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusMethod;

import android.util.Log;

import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.main.Runner;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.handler.AbstractMessageHandler;
import com.henry.dcoll.message.handler.MessageHandlerFactory;
import com.henry.dcoll.message.send.MessageSender;
import com.henry.dcoll.util.Serializer;

public class MessageReceiver implements IMessageReceiver, BusObject {
	private MessageHandlerFactory messageHandlerFactory;
	
	public MessageReceiver(DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		messageHandlerFactory = new MessageHandlerFactory(dSpaceContainer, messageSender);
	}
	
	@Override
	@BusMethod
	public Reply message(byte[] byteData) throws BusException {
		AbstractData abstractData = Serializer.deserializeObject(byteData);
		Log.i(Runner.TAG,"Sender {" + abstractData.getSender()
				+ "}, message type {" + abstractData.getMessageType()
				+ "}, message {" + abstractData.getMessage() + "}");
		AbstractMessageHandler messageHandler = messageHandlerFactory
				.getMessageHandler(abstractData.getMessageType());
		return messageHandler.handleMessage(abstractData);
	}
}
