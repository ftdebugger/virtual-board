package com.henry.dcoll.message.handler;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.RemovalFromLocalListByObjectData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;
import com.henry.dcoll.util.Serializer;

public class RemovalFromLocalListByObjectHandler extends AbstractMessageHandler {

	public RemovalFromLocalListByObjectHandler(String owner,
			DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		super(owner, dSpaceContainer, messageSender);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Reply handleMessage(AbstractData abstractData) {
		RemovalFromLocalListByObjectData data = (RemovalFromLocalListByObjectData) abstractData;
		String message = data.getMessage();
		String[] messageArray = message.split(":");
		String space = messageArray[0];
		String listName = messageArray[1];
		Object object = data.getObject();
		if (dSpaceContainer.contains(space)) {
			DSpace dSpace = dSpaceContainer.get(space);
			if (dSpace.contains(listName)) {
				DListCore<?> dListCore = dSpace.getByListName(listName);
				int index = dListCore.indexOfObject(object);
				if (index > 0) {
					dListCore.removeObject(object);
					return new Reply(Serializer.serializeObject(index));
				}
			}
		}
		return new Reply(Serializer.serializeObject(-1));
	}

}
