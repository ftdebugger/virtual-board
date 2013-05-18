package com.henry.dcoll.message.handler;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.AdditionIntoRemoteListData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;

public class AdditionIntoRemoteListHandler extends AbstractMessageHandler {

	public AdditionIntoRemoteListHandler(String owner,
			DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		super(owner, dSpaceContainer, messageSender);
	}

	@Override
	public Reply handleMessage(AbstractData abstractData) {
		AdditionIntoRemoteListData data = (AdditionIntoRemoteListData) abstractData;
		String sender = data.getSender();
		String[] messageArray = data.getMessage().split(":");
		String space = messageArray[0];
		String listName = messageArray[1];
		int index = Integer.valueOf(messageArray[2]);
		if (dSpaceContainer.contains(space)) {
			DSpace dSpace = dSpaceContainer.get(space);
			if (dSpace.contains(listName)) {
				DListCore<?> dListCore = dSpace.getByListName(listName);
				dListCore.addProxyObject(sender, index);
			}
		}
		return new Reply();
	}

}
