package com.henry.dcoll.message.handler;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.RemovalFromLocalListByIndexData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;
import com.henry.dcoll.util.Serializer;

public class RemovalFromLocalListByIndexHandler extends AbstractMessageHandler {

	public RemovalFromLocalListByIndexHandler(String owner,
			DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		super(owner, dSpaceContainer, messageSender);
	}

	@Override
	public Reply handleMessage(AbstractData abstractData) {
		RemovalFromLocalListByIndexData data = (RemovalFromLocalListByIndexData) abstractData;
		String message = data.getMessage();
		String[] messageArray = message.split(":");
		String space = messageArray[0];
		String listName = messageArray[1];
		int index = Integer.valueOf(messageArray[2]);
		if (dSpaceContainer.contains(space)) {
			DSpace dSpace = dSpaceContainer.get(space);
			if (dSpace.contains(listName)) {
				DListCore<?> dListCore = dSpace.getByListName(listName);
				Object object = dListCore.removeObject(index);
				return new Reply(Serializer.serializeObject(object));
			}
		}
		return new Reply();
	}

}
