package com.henry.dcoll.message.handler;

import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;

public abstract class AbstractMessageHandler {
	protected String owner;
	protected DSpaceContainer dSpaceContainer;
	protected MessageSender messageSender;
	
	public AbstractMessageHandler(String owner, DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		this.owner = owner;
		this.dSpaceContainer = dSpaceContainer;
		this.messageSender = messageSender;
	}

	public abstract Reply handleMessage(AbstractData abstractData);
}
